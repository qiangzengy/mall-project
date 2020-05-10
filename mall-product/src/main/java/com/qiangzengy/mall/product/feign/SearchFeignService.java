package com.qiangzengy.mall.product.feign;

import com.qiangzengy.common.to.es.SkuEsModel;
import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-es")
public interface SearchFeignService {

    @RequestMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
