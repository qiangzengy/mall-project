package com.qiangzengy.mall.product.service.impl;

import com.qiangzengy.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        //2.1找到所有的一级分类，

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
}