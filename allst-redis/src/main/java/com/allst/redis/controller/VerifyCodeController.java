package com.allst.redis.controller;

import com.allst.redis.utils.VerifyCode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author June
 * @since 2021年07月
 */
@RestController
@RequestMapping("/vc")
public class VerifyCodeController {

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    @RequestMapping("/getCode")
    public String getCode() {
        return VerifyCode.getRandomNumCode(6);
    }

    /**
     * 获取当前手机号的验证码
     *
     * @param phone 手机号
     * @return 验证码
     */
    @RequestMapping("/getPhoneCode/{phone}")
    public String getPhoneCode(@PathVariable("phone") String phone) {
        return VerifyCode.getPhoneCode(phone);
    }

    /**
     * 验证手机校验码
     *
     * @param phone 手机号
     * @return 结果
     */
    @RequestMapping("/verifyPhoneCode/{phone}/{code}")
    public String verifyPhoneCode(@PathVariable("phone") String phone, @PathVariable("code") String code) {
        return VerifyCode.getRedisCode(phone, code);
    }
}
