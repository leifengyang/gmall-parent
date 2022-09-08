package com.atguigu.gmall.web.lock;


import java.lang.reflect.Method;

public class HelloLock {
    volatile int i = 0;

    /**
     * 线程要执行方法做三件事
     * 1、抢锁
     * 2、获取CPU执行权
     * 3、准备方法上下文信息执行
     *
     * 只要wait();
     * 1、先释放锁
     * 2、释放CPU执行权
     * 3、保留现场
     * 4、只有别人把你notify了
     *
     * 抢到锁的人；
     * 1、等待CPU时间片转到执行你。
     *
     * new Thread(()->{
     *         new HelloLock().hello(); //静态方法，是类在调
     *         HelloLock.hello(); //
     * }).start()
     *
     *
     * new HelloLock().hello();
     * synchronized加到方法上，方法的调用者是谁，锁就是他
     */

    public synchronized static void hello(){
        //回答？不是
        //万物皆对象
        Method method = null;



    }

    public static void main(String[] args) {
        //String 原理
        String s = new String("1").intern();
        String s1 = new String("1").intern();
        System.out.println(s == s1);

        String aa = "222";
        String bb = "222";
        System.out.println(aa == bb);

//        Long integer1 = new Long(10);
//        Long integer2 = new Long(10);
//        System.out.println(integer1 == integer2);
        //-128 ~ 127 之间的数据
        //区间外的现场new
        Long l1 = 100L; //自动装箱
        Long l2 = 100L;
        System.out.println(l1.equals(l2));
    }
    // new Thread() 100个 运行 hello()
}
