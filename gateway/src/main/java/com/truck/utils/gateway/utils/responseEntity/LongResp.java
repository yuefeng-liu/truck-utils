package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 14-4-25.
 */
@Description("长整形返回值")
public class LongResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Description("长整形返回值")
    public long value;

    public static LongResp convert(long l) {
        LongResp lr = new LongResp();
        lr.value = l;
        return lr;
    }
}
