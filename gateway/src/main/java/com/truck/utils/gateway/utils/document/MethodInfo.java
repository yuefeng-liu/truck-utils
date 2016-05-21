package com.truck.utils.gateway.utils.document;

import com.truck.utils.gateway.utils.annotation.Description;

import java.util.List;

@Description("接口信息")
public class MethodInfo {
    @Description("返回值类型")
    public String              returnType;
    @Description("接口名")
    public String              methodName;
    @Description("接口简介")
    public String              description;
    @Description("接口详细信息")
    public String              detail;
    @Description("调用接口所需安全级别")
    public String              securityLevel;
    @Description("接口分组名")
    public String              groupName;
    @Description("接口可用状态")
    public String              state;
    @Description("接口返回值类型结构描述")
    public List<RespStruct>    respStructList;
    @Description("接口参数列表信息")
    public List<ParameterInfo> parameterInfoList;
    @Description("接口返回值类型结构描述")
    public List<ReqStruct>     reqStructList;
    @Description("接口返回业务异常列表")
    public List<CodeInfo>      errorCodeList;
    @Description("接口组负责人")
    public String              groupOwner;
    @Description("接口负责人")
    public String              methodOwner;
    @Deprecated
    @Description("允许访问的第三方编号列表")
    public int[]               allowThirdPartyIds;
    @Description("是否只允许通过加密通道访问")
    public boolean             encryptionOnly;
    @Description("Integared级别接口是否需要网关对请求进行签名验证")
    public boolean             needVerify;
}
