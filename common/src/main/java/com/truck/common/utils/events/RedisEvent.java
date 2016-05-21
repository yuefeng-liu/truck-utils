package com.truck.common.utils.events;

import java.util.Map;

/**
 * Created by Rocky on 2016/1/22.
 */
public class RedisEvent {

    public class Channels{
        public static final String COUPON = "coupon";
    }

    public enum Source{

        REGISTERED(1),
        ORDERED(2),
        DELIVERIED(3);

        private Integer value;

        public Integer getValue() {
            return value;
        }

        Source(Integer value){
            this.value = value;
        }
    }

    private Source source;
    private Map<String,Object> data;

    private RedisEvent(){}

    private RedisEvent(Source source,Map<String,Object> data){
        this.source = source;
        this.data = data;
    }

    public static RedisEvent registered(Map<String,Object> data){
        RedisEvent event = new RedisEvent(Source.REGISTERED,data);
        return event;
    }

    public static RedisEvent ordered(Map<String,Object> data){
        RedisEvent event = new RedisEvent(Source.ORDERED,data);
        return event;
    }

    public static RedisEvent deliveried(Map<String,Object> data){
        return new RedisEvent(Source.DELIVERIED,data);
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
