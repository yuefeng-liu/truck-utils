package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 14-4-25.
 */
@Description("字符串返回值")
public class StringResp implements Serializable {
    private static final long serialVersionUID = 1L;
    @Description("字符串返回值")
    public String value;

    public static StringResp convert(String s) {
        StringResp sr = new StringResp();
        sr.value = s;
        return sr;
    }
}
