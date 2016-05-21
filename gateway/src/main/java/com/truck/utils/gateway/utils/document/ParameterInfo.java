package com.truck.utils.gateway.utils.document;

import com.truck.utils.gateway.utils.annotation.Description;

@Description("参数信息")
public class ParameterInfo {
    @Description("参数类型")
    public String  type;
    @Description("默认值(非必选参数)")
    public String  defaultValue;
    @Description("验证规则(正则表达式)")
    public String  verifyRegex;
    @Description("验证失败提示")
    public String  verifyMsg;
    @Description("是否必选参数")
    public boolean isRequired;
    @Description("参数名")
    public String  name;
    @Description("参数描述")
    public String  description;
    @Description("是否是集合或数组类型")
    public boolean isList;
    @Description("是否需要rsa加密")
    public boolean isRsaEncrypt;
}
