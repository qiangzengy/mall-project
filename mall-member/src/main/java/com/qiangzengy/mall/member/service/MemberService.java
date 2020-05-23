package com.qiangzengy.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.member.entity.MemberEntity;
import com.qiangzengy.mall.member.exception.PhoneExistException;
import com.qiangzengy.mall.member.exception.UserNameExistException;
import com.qiangzengy.mall.member.vo.MemBerRegistVo;
import com.qiangzengy.mall.member.vo.MemberLogVo;
import com.qiangzengy.mall.member.vo.SocialMember;

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

    void regist(MemBerRegistVo memBerRegistVo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkNameUnique(String userName) throws UserNameExistException;


    MemberEntity login(MemberLogVo logVo);

    MemberEntity login(SocialMember member) throws Exception;

}

