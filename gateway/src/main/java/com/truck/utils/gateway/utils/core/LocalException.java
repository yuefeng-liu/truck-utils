package com.truck.utils.gateway.utils.core;

import com.truck.utils.gateway.utils.entity.AbstractReturnCode;

/**
 * Created by truck on 2015/11/30.
 */

/**
 * local 异常
 */
public class LocalException extends Exception {
    private String             data;
    private AbstractReturnCode code;
    /**
     * @param code 错误码
     * @param data 错误数据
     */
    public LocalException(AbstractReturnCode code, String data) {
        this.code = code;
        this.data = data;
    }
    public String getData() {
        return data;
    }
    public AbstractReturnCode getCode() {
        return code;
    }
}