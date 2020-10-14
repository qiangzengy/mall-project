package com.qiangzengy.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.qiangzengy.common.enums.ExceptionCode;
import com.qiangzengy.mall.member.exception.PhoneExistException;
import com.qiangzengy.mall.member.exception.UserNameExistException;
import com.qiangzengy.mall.member.vo.MemBerRegistVo;
import com.qiangzengy.mall.member.vo.MemberLogVo;
import com.qiangzengy.mall.member.vo.SocialMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.qiangzengy.mall.member.entity.MemberEntity;
import com.qiangzengy.mall.member.service.MemberService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.R;



/**
 * 会员
 *
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:46:26
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;


    @PostMapping("/oauth2/login")
    public R authlogin(@RequestBody SocialMember member) throws Exception {
        MemberEntity entity= memberService.login(member);
        if (entity==null){
            return R.error(ExceptionCode.LOGIN_ACCT_PASSWORD_INVAILD_EXCEPTION);
        }
        return R.ok().put("data",entity);
    }

    /**
     * 登陆
     */

    @PostMapping("/login")
    public R login(@RequestBody MemberLogVo logVo){
       MemberEntity entity= memberService.login(logVo);
       if (entity==null){
           return R.error(ExceptionCode.LOGIN_ACCT_PASSWORD_INVAILD_EXCEPTION);
       }
        return R.ok().put("data",entity);
    }

    /**
     * 注册会员
     */
    @PostMapping("/regist")
    public R regist(@RequestBody MemBerRegistVo memBerRegistVo){
        try {
            memberService.regist(memBerRegistVo);

        }catch (PhoneExistException e){
            return R.error(ExceptionCode.MEMBER_PHONE_EXCEPTION);

        }catch (UserNameExistException e){
            return R.error(ExceptionCode.MEMBER_NAME_EXCEPTION);
        }
        return R.ok();

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
