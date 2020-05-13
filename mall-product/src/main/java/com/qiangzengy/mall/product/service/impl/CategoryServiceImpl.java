package com.qiangzengy.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.utils.Query;
import com.qiangzengy.mall.product.entity.vo.Catalog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;

import com.qiangzengy.mall.product.dao.CategoryDao;
import com.qiangzengy.mall.product.entity.CategoryEntity;
import com.qiangzengy.mall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> treeList() {


        //1.查询所有分类
        List<CategoryEntity> categoryEntityList=baseMapper.selectList(null);
        //2.组装成父子树形结构
        //2.1找到所有的一级分类
        List<CategoryEntity> categoryList=categoryEntityList.stream().filter(categoryEntity ->
                //一级分类，parentCid为0；
                categoryEntity.getParentCid()==0
        ).map((category)->{
            //封装递归方法，找到每个类目的子类目；
            category.setChildren(getChildren(category,categoryEntityList));
            return category;
            //排序，对两个类目进行对比,该类目可能为null，需要判断，如果为null,赋值为0
            /* (category1,category2)->{
                   return (category1.getSort()==null?0:category1.getSort())-(category2.getSort()==null?0:category2.getSort());
                     }*/
        }).sorted(
                Comparator.comparingInt(category -> (category.getSort() == null ? 0 : category.getSort()))
        ).collect(Collectors.toList());

        return categoryList;
    }


    /**
     *获取每个类目的子类目需要两个参数
     * @param entity 当前类目
     * @param all 所有的类目
     * @return
     */
    private List<CategoryEntity>getChildren(CategoryEntity entity,List<CategoryEntity> all){
        //entity的id等于其子类目的父id，即
        List<CategoryEntity>categoriesList = all.stream().filter(categories ->
             categories.getParentCid()==entity.getCatId()
                ).map(
                 //该子类目可能还有子类目，需要继续获取子子类目
                (category) -> {
                    category.setChildren(getChildren(category,all));
                    return category;
                }
        ).sorted(
                Comparator.comparingInt(category -> (category.getSort() == null ? 0 : category.getSort()))
        ).collect(Collectors.toList());

        return categoriesList;

    }


    @Override
    public void removeCategory(List<Long> ids) {
        //TODO
        baseMapper.deleteBatchIds(ids);
    }


    @Override
    public Long[] findCatelogPath(Long catelogId) {

        List<Long> path=new ArrayList<>();

        /*
         * 由于我们需要这中形式[父，子，孙]，
         * 而findParentPath(catelogId,path)，返回结果为[孙，子，父]
         * 需要做逆序转换
         */
        List<Long> paths=findParentPath(catelogId,path);
        //逆序转换
        Collections.reverse(paths);
        return (Long[])paths.toArray();
    }


    private List<Long>  findParentPath(Long parentId,List<Long> path){
        path.add(parentId);
        //1。根据id查出该实体
        CategoryEntity entity= baseMapper.selectById(parentId);
        baseMapper.selectById(parentId);
        //2。判断是否有父id
        if(parentId!=0){
            //根据父id，查找父亲的实体
            findParentPath(entity.getParentCid(),path);

        }
        return path;
    }


    @Override
    public List<CategoryEntity> getLevel1Category() {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("parent_cid",0);
        return baseMapper.selectList(queryWrapper);
    }


    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        /**
         * 1。空结果缓存，解决缓存穿透的问题
         * 2。设置过期时间加随机值，解决缓存雪崩问题
         * 3。加锁：解决缓存击穿问题
         */

        //查询缓存数据
        String data= (String) redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(data)){
            //从数据库查询数据
            //Map<String, List<Catalog2Vo>> stringListMap= getCatalogJsonFromDBDb();
            //本地锁
            //Map<String, List<Catalog2Vo>> stringListMap= getCatalogJsonFromDBDbWithLocalLock();
            //分布式锁
            Map<String, List<Catalog2Vo>> stringListMap= getCatalogJsonFromDBDbWithRedisLock();
            //将数据转换成json
            String value= JSON.toJSONString(stringListMap);
            //将数据更新到缓存
            redisTemplate.opsForValue().set("catalogJson",value);
            return stringListMap;
        }

        Map<String, List<Catalog2Vo>> result=JSON.parseObject(data,new TypeReference<Map<String, List<Catalog2Vo>>>(){});
        return result;

    }


    /**
     *
     * 注意：这里有个坑需要注意！
     * （Springboot 2.0以后默认使用lettuce作为操作redis客户端。它使用的是netty进行网络通讯的。
     * lettuce的bug导致netty堆外内存溢出，如果netty没有指定堆外内存，默认使用项目启动是配置的内存。
     * 可以通过-Dio.netty.maxDirectMemory进行设置
     *
     * 解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
     * 1。升级lettuce客户端
     * 2。使用Jedis客户端
     *
     * 先查缓存，缓存没有在查数据库，并将数据更新到缓存
     * @return
     */


    /**
     * 本地锁的实现
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBDbWithLocalLock() {

        /**
         *  加锁解决缓存击穿的问题：
         *  1。本地锁，锁住当前进程，如果该服务部署在6台机器上，就需要每个机器一个锁。
         *  2。分布式锁
         */

        //本地锁，只要是同一把锁，就能锁住，需要这个锁的所有线程
        //synchronized (this)：springboot所有的组件在容器中都是单例的，这里可以写this
        synchronized (this){

            //这边需要先看缓存
            return getDataFromDb();

        }
    }


    /**
     * 分布式锁的实现
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBDbWithRedisLock() {

        /**
         *  加锁解决缓存击穿的问题：
         *  1。本地锁，锁住当前进程，如果该服务部署在6台机器上，就需要每个机器一个锁。
         *  2。分布式锁
         */

        String value=UUID.randomUUID().toString();
        //分布式锁，占分布式锁，去redis占坑
        Boolean bool = redisTemplate.opsForValue().setIfAbsent("lock", value,300,TimeUnit.SECONDS);

        if (bool){
            //加锁成功
            /**
             *  设置过期时间，30s，此时服务宕机，该怎么办？过期时间还没有设置完成（所有必须是原子操作，必须和加锁同步的）
             *
             */
            //redisTemplate.expire("lock",30, TimeUnit.SECONDS);
            try {
                Map<String, List<Catalog2Vo>> data=getDataFromDb();
                return data;

            }finally {
                String lu="if redis.call(‘get’,KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(‘del’,KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                redisTemplate.execute(new DefaultRedisScript<>(lu), Arrays.asList("lock"), value);
            /*String lockVa= (String) redisTemplate.opsForValue().get("lock");

            if(value.equals(lockVa)){
                redisTemplate.delete("lock");
            }*/

            }
            /**
             *   删除锁,可能出现的问题：
             *   1。代码准备删锁时，机器宕机了，锁没有被删除，会造成死锁的问题。
             *   2。getDataFromDb()代码执行时间过长，lock已过期，此时在执行删除操作，会删成别人的锁
             *   解决方案：
             *   1。设置过期时间，即使没有删除，会自动删除
             *   2。将值指定成uuid，在删除前判断一下
             */

            /**
             * 此时还可能存在处理时间过长的问题，刚取到原来的值，还没删除key，key过期了，又有新的key产生，
             * 在执行删除key的操作，此时删除的是别人的key，也需要原子操作
             * 解决方案：1。使用redis脚步删除
             */

        }else {
            //加锁失败，需要重试

            return getCatalogJsonFromDBDbWithRedisLock();//自旋的方式

        }

    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        //这边需要先看缓存
        String data = (String) redisTemplate.opsForValue().get("catalogJson");

        //缓存不为null
        if (StringUtils.isNotEmpty(data)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(data, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }

        //优化，一次性查出数据库所有的数据，有所需要的数据，直接从entityList中获取,减少数据库查询次数
        List<CategoryEntity> entityList = baseMapper.selectList(null);

        //查出所有1级分类
        List<CategoryEntity> list = getParent_cid(entityList, 0L);
        //封装数据
        Map<String, List<Catalog2Vo>> parent_cid = list.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            //每一个一级分类，查找这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(entityList, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(catalog2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, catalog2.getCatId().toString(), catalog2.getName());
                    //查找当前二级分类的三级分类，封装成vo
                    List<CategoryEntity> cata3 = getParent_cid(entityList, catalog2.getCatId());
                    if (cata3 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = cata3.stream().map(catalog3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(catalog2.getCatId().toString(), catalog3.getCatId().toString(), catalog3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        /**
         * 需要在锁里将数据写入缓存，如果不再锁里面将数据写入缓存，
         * 可能数据还没更新到缓存，第二个对象获取锁，在缓存没查到数据，
         * 又去查数据库了。
         */
        String value = JSON.toJSONString(parent_cid);
        //将数据更新到缓存
        redisTemplate.opsForValue().set("catalogJson", value);

        return parent_cid;
    }


    /**
     * 从数据库中查询数据
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBDb() {

            //优化，一次性查出数据库所有的数据，有所需要的数据，直接从entityList中获取,减少数据库查询次数
            List<CategoryEntity> entityList= baseMapper.selectList(null);

            //查出所有1级分类
            List<CategoryEntity> list=getParent_cid(entityList,0L);
            //封装数据
            Map<String, List<Catalog2Vo>> parent_cid=list.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

                //每一个一级分类，查找这个一级分类的二级分类
                List<CategoryEntity>categoryEntities= getParent_cid(entityList,v.getCatId());
                //封装上面的结果
                List<Catalog2Vo> catalog2Vos=null;
                if (categoryEntities != null){
                    catalog2Vos =categoryEntities.stream().map(catalog2 -> {
                        Catalog2Vo catalog2Vo=new Catalog2Vo(v.getCatId().toString(),null,catalog2.getCatId().toString(),catalog2.getName());
                        //查找当前二级分类的三级分类，封装成vo
                        List<CategoryEntity>cata3= getParent_cid(entityList,catalog2.getCatId());
                        if(cata3 !=null){
                            List<Catalog2Vo.Catalog3Vo> catalog3Vos=cata3.stream().map(catalog3 -> {
                                Catalog2Vo.Catalog3Vo catalog3Vo=new Catalog2Vo.Catalog3Vo(catalog2.getCatId().toString(),catalog3.getCatId().toString(),catalog3.getName());
                                return catalog3Vo;
                            }).collect(Collectors.toList());
                            catalog2Vo.setCatalog3List(catalog3Vos);
                        }
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }
                return catalog2Vos;
            }));

            return parent_cid;

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> list,Long parentCid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> entities=list.stream().filter(item -> item.getParentCid()==parentCid).collect(Collectors.toList());
        return entities;
    }
}