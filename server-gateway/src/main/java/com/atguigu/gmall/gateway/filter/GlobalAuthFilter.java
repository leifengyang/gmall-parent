package com.atguigu.gmall.gateway.filter;

import org.bouncycastle.asn1.x509.sigi.PersonalData;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Webflux：响应式web编程【消息队列分布式】
 *      内存版的消息队列
 *      //1
 *      Future fu = aService.b();
 *      //2
 *      fu.get();
 *
 * Servlet：阻塞式编程方式
 *
 */
@Component
public class GlobalAuthFilter implements GlobalFilter{


    /**
     * Mono：
     * Flux：
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }


    /**
     * StreamAPI
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
//        //1、集合
//        List<Integer> integers = Arrays.asList(1,2,3,4);
//        //2、每个元素加2
//        Integer integer = integers.stream()
//                .map((t) -> t + 2)
//                .reduce((a, b) -> a + b)
//                .get();
//        System.out.println(integer);

        //1、单数据流。 数据发布者
//        Mono<Integer> just = Mono.just(1);
        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1));

        //2、数据订阅者，感兴趣发布者的数据
        flux.subscribe((t)->{
            System.out.println("消费者1："+t);
        });


        flux.subscribe((t)->{
            System.out.println("消费者2："+t);
        });


        flux.subscribe((t)->{
            System.out.println("消费者3："+t);
        });

//        //js 的回调机制
//        Mono<Person> person = getPerson();
//        Thread.sleep(10000000L);
//
//        person.subscribe((t)->{
//            //拿到数据做业务
//        });
    }


}
