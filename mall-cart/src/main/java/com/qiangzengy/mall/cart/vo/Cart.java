package com.qiangzengy.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车内容
 * 需要计算的属性，必须重新get方法
 */
@Data
public class Cart {

    private List<CartItem> items;
    //商品数量
    private Integer countNum;
    //商品类型数量
    private Integer countType;
    //商品总价
    private BigDecimal totalAmount;
    //减免价格
    private BigDecimal reduce=new BigDecimal("0.00");


    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count=0;
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=1;
            }
        }
        return count;
    }


    public Integer getCountType() {

        return countType;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount=new BigDecimal("0");
        if (items!=null && items.size()>0){
            //计算商品的总价
            for (CartItem item : items) {
                if (item.getChec()){
                    amount=amount.add(item.getTotalPrice());
                }
            }
            //减去优惠的价格
            amount.subtract(getReduce());
        }
        return amount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
