package com.truck.utils.gateway.utils.document;

import com.truck.utils.gateway.utils.annotation.Description;

@Description("编码信息")
public class CodeInfo {
    @Description("编码值")
    public int     code;
    @Description("编码名称")
    public String  name;
    @Description("编码描述")
    public String  desc;
    @Description("编码所属服务")
    public String  service;
    @Description("是否显示给客户端")
    public boolean isDesign;
}
