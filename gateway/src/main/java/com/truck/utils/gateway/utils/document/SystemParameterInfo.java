package com.truck.utils.gateway.utils.document;

import com.truck.utils.gateway.utils.annotation.Description;

@Description("系统级参数")
public class SystemParameterInfo {
    @Description("参数名称")
    public String name;
    @Description("描述")
    public String desc;
}
