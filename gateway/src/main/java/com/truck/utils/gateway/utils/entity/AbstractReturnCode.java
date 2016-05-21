package com.truck.utils.gateway.utils.entity;

import java.io.Serializable;

/**
 * Created by truck on 15/11/28.
 * Add private String service.
 */
public abstract class AbstractReturnCode implements Serializable {

    private        String name;
    private final String desc;
    private final int    code;

    private        String             service;
    private final AbstractReturnCode display;

    /**
     * 初始化一个对外暴露的ReturnCodeSuper(用于客户端异常处理)
     */
    public AbstractReturnCode(String desc, int code) {
        this.desc = desc;
        this.code = code;
        this.display = this;
    }

    /**
     * 初始化一个不对外暴露的ReturnCodeSuper(仅用于服务端数据分析)
     */
    public AbstractReturnCode(int code, AbstractReturnCode shadow) {
        this.desc = null;
        this.code = code;
        this.display = shadow;
    }

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

    public AbstractReturnCode getDisplay() {
        return display;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }

}
