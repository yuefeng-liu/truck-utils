package com.truck.utils.gateway.utils.entity;

/**
 * Created by truck on 2015/11/30.
 */

public class ApiParameterInfo {
    /**
     * 参数类型
     */
    public Class<?> type;

    /**
     * 用于定义String类型参数取值的枚举类型
     */
    public Class<? extends Enum> verifyEnumType;

    /**
     * 当入参为Collection 或 数组时, 若actuallyGenericType有值, 则值为实际的泛型信息
     */
    public Class<?> actuallyGenericType;

    /**
     * 默认值字符串形式
     */
    public String defaultValue;

    /**
     * 验证字符串表达式
     */
    public String verifyRegex;

    /**
     * 验证错误提示
     */
    public String verifyMsg;

    /**
     * 是否必须
     */
    public boolean isRequired;

    /**
     * 是否加密传输, 用于客户端向服务端发送隐私信息
     */
    public boolean isRsaEncrypted;

    /**
     * 参数名
     */
    public String name;

    /**
     * 名字列表
     */
    public String[] names;

    /**
     * 参数描述
     */
    public String description;

    /**
     * 是否是自动注入参数
     */
    public boolean isAutowired;

    /**
     * 参数的默认值是否需要在常量中定义
     */
    public boolean needDefaultValueConstDefined;
}
