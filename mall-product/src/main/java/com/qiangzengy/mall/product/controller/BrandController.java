package com.qiangzengy.mall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.qiangzengy.common.valid.AddGroup;
import com.qiangzengy.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.qiangzengy.mall.product.entity.BrandEntity;
import com.qiangzengy.mall.product.service.BrandService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping(value = "/info/{brandId}",method = RequestMethod.POST)
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * 开启校验功能@Valid
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){
		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		//brandService.updateById(brand);
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
