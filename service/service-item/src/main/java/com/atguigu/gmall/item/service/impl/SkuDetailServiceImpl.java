package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.starter.cache.annotation.GmallCache;
import com.atguigu.starter.cache.service.CacheOpsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service  //单实例service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Autowired
    SkuDetailFeignClient skuDetailFeignClient;


    /**
     * Map作为缓存【本地缓存】：优缺点
     * 优点：
     * 缺点：
     *   1、100w的数据内存够不够
     */
//    private Map<Long,SkuDetailTo> skuCache = new ConcurrentHashMap<>();


    /**
     * 可配置的线程池，可自动注入
     */
    @Autowired
    ThreadPoolExecutor executor;


    @Autowired
    StringRedisTemplate redisTemplate;


    //每个skuId，关联一把自己的锁
    Map<Long,ReentrantLock> lockPool = new ConcurrentHashMap<>();
    //锁的粒度太大了，把无关的人都锁住了
    ReentrantLock lock = new ReentrantLock(); //锁的住


    @Autowired
    CacheOpsService cacheOpsService;


    /**
     * 表达式中的params代表方法的所有参数列表
     * @param skuId
     * @return
     */
    @GmallCache(
            cacheKey =SysRedisConst.SKU_INFO_PREFIX+"#{#params[0]}",
            bloomName = SysRedisConst.BLOOM_SKUID,
            bloomValue = "#{#params[0]}",
            lockName = SysRedisConst.LOCK_SKU_DETAIL+"#{#params[0]}"
    )
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {
        SkuDetailTo fromRpc = getSkuDetailFromRpc(skuId);
        return fromRpc;
    }


    //未缓存优化前 - 400/s
    public SkuDetailTo getSkuDetailFromRpc(Long skuId) {
        SkuDetailTo detailTo = new SkuDetailTo();

        //以前   一次远程超级调用【网络交互比较浪费时间】  查出所有数据直接给我们返回
        // 一次远程用 2s
//        CountDownLatch downLatch = new CountDownLatch(6);

        // 6次远程调用 6s + 0.5*6 = 9s

        //同步调用
        //远程调用其实不用等待，各查各的。 异步的方式


        //CompletableFuture.runAsync()// CompletableFuture<Void>  启动一个下面不用它返回结果的异步任务
        //CompletableFuture.supplyAsync()//CompletableFuture<U>  启动一个下面用它返回结果的异步任务

        //1、查基本信息   1s
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            Result<SkuInfo> result = skuDetailFeignClient.getSkuInfo(skuId);
            SkuInfo skuInfo = result.getData();
            detailTo.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);


        //2、查商品图片信息  1s
        CompletableFuture<Void> imageFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if(skuInfo != null){
                Result<List<SkuImage>> skuImages = skuDetailFeignClient.getSkuImages(skuId);
                skuInfo.setSkuImageList(skuImages.getData());
            }

        }, executor);


        //3、查商品实时价格 2s
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            Result<BigDecimal> price = skuDetailFeignClient.getSku1010Price(skuId);
            detailTo.setPrice(price.getData());
        }, executor);


        //4、查销售属性名值
        CompletableFuture<Void> saleAttrFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if(skuInfo != null) {
                Long spuId = skuInfo.getSpuId();
                Result<List<SpuSaleAttr>> saleattrvalues = skuDetailFeignClient.getSkuSaleattrvalues(skuId, spuId);
                detailTo.setSpuSaleAttrList(saleattrvalues.getData());
            }

        }, executor);


        //5、查sku组合
        CompletableFuture<Void> skuVlaueFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if(skuInfo!=null){
                Result<String> sKuValueJson = skuDetailFeignClient.getSKuValueJson(skuInfo.getSpuId());
                detailTo.setValuesSkuJson(sKuValueJson.getData());
            }
        }, executor);


        //6、查分类
        CompletableFuture<Void> categoryFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if(skuInfo!= null){
                Result<CategoryViewTo> categoryView = skuDetailFeignClient.getCategoryView(skuInfo.getCategory3Id());
                detailTo.setCategoryView(categoryView.getData());
            }
        }, executor);


        //异步实际上是： 空间换时间；  new Thread()
        //最一行： 串行： 6s
        //最一行： 并行： 等待一个最长时间，全部任务都能完成。
        //如果异步：  new Thread().start();
        //  不能直接用 new Thread().start();
        //          一个请求进来，直接无脑开6个线程，高并发下直接OOM。
        //          一个一炸可能导致整个集群雪崩。
        // 不能无脑开线程，很容易资源耗尽，池技术（线程池、连接池、xxx池）【资源复用问题】
        //   线程池+阻塞队列：解决资源复用与等待问题。


        //6个任务都结束后，To才能返回
        //
        //1、CompletableFuture 异步【编排】
        //启动一个异步任务有多少种方法？
        //1、new Thread().start()
        //2、Runnable  new Thread(runnable).start();
        //3、Callable  带结果  FutureTask
        //4、线程池
        //     executor.submit(()->{});  executor.execute(()->{});
        //5、异步编排 CompletableFuture
        //    - CompletableFuture启动异步任务
        //    -

        CompletableFuture
                .allOf(imageFuture, priceFuture, saleAttrFuture, skuVlaueFuture, categoryFuture)
                .join();

        return detailTo;
    }



    /**
     *
     * 切面点表达式怎么写？
     *  execution(* com.atguigu.gmall.item.**.*(..))
     * @param skuId
     * @return
     */
    public SkuDetailTo getSkuDetailWithCache(Long skuId) {
        String cacheKey = SysRedisConst.SKU_INFO_PREFIX +skuId;
        //1、先查缓存
        SkuDetailTo cacheData = cacheOpsService.getCacheData(cacheKey,SkuDetailTo.class);
        //2、判断
        if(cacheData == null){
            //3、缓存没有
            //4、先问布隆，是否有这个商品
            boolean contain = cacheOpsService.bloomContains(skuId);
            if(!contain){
                //5、布隆说没有，一定没有
                log.info("[{}]商品 - 布隆判定没有，检测到隐藏的攻击风险....",skuId);
                return null;
            }
            //6、布隆说有，有可能有，就需要回源查数据
            boolean lock = cacheOpsService.tryLock(skuId); //为当前商品加自己的分布式锁。100w的49号查询只会放进一个
            if(lock){
                //7、获取锁成功，查询远程
                log.info("[{}]商品 缓存未命中，布隆说有，准备回源.....",skuId);
                SkuDetailTo fromRpc = getSkuDetailFromRpc(skuId);
                //8、数据放缓存
                cacheOpsService.saveData(cacheKey,fromRpc);
                //9、解锁
                cacheOpsService.unlock(skuId);
                return fromRpc;
            }
            //9、没获取到锁
            try {Thread.sleep(1000);
               return cacheOpsService.getCacheData(cacheKey,SkuDetailTo.class);
            } catch (InterruptedException e) {

            }
        }
        //4、缓存中有
        return cacheData;
    }

    //    void redisLock(){
