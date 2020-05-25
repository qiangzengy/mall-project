package com.qiangzengy.common.to.mq;

import lombok.Data;

@Data
public class StockLockTo {

    //库存工作单的id
    private Long id;

    //工作单详情的id
    private StockDetailTo detail;

}
