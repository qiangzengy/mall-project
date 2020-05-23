package com.qiangzengy.mall.member.vo;

import lombok.Data;

@Data
public class SocialMember {
    private String access_token;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private String isRealName;
}
