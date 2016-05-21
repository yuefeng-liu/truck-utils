package com.truck.utils.gateway.utils.document;

import com.truck.utils.gateway.utils.annotation.Description;

@Description("接口实体字段信息")
public class FieldInfo {
    @Description("字段类型")
    public String  type;
    @Description("字段名")
    public String  name;
    @Description("是否为集合类型")
    public boolean isList;
    @Description("注释")
    public String  desc;
}
