package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by truck on 2015/11/30.
 */


@Description("键值对")
public class KeyValuePair implements Serializable {
    private static final long serialVersionUID = 1L;
    @Description("键")
    public String key;
    @Description("值")
    public String value;

    public KeyValuePair() {}

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
