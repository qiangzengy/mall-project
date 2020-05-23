package com.qiangzengy.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.qiangzengy.common.utils.HttpUtils;
import com.qiangzengy.mall.member.entity.MemberLevelEntity;
import com.qiangzengy.mall.member.exception.PhoneExistException;
import com.qiangzengy.mall.member.exception.UserNameExistException;
import com.qiangzengy.mall.member.service.MemberLevelService;
import com.qiangzengy.mall.member.vo.MemBerRegistVo;
import com.qiangzengy.mall.member.vo.MemberLogVo;
import com.qiangzengy.mall.member.vo.SocialMember;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.Query;

import com.qiangzengy.mall.member.dao.MemberDao;
import com.qiangzengy.mall.member.entity.MemberEntity;
import com.qiangzengy.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void regist(MemBerRegistVo memBerRegistVo) {
        MemberEntity entity = new MemberEntity();
        //获取默认的会员等级
        MemberLevelEntity levelEntity = memberLevelService.getOne((Wrapper<MemberLevelEntity>) new QueryWrapper().eq("default_status", 1));
        entity.setLevelId(levelEntity.getId());

        //检查手机号是否唯一
        checkPhoneUnique(memBerRegistVo.getPhone());
        entity.setMobile(memBerRegistVo.getPhone());
        //检查用户名是否唯一
        checkNameUnique(memBerRegistVo.getUserName());
        entity.setUsername(memBerRegistVo.getUserName());
        //密码需要进行加密存储
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        String encode = encoder.encode(memBerRegistVo.getPassword());
        entity.setPassword(encode);
        baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer mobile = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(mobile>0){
            throw new PhoneExistException();
        }

    }

    @Override
    public void checkNameUnique(String userName) throws UserNameExistException{
        Integer mobile = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(mobile>0){
            throw new UserNameExistException();
        }

    }

    @Override
    public MemberEntity login(MemberLogVo logVo) {
        String loginacct=logVo.getLoginacct();
        String password=logVo.getPassword();
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity ==null){
            return null;
        }
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, entity.getPassword());
        if (matches){
            return entity;
        }
        return null;
    }


    /**
     * 社交账号登陆/注册
     * @param member
     * @return
     */
    @Override
    public MemberEntity login(SocialMember member) throws Exception {
        String uid=member.getUid();
        //判断当前用户是否登陆过
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (entity!=null){
            //该用户已注册
            MemberEntity memberEntity=new MemberEntity();
            memberEntity.setId(entity.getId());
            memberEntity.setSocialUid(uid);
            memberEntity.setAccessToken(member.getAccess_token());
            memberEntity.setExposeIn(member.getExpires_in().toString());
            baseMapper.updateById(memberEntity);
            entity.setSocialUid(uid);
            entity.setAccessToken(member.getAccess_token());
            entity.setExposeIn(member.getExpires_in().toString());
            return entity;
        }else {
            //需要注册
            MemberEntity mentity=new MemberEntity();
            mentity.setSocialUid(uid);
            mentity.setAccessToken(member.getAccess_token());
            mentity.setExposeIn(member.getExpires_in().toString());
            //查询社交用户的账号信息
            try{
                Map<String,String>map=new HashMap<>();
                HttpResponse response = HttpUtils.doGet("http://api.weibo.com", "/2/user/show.json",
                        "get", null, map);
                if (response.getStatusLine().getStatusCode()==200){
                    String json= EntityUtils.toString(response.getEntity());
                    JSONObject object= JSON.parseObject(json);
                    String name = object.getString("name");
                    String gender = object.getString("gender");
                    mentity.setNickname(name);
                    mentity.setGender("m".equals(gender)?1:0);
                }

            }catch (Exception e){

            }
            return mentity;

        }

    }
}