package com.qiangzengy.mall.cart.service;

import com.qiangzengy.mall.cart.vo.Cart;
import com.qiangzengy.mall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

public interface CartService {


    /**
     * 将商品添加到购物车
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;


    /**
     * 获取购物项
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取购物车数据
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     * @param key
     */
    void clearCart(String key);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);
}
