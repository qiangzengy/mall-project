package com.qiangzengy.mall.cart.to;

import lombok.Data;

@Data
public class UserInfoTo {

    //如果登陆，会有用户id
    private Long userId;
    //如果没有登陆，会有临时key
    private String userKey;
    //是否有临时用户
    private boolean tempUser=false;
}