//        //每个请求都会生成一个唯一令牌
//        String token = UUID.randomUUID().toString();
//        //1、加锁。锁的key只要不变就是同一把锁。锁的值，每个线程用自己的唯一值
//        boolean b =  setnxex("lock",token,60s);
//        //2、判断
//        if(b){
////            //3、加上过期时间
////            setexpire("lock",10s);
//            //4、超长业务，业务期间可能断电。即使断电，redis自动解锁
//
//            //5、解锁
//            String v = redisget("lock");
//            if(token.equals(v)){ //说明就是我的锁
//                del("lock"); //依然可能删掉别人的锁
//            }
//            else {
//                //不是你的锁。不要动
//            }
//        }else {
//            //睡眠等1s，只查缓存
//        }
//
//    }

      //500w  100w：49  100w：50  100w：51   100w: 52    100w: 53
    public SkuDetailTo getSkuDetailXxxxFeature(Long skuId) {
        lockPool.put(skuId,new ReentrantLock());
        //每个不同的sku，用自己专用的锁
        //1、看缓存中有没有  sku:info:50
        String jsonStr = redisTemplate.opsForValue().get("sku:info:" + skuId);
        if ("x".equals(jsonStr)) {
            //说明以前查过，只不过数据库没有此记录，为了避免再次回源，缓存了一个占位符
            return null;
        }
        //
        if (StringUtils.isEmpty(jsonStr)) {
            //2、redis没有缓存数据
            //2.1、回源。之前可以判断redis中保存的sku的id集合，有没有这个id
            //防止随机值穿透攻击？ 回源之前，先要用布隆/bitmap判断有没有
//            int result = getbit(49);
            // TODO 加锁解决击穿
            SkuDetailTo fromRpc = null;

            //JVM 抢不到锁的等1s。 怎么判断synchronized 抢到还是没抢到？

//            ReentrantLock lock = new ReentrantLock();  //锁不住

//            lock.lock(); //等锁，必须等到锁
            //判断锁池中是否有自己的锁
            //锁池中不存在就放一把新的锁，作为自己的锁，存在就用之前的锁
            ReentrantLock lock = lockPool.putIfAbsent(skuId, new ReentrantLock());

            boolean b = this.lock.tryLock(); //立即尝试加锁，不用等，瞬发。等待逻辑在业务上 .抢一下，不成就不用再抢了
//            boolean b = lock.tryLock(1, TimeUnit.SECONDS); //等待逻辑在锁上.1s内，CPU疯狂抢锁
            if(b){
                //抢到锁
                fromRpc = getSkuDetailFromRpc(skuId);
            }else {
                //没抢到
//                Thread.sleep(1000);
                jsonStr = redisTemplate.opsForValue().get("sku:info:" + skuId);
                //逆转为 SkuDetailTo
                return null;
            }




            //2.2、放入缓存【查到的对象转为json字符串保存到redis】
            String cacheJson = "x";
            if (fromRpc != null) {
                cacheJson = Jsons.toStr(fromRpc);
                //加入雪崩解决方案。固定业务时间+随机过期时间
                redisTemplate.opsForValue().set("sku:info:" + skuId, cacheJson, 7, TimeUnit.DAYS);
            } else {
                redisTemplate.opsForValue().set("sku:info:" + skuId, cacheJson, 30, TimeUnit.MINUTES);
            }

            return fromRpc;
        }
        //3、缓存中有. 把json转成指定的对象
        SkuDetailTo skuDetailTo = Jsons.toObj(jsonStr, SkuDetailTo.class);
        return skuDetailTo;
    }




//    @Override  //使用本地缓存
//    public SkuDetailTo getSkuDetail(Long skuId) {
//
//        //1、先看缓存
//        SkuDetailTo cacheData = skuCache.get(skuId);
//        //2、判断
//        if(cacheData == null){
//            //3、缓存没有，真正查询【回源（回到数据源头真正检索）】【提高缓存的命中率】
//            // 1 - 0/1： 0%
//            // 2 - 1/2: 50%
//            // N - (N-1)/N： 无限接近100%
//            //缓存命中率提升到100%；预缓存机制；
//            SkuDetailTo fromRpc = getSkuDetailFromRpc(skuId);
//            skuCache.put(skuId,fromRpc);
//            return fromRpc;
//        }
//        //4、缓存有
//        return cacheData;
//    }
}
