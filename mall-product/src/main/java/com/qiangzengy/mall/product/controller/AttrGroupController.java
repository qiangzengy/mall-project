package com.qiangzengy.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.qiangzengy.mall.product.entity.AttrEntity;
import com.qiangzengy.mall.product.entity.vo.AttrGroupRelationVo;
import com.qiangzengy.mall.product.service.AttrService;
import com.qiangzengy.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.qiangzengy.mall.product.entity.AttrGroupEntity;
import com.qiangzengy.mall.product.service.AttrGroupService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.R;



/**
 * 属性分组
 *
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;


    @Autowired
    private CategoryService categoryService;


    @Autowired
    private AttrService attrService;


    ///product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entities =  attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entities);
    }


    /**
     * 列表
     */
    @RequestMapping(value = "/list/{catelogId}",method = RequestMethod.GET)
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId")Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.querycatelogIdPage(params,catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping(value = "/info/{attrGroupId}",method = RequestMethod.GET)
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){

        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        //[父,子,孙]
        Long[] path = categoryService.findCatelogPath(catelogId);

        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);


    }

    /**
     * 保存
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除属性与分组的关联关系
     * /product/attrgroup/attr/relation/delete
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrService.deleteRelation(vos);

        return R.ok();
    }


    /**
     * 获取属性分组没有关联的其他属性
     * /product/attrgroup/{attrgroupId}/noattr/relation
     * @param attrgroupId
     * @param params
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }

}
