package com.truck.utils.gateway.utils.entity;

import com.truck.utils.gateway.utils.define.SecurityType;

import java.io.Serializable;

/**
 * Created by truck on 2015/11/30.
 */

/**
 * 调用者信息，包括设备信息和用户信息(已登录)的一部分
 *
 * @author truck
 */
public class CallerInfo implements Serializable {

    public static final CallerInfo TESTER = new CallerInfo();
    static {
        if (CompileConfig.isDebug) {
            TESTER.deviceId = -1;
            TESTER.uid = 1234567890909L;
            TESTER.expire = Long.MAX_VALUE;
            TESTER.securityLevel = SecurityType.Test.authorize(0);
        }
    }

    // 设备/用户分组
    public String[] groups;

    // 通过动态密码验证的phone number
    public String phoneNumber;

    // 参与token计算
    public int    appid;
    public int    securityLevel;
    public long   expire;
    public long   deviceId;
    public long   uid;
    public byte[] key;                      // 设备身份公钥
}
