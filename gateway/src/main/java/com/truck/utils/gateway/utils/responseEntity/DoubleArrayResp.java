package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 14-5-2.
 */
@Description("浮点形数组返回值")
public class DoubleArrayResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Description("浮点形数组返回值")
    public double[] value;

    public static DoubleArrayResp convert(double[] ds) {
        DoubleArrayResp da = new DoubleArrayResp();
        da.value = ds;
        return da;
    }

    public static DoubleArrayResp convert(float[] fs) {
        DoubleArrayResp da = new DoubleArrayResp();
        da.value = new double[fs.length];
        for (int i = 0; i < fs.length; i++) {
            da.value[i] = fs[i];
        }
        return da;
    }
}