package com.truck.utils.gateway.utils.define;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * Created by Rocky on 2015/12/22.
 */
@Description("接口请求状态")
public class Status implements Serializable{

	@Description("状态码")
    public Integer code;
    @Description("响应消息")
    public String message;
    @Description("系统时间")
    public Long sysTime;

    public Status() {
    }

    public Status(Integer code, String message, Long sysTime) {
        this.code = code;
        this.message = message;
        this.sysTime = sysTime;
    }
}
