package com.truck.utils.gateway.utils.document;

import com.truck.utils.gateway.utils.annotation.Description;

import java.util.List;


/**
 * Created by truck on 2015/11/30.
 */
@Description("接口文档")
public class Document {
    @Description("应用接口信息")
    public List<MethodInfo>          apiList;
    @Description("通用异常信息")
    public List<CodeInfo>            codeList;
    @Description("通用返回值结构描述")
    public List<RespStruct>          respStructList;
    @Description("系统级参数列表描述")
    public List<SystemParameterInfo> systemParameterInfoList;
}
