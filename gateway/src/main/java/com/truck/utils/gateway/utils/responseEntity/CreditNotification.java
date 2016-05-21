package com.truck.utils.gateway.utils.responseEntity;

import com.truck.utils.gateway.utils.annotation.Description;

import java.io.Serializable;

/**
 * @author guankaiqiang@jk.cn
 */
@Description("积分通知")
public class CreditNotification implements Serializable {
    @Description("描述,为何送积分")
    public String description;
    @Description("积分值")
    public long   credit;
    @Description("提示,送了多少积分")
    public String notification;
}