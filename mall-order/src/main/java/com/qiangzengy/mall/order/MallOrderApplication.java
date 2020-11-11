package com.qiangzengy.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 使用 RabbitMQ
 * 1、引入amgp场景; RabbitAutoConfiguration就会自动生效
 *
 * 2、给容器中自动配置了
 *     RabbitTemplate、 AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 *     所有的属性都是 spring.rabbitmq
 *     ConfigurationProperties(prefix ="spring, rabbitmq")
 *     pubLic cLass RabbitProperties
 *
 * 3、给配置文件中配置 spring, rabbitmq信息
 *
 * 4、@Enablerabb1: @EnabLeXxxXX;开启功能
 *
 * 5、监听消息:使用 @Rabbitlistener作用在类+方法上；必须有@EnableRabbit，
 *   *            @RabbitHandler作用在方法上（重载区分不同的消息）
 *
 *
 *
 * 接口幂等性问题：
 *     解决：1。token机制
 *     1、服务端提供了发送 token的接口。我们在分析业务的时候,哪些业务是存在幂等问题的,
 *        就必须在执行业务前,先去获取 token,服务器会把 token保存到reds中。
 *     2、然后调用业务接囗请求时,把 token携带过去,一般放在请求头部。
 *     3、服务器判断 token是否存在reds中,存在表示第一次请求,然后删除 token继续执行业
 *        务。
 *     4、如果判断 token不存在reds中,就表示是重复操作,直接返回重复标记给cent,这样
 *        就保证了业务代码,不被重复执行。
 * 危险性:
 * 1、先刪除 token还是后删除 token;
 * (1)先删除可能导致,业务确实没有执行,重试还带上之前 token,由于防重设计导致,
 * 请求还是不能执行。
 * (2)后删除可能导致,业务处理成功,但是服务闪断,出现超时,没有删除 token,别
 * 人继续重试,导致业务被执行两边
 * 我们最好设计为先删除 token,如果业务调用失败,就重新获取 token再次请求。
 * token获取、比较和删除必须是原子性
 * (1) redisget(token)、 tokenequals、 redis del(token)如果这两个操作不是原子,可能导
 * 致,高并发下,都get到同样的数据,判断都成功,继续业务并发执行
 * (2)可以在 redis使用lua脚本完成这个操作
 *   if redis call('get, KEYS(1])==ARGV[1]then return redis call('del, KEYS(1))else return O end
 *
 *   本地事务失效问题：
 *      同一个对象内事务方法互调默认失效，原因：绕过了代理对象，事务是使用代理对象来控制的
 *    解决方案：使用代理对象来调用事务
 *       1，引入spring-boot-starter-aop，(AspectJAutoProxy)
 *       2，@EnableAspectJAutoProxy 开启aspectj动态代理功能，以后所有的动态代理都是它创建的，不是jdk创建的。（好处：即使没有接口也可以创建动态代理）
 *       (exposeProxy = true) 对外暴露代理对象
 *       3，本类互调用代理对象
 *               OrderServiceImpl o = (OrderServiceImpl)AopContext.currentProxy();
 *               o.b();
 */


@EnableDiscoveryClient
@SpringBootApplication
@EnableRabbit
@MapperScan("com.qiangzengy.mall.order.dao")
@EnableFeignClients("com.qiangzengy.mall.order.feign")
@EnableAspectJAutoProxy(exposeProxy = true)
public class MallOrderApplication {

    /**
     *
     * 1.下单流程
     *
     * 判断用户是否登录，没有登录则去登录，登录了则进行下单操作，判断商品库存是否够，选择收获地址，是否金币抵扣，创建订单，支付订单
     *
     *
     *
     *
     */

    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
