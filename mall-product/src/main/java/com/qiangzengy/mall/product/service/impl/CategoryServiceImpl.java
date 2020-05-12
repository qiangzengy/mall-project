package com.qiangzengy.mall.product.service.impl;

import com.qiangzengy.common.utils.Query;
import com.qiangzengy.mall.product.entity.vo.Catalog2Vo;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
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

        //优化，一次性查出数据库所有的数据，有所需要的数据，直接从entityList中获取。
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