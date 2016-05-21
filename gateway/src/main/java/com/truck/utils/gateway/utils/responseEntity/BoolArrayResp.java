package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 14-4-28.
 */
@Description("布尔类型数组返回值")
public class BoolArrayResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Description("布尔类型数组返回值")
    public boolean[] value;

    public static BoolArrayResp convert(boolean[] bs) {
        BoolArrayResp ba = new BoolArrayResp();
        ba.value = bs;
        return ba;
    }
}
