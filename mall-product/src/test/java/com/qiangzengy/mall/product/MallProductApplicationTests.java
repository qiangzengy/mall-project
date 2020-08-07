package com.qiangzengy.mall.product;

import com.qiangzengy.mall.product.entity.BrandEntity;
import com.qiangzengy.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private RedissonClient redissonClient;


    @Test
   public void contextLoads() {

        BrandEntity entity=new BrandEntity();
        entity.setName("123");
        brandService.save(entity);
        System.out.println("成功");

    }

    @Test
    void testRedisLock(){
        /**
         * 测试reidsson分布式锁：
         * 大家都知道，如果负责储存这个分布式锁的Redisson节点宕机以后，
         * 而且这个锁正好处于锁住的状态时，这个锁会出现锁死的状态。为了避免这种情况的发生，
         * Redisson内部提供了一个监控锁的看门狗，
         * 它的作用是在Redisson实例被关闭前，不断的延长锁的有效期。
         * 默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定。
         * 文档：https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
         */
        //获取锁，只要锁的名字一样就是同一把锁
        RLock rLock = redissonClient.getLock("anyLock");
        //加锁
        rLock.lock();//阻塞
        try {
            System.out.println("加锁成功。。。。。。。。；线程id："+Thread.currentThread().getId());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //解锁
            rLock.unlock();
            System.out.println("解锁成功。。。。。。。。。; 线程id："+Thread.currentThread().getId());
        }
    }


    @Test
    void testRedis(){

        Thread thread1 = new Thread(() -> {
                //获取锁，只要锁的名字一样就是同一把锁
                RLock rLock = redissonClient.getLock("anyLock");
                //加锁
                rLock.lock();//阻塞
            try {
                System.out.println("thread1  加锁成功。。。。。。。。；线程id："+Thread.currentThread().getId());
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //解锁
                rLock.unlock();
                System.out.println("thread1 解锁成功。。。。。。。。。; 线程id："+Thread.currentThread().getId());
            }

        });
        Thread thread2 = new Thread(() -> {

            //获取锁，只要锁的名字一样就是同一把锁
            RLock rLock = redissonClient.getLock("anyLock");
            //加锁
            rLock.lock();//阻塞
            try {
                System.out.println("thread2 加锁成功。。。。。。。。；线程id："+Thread.currentThread().getId());
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //解锁
                rLock.unlock();
                System.out.println("thread2 解锁成功。。。。。。。。。; 线程id："+Thread.currentThread().getId());
            }

        });

        thread1.start();
        thread2.start();


    }

    public static void main(String[] args) {


        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("A" + "运行  :  " + i);
                try {
                    new Thread().sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });


        Thread thread2 = new Thread(() -> {

            for (int i = 0; i < 5; i++) {
                System.out.println("B" + "运行  :  " + i);

                try {
                    new Thread().sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        thread1.start();
        thread2.start();
    }

}
