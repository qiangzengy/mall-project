package com.qiangzengy.mall.product.web;

import com.qiangzengy.mall.product.entity.CategoryEntity;
import com.qiangzengy.mall.product.entity.vo.Catalog2Vo;
import com.qiangzengy.mall.product.service.CategoryService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //1.查出所有的1级分类
        List<CategoryEntity> categoryEntities=categoryService.getLevel1Category();
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }


    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson(){

        Map<String,List<Catalog2Vo>> catelogMap=categoryService.getCatalogJsonFromDBDb();
        return catelogMap;

    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){

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

        return "HelloWord";
    }



}
