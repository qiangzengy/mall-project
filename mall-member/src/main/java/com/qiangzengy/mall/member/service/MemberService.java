package com.qiangzengy.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:46:26
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

