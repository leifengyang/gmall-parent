package com.atguigu.starter.cache.aspect;


import com.atguigu.starter.cache.annotation.GmallCache;
import com.atguigu.starter.cache.constant.SysRedisConst;
import com.atguigu.starter.cache.service.CacheOpsService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;


@Aspect //申明这是一个切面
@Component
public class CacheAspect {


    @Autowired
    CacheOpsService cacheOpsService;

    //创建一个表达式解析器，这个是线程安全的
    ExpressionParser parser = new SpelExpressionParser();
    ParserContext context = new TemplateParserContext();
    /**
     * 通知：告诉
     */
    //在标注了 @GmallCache 注解的方法之前执行
//    @Before("@annotation(com.atguigu.gmall.item.cache.annotation.GmallCache)")
//    public void haha(){
//        System.out.println("前置通知......");
//    }


    /**
     *   目标方法： public SkuDetailTo getSkuDetailWithCache(Long skuId)
     *   连接点：所有目标方法的信息都在连接点
     *
     *   try{
     *       //前置通知
     *       目标方法.invoke(args)
     *       //返回通知
     *   }catch(Exception e){
     *       //异常通知
     *   }finally{
     *       //后置通知
     *   }
     */
    @Around("@annotation(com.atguigu.starter.cache.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;

        //key不同方法可能不一样
        String cacheKey = determinCacheKey(joinPoint);

        //1、先查缓存
        // OrderInfo getOrderInfo(Long orderId);
        // Map<String,OrderInfo> getOrderInfo();
        Type returnType = getMethodGenericReturnType(joinPoint);
        Object cacheData = cacheOpsService.getCacheData(cacheKey,returnType);

        //2、缓存
        if(cacheData == null){
            //3、准备回源
            //4、先问布隆。 有些场景并不一定需要布隆。比如：三级分类（只有一个大数据）
//            boolean contains = cacheOpsService.bloomContains(arg);
            String bloomName = determinBloomName(joinPoint);
            if(!StringUtils.isEmpty(bloomName)){
                //指定开启了布隆
                Object bVal = determinBloomValue(joinPoint);
                boolean contains = cacheOpsService.bloomContains(bloomName,bVal);
                if(!contains){
                    return null;
                }
            }


            //5、布隆说有，准备回源。有击穿风险
            boolean lock = false;
            String lockName = "";
            try {
                //不同场景用自己的锁
                lockName = determinLockName(joinPoint);
                lock = cacheOpsService.tryLock(lockName); //49
                if(lock){

                    //6、获取到锁，开始回源
                    result = joinPoint.proceed(joinPoint.getArgs());
                    //7、调用成功，重新保存到缓存
                    cacheOpsService.saveData(cacheKey,result);
                    return result;
                }else {
                    Thread.sleep(1000L);
                    return  cacheOpsService.getCacheData(cacheKey,returnType);
                }
            }finally {
                if(lock) cacheOpsService.unlock(lockName);
            }

        }


        //X、缓存中有直接返回
        return cacheData;
    }


    /**
     * 根据表达式计算出要用的锁的名字
     * @param joinPoint
     * @return
     */
    private String determinLockName(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        //3、拿到锁表达式
        String lockName = cacheAnnotation.lockName(); //lock-方法名
        if(StringUtils.isEmpty(lockName)){
            //没指定锁用方法级别的锁
            return SysRedisConst.LOCK_PREFIX+method.getName();
        }

        //4、计算锁值
        String lockNameVal = evaluationExpression(lockName, joinPoint, String.class);
        return lockNameVal;
    }


    /**
     * 根据布隆过滤器值表达式计算出布隆需要判定的值
     * @param joinPoint
     * @return
     */
    private Object determinBloomValue(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        //3、拿到布隆值表达式
        String bloomValue = cacheAnnotation.bloomValue();

        Object expression = evaluationExpression(bloomValue, joinPoint, Object.class);

        return expression;
    }

    /**
     * 获取布隆过滤器的名字
     * @param joinPoint
     * @return
     */
    private String determinBloomName(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        String bloomName = cacheAnnotation.bloomName();

        return bloomName;
    }


    /**
     * 获取目标方法的精确返回值类型
     * @param joinPoint
     * @return
     */
    private Type getMethodGenericReturnType(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();
        Type type = method.getGenericReturnType();
        return type;
    }


    /**
     * 根据当前整个连接点的执行信息，确定缓存用什么key
     * @param joinPoint
     * @return
     */
    private String determinCacheKey(ProceedingJoinPoint joinPoint) {
        //1、拿到目标方法上的@GmallCache注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        //2、拿到注解
        GmallCache cacheAnnotation = method.getDeclaredAnnotation(GmallCache.class);

        String expression = cacheAnnotation.cacheKey();

        //3、根据表达式计算缓存键
        String cacheKey = evaluationExpression(expression,joinPoint,String.class);

        return cacheKey;
    }

    private<T> T evaluationExpression(String expression,
                                        ProceedingJoinPoint joinPoint,
                                        Class<T> clz) {
        //1、得到表达式
        Expression exp = parser.parseExpression(expression, context);

        //2、sku:info:#{#params[0]}
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        //3、取出所有参数，绑定到上下文
        Object[] args = joinPoint.getArgs();
        evaluationContext.setVariable("params",args);

        //4、得到表达式的值
        T expValue = exp.getValue(evaluationContext, clz);
        return expValue;
    }
}
