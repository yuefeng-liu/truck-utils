package com.truck.utils.gateway.utils.define;

/**
 * Created by truck on 2015/11/30.
 */

public interface Evaluater<TLeft, TRight> {
    void evaluate(TLeft left, TRight right);
}

