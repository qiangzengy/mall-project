package com.qiangzengy.common.to.mq;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockDetailTo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 购买个数
	 */
	private Integer skuNum;
	/**
	 * 工作单id
	 */
	private Long taskId;

	private Long wareId;

	private Integer lock_status;

}
