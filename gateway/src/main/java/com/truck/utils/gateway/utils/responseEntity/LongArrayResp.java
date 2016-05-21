package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 14-4-25.
 */
@Description("长整形数组返回值")
public class LongArrayResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Description("长整形数组返回值")
    public long[] value;

    public static LongArrayResp convert(long[] ls) {
        LongArrayResp la = new LongArrayResp();
        la.value = ls;
        return la;
    }
}
