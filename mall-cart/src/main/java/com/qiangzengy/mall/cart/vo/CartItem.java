package com.qiangzengy.mall.cart.vo;


import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车商品属性
 */
public class CartItem {

    //商品id
    private Long skuId;
    //是否选中
    private Boolean chec=true;
    //标题
    private String title;
    //图片
    private String image;
    //商品属性，黑色、64G
    private List<String>skuAttr;
    //价格
    private BigDecimal price;
    //数量
    private Integer count;
    //总价格，动态计算
    private BigDecimal totalPrice;


    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getChec() {
        return chec;
    }

    public void setChec(Boolean chec) {
        this.chec = chec;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIamge() {
        return image;
    }

    public void setIamge(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 计算当前项总价
     * @param
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(""+this.count));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice=totalPrice;
    }
}
