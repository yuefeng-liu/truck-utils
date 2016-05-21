package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

@Description("返回json格式的string")
public class JSONString implements Serializable {
    @Description("json string")
    public String value;
}