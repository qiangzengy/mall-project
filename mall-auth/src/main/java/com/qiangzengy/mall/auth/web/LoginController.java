package com.qiangzengy.mall.auth.web;

import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.constant.AuthConstant;
import com.qiangzengy.common.enums.ExceptionCode;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.common.vo.MemberRespVo;
import com.qiangzengy.mall.auth.feign.MemberFeignService;
import com.qiangzengy.mall.auth.feign.ThirdPartyFeignService;
import com.qiangzengy.mall.auth.utils.RandomUtil;
import com.qiangzengy.mall.auth.vo.UserLogVo;
import com.qiangzengy.mall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sms")
public class LoginController {

    @Autowired
    private ThirdPartyFeignService partyFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;


    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        //防止同一个phone60s内再次发送验证码
        String redisV = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisV)) {
            String[] s = redisV.split("_");
            long times = Long.parseLong(s[1]);
            if (System.currentTimeMillis() - times < 60000) {
                return R.error(ExceptionCode.VAILD_SMS_CODE_EXCEPTION.getCode(), ExceptionCode.VAILD_SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //验证码校验
        String codeNew = RandomUtil.getFourBitRandom() + "_" + System.currentTimeMillis();
        //缓存验证码，方便下次校验
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, codeNew, 10, TimeUnit.MINUTES);
        String code = codeNew.split("_")[0];
        partyFeignService.sendMsm(phone, code);
        return R.ok();

    }

    /**
     * @Valid 进行数据校验
     * 检验结果封装在BindingResult中
     * <p>
     * RedirectAttributes 模拟重定向携带数据的
     * <p>
     * 重定向携带数据，利用session原理，将数据放到session中，
     * 只要跳到下一个页面取出这个数据后，session里面的数据就会
     * 删除。（需解决分布式session问题)
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo registVo, BindingResult result,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {

            //result.getFieldErrors() 获取所有属性的错误信息
            Map<String, Object> collect = result.getFieldErrors().stream().collect(Collectors.toMap(
                    FieldError::getField,
                    DefaultMessageSourceResolvable::getDefaultMessage));
            attributes.addFlashAttribute("errors", collect);
            //model.addAttribute("errors",collect);

            //校验出错，转发到注册页
            return "redirect://http://auth.gulimall.com/reg.html";
        }

        //校验验证码
        String code = registVo.getCode();
        String value = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + registVo.getPhone());
        if (StringUtils.isNotEmpty(value)) {

            if (code.equals(value.split("_")[0])) {
                //删除验证码
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + registVo.getPhone());
                //调用远程服务进行注册
                R r = memberFeignService.regist(registVo);
                if (r.getCode() == 0) {
                    //成功
                    return "redirect://http://auth.gulimall.com/login.html";
                } else {
                    //失败
                    Map<String, String> map = new HashMap<>();
                    map.put("errors", r.getData("msg", new TypeReference<String>() {
                    }));
                    attributes.addFlashAttribute("errors", map);
                    return "redirect://http://auth.gulimall.com/reg.html";
                }

            } else {
                Map<String, String> map = new HashMap<>();
                map.put("errors", "验证码错误");
                attributes.addFlashAttribute("errors", map);
                //校验出错，转发到注册页
                return "redirect://http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> map = new HashMap<>();
            map.put("errors", "验证码错误");
            attributes.addFlashAttribute("errors", map);
            //校验出错，转发到注册页
            return "redirect://http://auth.gulimall.com/reg.html";
        }

    }


    @PostMapping("/login")
    public String login(UserLogVo logVo, RedirectAttributes attributes, HttpSession session) {
        R r = memberFeignService.login(logVo);
        if (r.getCode() == 0) {
            MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER, data);
            return "redirect://gulimall.com";
        } else {
            Map<String, String> map = new HashMap<>();
            map.put("errors", r.getData("msg", new TypeReference<String>() {
            }));
            attributes.addFlashAttribute("errors", map);
            return "redirect://http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String logPage(HttpSession session) {
        //只要没登陆过，才来这里
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        }
        return "redirect://http://auth.gulimall.com";
    }

}
