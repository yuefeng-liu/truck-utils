package com.truck.utils.gateway.utils.define;

/**
 * Created by truck on 15/11/28.
 */

/**
 * 所有 mock service implementation 的类都需要实现本接口
 * 注入的参数为指向真实服务的代理
 */
public interface MockApiImplementation<T> {
    void $setProxy(T proxy);
}
