package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 14-5-2.
 */
@Description("浮点形返回值")
public class DoubleResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Description("浮点形返回值")
    public double value;

    public static DoubleResp convert(double d) {
        DoubleResp dr = new DoubleResp();
        dr.value = d;
        return dr;
    }

    public static DoubleResp convert(float d) {
        DoubleResp dr = new DoubleResp();
        dr.value = d;
        return dr;
    }
}