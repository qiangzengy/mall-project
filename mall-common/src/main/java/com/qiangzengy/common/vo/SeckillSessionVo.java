package com.qiangzengy.common.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
@Data
public class SeckillSessionVo {

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 活动关联的商品
     */
    private List<SeckillSkuRelationVo> seckillSkuRelationEntities;
}
