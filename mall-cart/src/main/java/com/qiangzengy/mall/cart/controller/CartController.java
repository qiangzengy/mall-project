package com.qiangzengy.mall.cart.controller;

import com.qiangzengy.mall.cart.service.CartService;
import com.qiangzengy.mall.cart.vo.Cart;
import com.qiangzengy.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * RedirectAttributes redire
 * redire.addFlashAttribute() 将数据放在session里面，可以在页面取出，只能取一次
 * redire.addAttribute() 将数据放在url后面
 *
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     *  没登陆获取购物车临时数据
     *  分析：
     *  1。没登陆，第一次使用购物车，会自动分配一个user-key作为临时用户标识，
     *  浏览器自己保存，每次访问都会带，过期时间为一个月。
     *
     *  登陆：session有
     *  没登陆：按照cookie里面带来的user-key来做
     *  第一次没有临时用户，需要帮忙创建一个临时用户
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //快速得到用户信息，可以使用ThreadLocal
        /**
         * ThreadLocal：在同一个线程共享数据
         */
        //UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart=cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redire) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);
        redire.addAttribute("skuId",skuId);
        //需要重定向到success页，避免刷新重复提交的问题
        return "redirect:http://cart.gulimall.com/cartSuccess.html";

    }

    /**
     * 跳转到成功页
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/cartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId,Model model){
        CartItem item=cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        return "success";
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/check")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check ") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";

    }


    /**
     * 改变购物项数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/count")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num ") Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";

    }

    /**
     * 删除购物车
     * @param skuId
     * @return
     */
    @GetMapping("/delete")
    public String deleteCart(@RequestParam("skuId") Long skuId){
        cartService.clearCart(skuId.toString());
        return "redirect:http://cart.gulimall.com/cart.html";

    }




}
