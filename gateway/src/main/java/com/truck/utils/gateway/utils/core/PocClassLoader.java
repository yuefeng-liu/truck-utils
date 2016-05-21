package com.truck.utils.gateway.utils.core;

/**
 * Created by truck on 2015/11/30.
 */
public class PocClassLoader extends ClassLoader {
    public PocClassLoader() {
        super();
    }

    public PocClassLoader(ClassLoader cl) {
        super(cl);
    }

    //    @Override
    //    protected Class<?> findClass(String name) throws java.lang.ClassNotFoundException {
    //        return Thread.currentThread().getContextClassLoader().loadClass(name);
    //    }

    public Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}

