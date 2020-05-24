package com.qiangzengy.common.exception;

import lombok.Getter;
import lombok.Setter;

public class NoStockException extends RuntimeException {

    @Getter
    @Setter
    public Long skuId;
    public NoStockException(Long skuId) {
        super("商品id:"+skuId+"没有足够的库存");
    }
    public NoStockException() {
        super("没有足够的库存");
    }
}
