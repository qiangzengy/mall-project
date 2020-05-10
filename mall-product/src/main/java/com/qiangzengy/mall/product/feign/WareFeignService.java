package com.qiangzengy.mall.product.feign;

import com.qiangzengy.common.to.es.SkuHasStockVo;
import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareFeignService {

     @PostMapping("/ware/waresku/hasStock")
     R<List<SkuHasStockVo>> getSkuHasStock(@RequestBody List<Long> skuIds);
}
