package com.qiangzengy.mall.product.service.impl;

import com.qiangzengy.common.utils.Query;
import com.qiangzengy.mall.product.entity.AttrEntity;
import com.qiangzengy.mall.product.entity.vo.AttrGroupWithAttrsVo;
import com.qiangzengy.mall.product.entity.vo.SpuItemAttrGroupVo;
import com.qiangzengy.mall.product.service.AttrService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;

import com.qiangzengy.mall.product.dao.AttrGroupDao;
import com.qiangzengy.mall.product.entity.AttrGroupEntity;
import com.qiangzengy.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public PageUtils querycatelogIdPage(Map<String, Object> params,Long catelogId) {

        //判断catelogId是否为null
        if (catelogId==null){
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    new QueryWrapper<AttrGroupEntity>()
            );

            return new PageUtils(page);
        }else {
            //sql的实现
            //select * from pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like %key%)
            //获取参数中key的值
            String key=params.get("key").toString();
            QueryWrapper<AttrGroupEntity>queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("catelog_id",catelogId);
            if(StringUtils.isNotEmpty(key)){
                queryWrapper.and((obj) -> {
                    obj.eq("attr_group_id",key).or().like("attr_group_name",key);
                });
            }

            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);

        }

    }


    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //com.atguigu.gulimall.product.vo
        //1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2、查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group,attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        List<SpuItemAttrGroupVo> groupVos=this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);

        return null;
    }
}