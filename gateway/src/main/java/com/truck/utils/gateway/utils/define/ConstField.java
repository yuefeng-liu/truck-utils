package com.truck.utils.gateway.utils.define;

import java.nio.charset.Charset;

/**
 * Created by truck on 2015/11/30.
 */

public class ConstField {
    public static final Charset UTF8                 = Charset.forName("utf-8");
    public static final Charset ASCII                = Charset.forName("ascii");
    public static final byte[]  XML_START            = "<xml>".getBytes(UTF8);
    public static final byte[]  XML_END              = "</xml>".getBytes(UTF8);
    public static final byte[]  JSON_START           = "{\"stat\":".getBytes(UTF8);
    public static final byte[]  JSON_CONTENT         = ",\"content\":[".getBytes(UTF8);
    public static final byte[]  XML_EMPTY            = "<empty/>".getBytes(ConstField.UTF8);
    public static final byte[]  JSON_SPLIT           = ",".getBytes(UTF8);
    public static final byte[]  JSON_END             = "]}".getBytes(UTF8);
    public static final byte[]  JSON_EMPTY           = "{}".getBytes(UTF8);
    public static final byte[]  JSONP_START          = "(".getBytes(UTF8);
    public static final byte[]  JSONP_END            = ");".getBytes(UTF8);
    public static final String  ERROR_CODE_EXT       = "com.truck.utils.gateway.utils.ERROR_CODE_EXT";
    public static final String  SET_COOKIE_TOKEN     = "com.truck.utils.gateway.utils.SET_COOKIE_TOKEN";
    public static final String  SET_COOKIE_USER_INFO = "com.truck.utils.gateway.utils.SET_COOKIE_USER_INFO";
    public static final String  REDIRECT_TO          = "com.truck.utils.gateway.utils.REDIRECT_TO";
    public static final String  CREDIT               = "com.truck.utils.gateway.utils.CREDIT";
    public static final String  MSG                  = "com.truck.utils.gateway.utils.MSG";
    public static final String  SERVICE_LOG          = "com.truck.utils.gateway.utils.SERVICE_LOG";
}

