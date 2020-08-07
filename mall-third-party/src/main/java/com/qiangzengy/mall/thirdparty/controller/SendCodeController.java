package com.qiangzengy.mall.thirdparty.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.qiangzengy.common.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.aliyuncs.CommonRequest;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sms")
public class SendCodeController {

    @Value("${aliyun.oss.access-id}")
    private String accessId;
    @Value("${aliyun.oss.access-key}")
    private String accessKey;

    @GetMapping("/send")
    public R sendMsm(@RequestParam("phone") String phone,@RequestParam("code") String code) {
        Map<String,Object> param = new HashMap<>();
        param.put("code",code);
        //调用service发送短信的方法
        send(param,phone);
        return R.ok();
    }


    private boolean send(Map<String, Object> param, String phone) {
        DefaultProfile profile =
                DefaultProfile.getProfile("default", accessId,accessKey);
        IAcsClient client = new DefaultAcsClient(profile);
        //设置相关固定的参数
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        //设置发送相关的参数
        request.putQueryParameter("PhoneNumbers",phone); //手机号
        request.putQueryParameter("SignName","爱嘉昕"); //申请阿里云 签名名称
        request.putQueryParameter("TemplateCode","SMS_187540589"); //申请阿里云 模板code
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param)); //验证码数据，转换json数据传递

        try {
            //最终发送
            CommonResponse response = client.getCommonResponse(request);
            boolean success = response.getHttpResponse().isSuccess();
            return success;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
