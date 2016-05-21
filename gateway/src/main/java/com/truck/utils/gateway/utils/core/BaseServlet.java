package com.truck.utils.gateway.utils.core;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.RpcContext;
import com.truck.utils.gateway.utils.define.*;
import com.truck.utils.gateway.utils.document.CallState;
import com.truck.utils.gateway.utils.document.Response;
import com.truck.utils.gateway.utils.dubboext.DubboExtProperty;
import com.truck.utils.gateway.utils.entity.*;
import com.truck.utils.gateway.utils.responseEntity.KeyValuePair;
import com.truck.utils.gateway.utils.responseEntity.RawString;
import com.truck.utils.gateway.utils.util.MiscUtil;
import com.truck.utils.gateway.utils.util.POJOSerializerProvider;
import org.apache.http.HttpHeaders;
import org.slf4j.*;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * api入口servlet的基础类实现，封装了基本的业务流程和参数解析过程，将签名验证，api解析，序列化输出等工作的具体实现交由子类处理。
 *
 * @author truck
 */
public abstract class BaseServlet extends HttpServlet {
    private static final   long                 serialVersionUID         = 1L;
    private static final   Logger logger                     = LoggerFactory.getLogger(BaseServlet.class);
    protected static final Marker SERVLET_MARKER            = MarkerFactory.getMarker("servlet");
    //debug 模式下识别http header中dubbo.version参数,将请求路由到指定的dubbo服务上
    public static final    String               DEBUG_DUBBOVERSION        = "DUBBO-VERSION";
    //debug 模式下识别http header中dubbo.service.ip参数,将请求路由到指定的dubbo服务上
    public static final    String               DEBUG_DUBBOSERVICE_URL   = "DUBBO-SERVICE-URL";
    protected static final ApiMethodCall[]      EMPTY_METHOD_CALL_ARRAY  = new ApiMethodCall[0];
    private static final   String               HEADER_ORGIN               = "Access-Control-Allow-Origin";
    private static final   String               HEADER_METHOD              = "Access-Control-Allow-Method";
    private static final   String               HEADER_CREDENTIALS        = "Access-Control-Allow-Credentials";
    private static final   String               HEADER_METHOD_VALUE       = "POST, GET, OPTIONS, PUT, DELETE, HEAD";
    private static final   String               HEADER_CREDENTIALS_VALUE = "true";
    public static final    String               HTTPMETHOD_POST            = "POST";
    public static final    String               FORMAT_XML                  = "xml";
    public static final    String               FORMAT_JSON                 = "json";
    public static final    String               FORMAT_PLAINTEXT           = "plaintext";
    private static final   String               SERVER_ADDRESS             = "a:";
    private static final   String               THREADID                    = "t:";
    private static final   String               SPLIT                       = "|";
    private static final   String               REQ_TAG                     = "s:";
    private static final   String               CONTENT_TYPE_XML          = "application/xml; charset=utf-8";
    private static final   String               CONTENT_TYPE_JSON         = "application/json; charset=utf-8";
    private static final   String               CONTENT_TYPE_PLAINTEXT   = "text/plain";
    private static final   String               JSONARRAY_PREFIX          = "[";
    private static final   String               JSONARRAY_SURFIX          = "]";
    private static final Serializer<Response> apiResponseSerializer    = POJOSerializerProvider.getSerializer(Response.class);

    private ApiManager apiManager;

    public BaseServlet(ApiManager apiManager) {
        this.apiManager = apiManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (apiManager == null) {
            return;
        }
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (apiManager == null) {
            return;
        }
        processRequest(request, response);
    }

    /**
     * 设置应答 header
     * @param request
     * @param response
     * @param context
     */
    private void setResponseHeader(HttpServletRequest request, HttpServletResponse response, ApiContext context) {
        //解决H5跨域问题
        {
            String origin = request.getHeader("Origin");
            if (origin != null && CommonConfig.getInstance().getOriginWhiteList().containsKey(origin)) {
                response.setHeader(HEADER_ORGIN, origin);
                response.addHeader(HEADER_METHOD, HEADER_METHOD_VALUE);
                response.setHeader(HEADER_CREDENTIALS, HEADER_CREDENTIALS_VALUE);
            }
        }

        // 设置response 的content type
        {
            switch (context.format) {
                case JSON:
                    response.setContentType(CONTENT_TYPE_JSON);
                    break;
                case XML:
                    response.setContentType(CONTENT_TYPE_XML);
                    break;
                case PLAINTEXT:
                    response.setContentType(CONTENT_TYPE_PLAINTEXT);
                    break;
            }
        }

        {
            if (context.deviceIdStr != null && context.deviceIdStr.length() > 0) {
                try {
                    long did = Long.parseLong(context.deviceIdStr);
                    context.deviceId = did;

                    // user token 解析失败，删除 user token
                    if (did < 0 && context.token != null && context.caller == null) {
                        context.clearUserToken = true;
                    }
                } catch (Exception e) {
                    logger.error(SERVLET_MARKER, "deviceId error " + context.deviceIdStr, e);
                    setDeviceIDinCookie(context, response, request);
                }
            } else {
                setDeviceIDinCookie(context, response, request);
            }
        }
    }

    private void setDeviceIDinCookie(ApiContext context, HttpServletResponse response,HttpServletRequest request) {
        //随机生成一个设备id
        context.deviceId = -(100000000000000L + ((long)(Math.random() * 900000000000000L)));
        context.deviceIdStr = String.valueOf(context.deviceId);
        MDC.put(CommonParameter.deviceId, context.deviceIdStr);

        //添加设备cookie
        HashMap<String, String> map = CommonConfig.getInstance().getOriginWhiteList();
        Cookie deviceId_cookie = new Cookie(CommonParameter.cookieDeviceId, context.deviceIdStr);
        deviceId_cookie.setMaxAge(Integer.MAX_VALUE);
        deviceId_cookie.setSecure(false);
        deviceId_cookie.setPath("/");
        if (context.host != null && map.containsKey(context.host)) {
            deviceId_cookie.setDomain(map.get(context.host));
        }
        response.addCookie(deviceId_cookie);

        if (StringUtils.isEmpty(request.getParameter(ExtendParameter.idfa))){
            context.idfa = context.deviceIdStr;
            String idfa_key = context.appid + ExtendParameter.idfa;
            Cookie idfa_cookie = new Cookie(idfa_key,context.deviceIdStr);
            idfa_cookie.setMaxAge(Integer.MAX_VALUE);
            idfa_cookie.setSecure(false);
            idfa_cookie.setPath("/");
            if (context.host != null && map.containsKey(context.host)) {
                idfa_cookie.setDomain(map.get(context.host));
            }
            idfa_cookie.setDomain(".lyfai521.top");
            response.addCookie(idfa_cookie);
        }
    }

    /**
     * 执行web请求
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        boolean fatalError = false;
        AbstractReturnCode parseResult = null;
        //生成一个ApiContext
        ApiContext apiContext = ApiContext.getCurrent();
        long current = System.currentTimeMillis();
        try {
            apiContext.clear();
            apiContext.startTime = current;
            parseParameter(apiContext, request, response);
            setResponseHeader(request, response, apiContext);
            parseResult = parseMethodInfo(apiContext, request);
            // 验证token是否过期, 只对安全级别为 UserLogin 的访问进行此种检查
            if (parseResult == ApiReturnCode.SUCCESS && apiContext.caller != null && SecurityType.UserLogin.check(apiContext.requiredSecurity)) {
                if (apiContext.caller.expire < current) {
                    parseResult = ApiReturnCode.TOKEN_EXPIRE;
                    apiContext.clearUserTokenFlag = true;
                }
            }
        } catch (Exception e) {
            logger.error(SERVLET_MARKER, "init request failed.", e);
            fatalError = true;
        }
        try {
            AccessLogger access = CommonConfig.getInstance().getAccessLogger();
            // 参数解析失败
            if (fatalError) {
                access.logRequest("with fatal error", String.valueOf(ApiReturnCode.FATAL_ERROR.getCode()));
            } else if (parseResult != ApiReturnCode.SUCCESS) {
                access.logRequest("with error", String.valueOf(parseResult.getCode()));
            } else { // 参数解析成功
                access.logRequest();
                int count = 0;
                for (ApiMethodCall call : apiContext.apiCallInfos) {
                    apiContext.currentCall = call;
                    MDC.put(CommonParameter.method, call.method.methodName);
                    call.startTime = (count++ == 0) ? apiContext.startTime : System.currentTimeMillis();
                    executeApiCall(call, request, response);
                    call.costTime = (int)(System.currentTimeMillis() - call.startTime);
                    serializeCallResult(apiContext, call);
                    //access log
                    access.logAccess(call.costTime, call.method.methodName, call.getReturnCode(), call.getOriginCode(),
                            call.resultLen, call.message.toString(), call.serviceLog == null ? "" : call.serviceLog);
                    MDC.remove(CommonParameter.method);
                }
            }
        } catch (SerializeException se) {
            logger.error(SERVLET_MARKER, "output failed.", se.getException());
            fatalError = true;
        } catch (Throwable t) {
            logger.error(SERVLET_MARKER, "api execute error.", t);
            fatalError = true;
        } finally {
            try {
                // token 解析失败，删除 token 以及标志位
                if (apiContext.clearUserToken) {
                    HashMap<String, String> map = CommonConfig.getInstance().getOriginWhiteList();
                    // 删除 cookie
                    Cookie tk_cookie = new Cookie(apiContext.appid + CommonParameter.token, "");
                    tk_cookie.setMaxAge(0);
                    tk_cookie.setHttpOnly(true);
                    tk_cookie.setSecure(false);
                    tk_cookie.setPath("/");

                    // 删除 cookie 标志位
                    Cookie ct_cookie = new Cookie(apiContext.appid + "_ct", "");
                    ct_cookie.setMaxAge(0);
                    ct_cookie.setHttpOnly(false);
                    ct_cookie.setSecure(false);
                    ct_cookie.setPath("/");

                    // 删除 用户信息
                    Cookie userInfo_cookie = new Cookie(apiContext.appid + "_uinfo", "");
                    userInfo_cookie.setMaxAge(0);
                    userInfo_cookie.setHttpOnly(false);
                    userInfo_cookie.setSecure(false);
                    userInfo_cookie.setPath("/");
                    if (apiContext.host != null && map.containsKey(apiContext.host)) {
                        String domain = map.get(apiContext.host);
                        tk_cookie.setDomain(domain);
                        ct_cookie.setDomain(domain);
                        userInfo_cookie.setDomain(domain);
                    }
                    response.addCookie(tk_cookie);
                    response.addCookie(ct_cookie);
                    response.addCookie(userInfo_cookie);
                } else if (apiContext.caller != null && apiContext.caller.deviceId < 0 && apiContext.clearUserTokenFlag) {
                    // token 过期，删除标志位，将客户端 token 标记为失效
                    HashMap<String, String> map = CommonConfig.getInstance().getOriginWhiteList();
                    // 删除 cookie 标志位
                    Cookie ct_cookie = new Cookie(apiContext.appid + "_ct", "");
                    ct_cookie.setMaxAge(0);
                    ct_cookie.setHttpOnly(false);
                    ct_cookie.setSecure(false);
                    ct_cookie.setPath("/");
                    if (apiContext.host != null && map.containsKey(apiContext.host)) {
                        ct_cookie.setDomain(map.get(apiContext.host));
                    }
                    response.addCookie(ct_cookie);
                }
                if (fatalError) {
                    // 错误请求
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                } else if (parseResult != ApiReturnCode.SUCCESS) {
                    // 访问被拒绝(如签名验证失败)
                    Exception e = output(apiContext, parseResult, EMPTY_METHOD_CALL_ARRAY, response);
                    if (e != null) {
                        logger.error(SERVLET_MARKER, "output failed.", e);
                    }
                } else {
                    Exception e = output(apiContext, ApiReturnCode.SUCCESS,
                            apiContext.apiCallInfos.toArray(new ApiMethodCall[apiContext.apiCallInfos.size()]), response);
                    if (e != null) {
                        logger.error(SERVLET_MARKER, "output failed.", e);
                    }
                }
            } catch (Exception e) {
                logger.error(SERVLET_MARKER, "output failed.", e);
            }
            if (apiContext != null) {
                apiContext.clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void serializeCallResult(ApiContext apiContext, ApiMethodCall call) throws SerializeException {
        try {
            int oldSize = apiContext.outputStream.size();
            switch (apiContext.format) {
                case XML:
                    if (call.result == null) {
                        if (call.method.returnType != RawString.class) {
                            apiContext.outputStream.write(ConstField.XML_EMPTY);
                        }
                    } else {
                        ((Serializer<Object>)call.method.serializer).toXml(call.result, apiContext.outputStream, true);
                    }
                    break;
                case JSON:
                    if (apiContext.serializeCount > 0) {
                        apiContext.outputStream.write(ConstField.JSON_SPLIT);
                    }
                    if (call.result == null) {
                        if (call.method.returnType != RawString.class) {
                            apiContext.outputStream.write(ConstField.JSON_EMPTY);
                        }
                    } else {
                        ((Serializer<Object>)call.method.serializer).toJson(call.result, apiContext.outputStream, true);
                    }
                    break;
            }
            call.resultLen = apiContext.outputStream.size() - oldSize;
        } catch (Exception e) {
            throw new SerializeException(e);
        } finally {
            apiContext.serializeCount++;
        }
    }

    /**
     * 解析header，参数，cookie中的信息，这里返回任何预定义的错误信息
     * 解析结果填充到 ApiContext 对象中
     */
    private void parseParameter(ApiContext context, HttpServletRequest request, HttpServletResponse response) {
        // 解析通用参数
        {
            String agent1 = request.getHeader(HttpHeaders.USER_AGENT);
            context.agent = "truck.tester";
            context.referer = request.getHeader(HttpHeaders.REFERER);
            context.clientIP = MiscUtil.getClientIP(request);
            context.cid = request.getParameter(CommonParameter.callId);
            if (context.cid != null && context.cid.length() > 32) {
                context.cid = context.cid.substring(0, 32);
            }
            if (context.cid == null) {
                context.cid = REQ_TAG + context.startTime;
            }
            context.host = request.getHeader("host");
            context.cid = SERVER_ADDRESS + CommonConfig.getInstance().getServerAddress() + SPLIT + THREADID + Thread.currentThread().getId() + SPLIT + context.cid;
            context.versionCode = request.getParameter(CommonParameter.versionCode);
            context.deviceIdStr = request.getParameter(CommonParameter.deviceId);
            context.deviceToken = request.getParameter(CommonParameter.deviceToken);
            context.uid = request.getParameter(CommonParameter.userId);
            context.jsonpCallback = request.getParameter(CommonParameter.jsonpCallback);
            context.token = request.getParameter(CommonParameter.token);
            if (context.jsonpCallback != null) {
                if (!context.callbackRegex.matcher(context.jsonpCallback).matches()) {
                    logger.error("unsupported callback name : " + context.jsonpCallback);
                    context.jsonpCallback = null;
                }
            }
            MDC.clear();
            MDC.put(CommonParameter.callId, context.cid);
            MDC.put(CommonParameter.clientIp, context.clientIP);
            if (context.deviceIdStr != null) {
                MDC.put(CommonParameter.deviceId, context.deviceIdStr);
            }
        }

        //应用编号,
        {
            String appid = request.getParameter(CommonParameter.applicationId);
            context.appid = (appid != null && appid.length() != 0) ? Integer.parseInt(appid) : 0;
            MDC.put(CommonParameter.applicationId, appid);
        }

        //cookie
        {
            // 优先使用 url 中的 userToken 和 deviceId
            Cookie[] cs = request.getCookies();
            if (cs != null) {
                String tokenName = context.appid + CommonParameter.token;
                String idfaName = context.appid + ExtendParameter.idfa;
                for (Cookie c : cs) {
                    if (tokenName.equals(c.getName())) {
                        try {
                            if (context.token == null && c.getValue() != null && !c.getValue().isEmpty()) {
                                context.token = URLDecoder.decode(c.getValue(), "utf-8");
                            }
                        } catch (Exception e) {
                            logger.error(SERVLET_MARKER, "token in cookie error " + c.getValue(), e);
                            context.clearUserToken = true;
                        }
                    } else if (CommonParameter.cookieDeviceId.equals(c.getName())) {
                        if (context.deviceIdStr == null) {
                            context.deviceIdStr = c.getValue();
                            MDC.put(CommonParameter.deviceId, context.deviceIdStr);
                        }
                    } else if(idfaName.equals(c.getName())) {
                        if(context.idfa == null){
                            context.idfa = c.getValue();
                            MDC.put(ExtendParameter.idfa,context.idfa);
                        }
                    } else {
                        context.addCookie(c.getName(), c.getValue());
                    }
                }
            }

            // 优先使用url中覆写的 cookie 值
            String cookies = request.getParameter(CommonParameter.cookie);
            if (cookies != null && cookies.length() > 0) {
                String[] cos = cookies.split(";");
                for (String c : cos) {
                    int index = c.indexOf('=');
                    if (index > 0 && index != c.length()) {
                        context.addCookie(c.substring(0, index).trim(), c.substring(index + 1));
                    }
                }
            }
        }

        //集成第三方的编号，这个编号没有太高的安全性要求，采用明文方式传输即可
        {
            String thirdPartyId = request.getParameter(CommonParameter.thirdPartyId);
            context.thirdPartyId = thirdPartyId != null && !thirdPartyId.isEmpty() ? Integer.parseInt(thirdPartyId) : 0;
        }

        // 确定返回信息语言
        {
            context.location = request.getParameter(CommonParameter.location);
        }

        // 构造请求字符串用于日志记录
        {
            parseRequestInfo(context, request);
        }

        // 确定返回值的序列化类型
        {
            parseFormatType(context, request);
        }

        // 解析调用者身份(在验证签名正确前此身份不受信任)
        {
            parseToken(context, request);
            if (context.caller != null) {
                context.caller.phoneNumber = request.getParameter(CommonParameter.phoneNumber);
            }
        }
    }

    /**
     * 根据客户端在Header或者Cookie中设定的目标dubbo服务的版本号或者url，绕过注册中心调用对应的dubbo服务，仅在DEBUG模式下允许使用
     */

    /**
     * 构造请求字符串用于日志记录
     * // HttpServletRequest.getReader 可以获取未decode的数据 但是会影响后续getParameter获取参数;
     * // 暂时先在debug模式下用URLEncode转义，正式环境考虑到性能损失不做处理。
     */
    private void parseRequestInfo(ApiContext context, HttpServletRequest request) {
        String httpMethod = request.getMethod();
        if (HTTPMETHOD_POST.equalsIgnoreCase(httpMethod)) {
            StringBuilder sb = new StringBuilder(256);
            Enumeration<String> keys = request.getParameterNames();
            /*
            if (CompileConfig.isDebug) {  // 开发环境下用于将打印到日志的url还原成能够直接放到浏览器请求的编码格式。
                try {
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement();
                        if (key != null) {
                            sb.append(key);
                            sb.append("=");
                            sb.append(URLEncoder.encode(request.getParameter(key), "UTF-8"));
                            sb.append("&");
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error(SERVLET_MARKER, "URLEncoder encode the post data failad", e);
                }
            } else {
            */
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    if (key != null) {
                        sb.append(key);
                        sb.append("=");
                        sb.append(request.getParameter(key));
                        sb.append("&");
                    }
                }
            /* } */
            context.requestInfo = sb.toString();
        } else {
            context.requestInfo = request.getQueryString();
        }
    }

    /**
     * 确定返回值的序列化类型
     */
    private void parseFormatType(ApiContext context, HttpServletRequest request) {
        String format = request.getParameter(CommonParameter.format);
        if (format != null && format.length() > 0) {
            if (format.equals(FORMAT_XML)) {
                context.format = SerializeType.XML;
            } else if (format.equals(FORMAT_JSON)) {
                context.format = SerializeType.JSON;
            } else if (format.equals(FORMAT_PLAINTEXT)) {
                context.format = SerializeType.PLAINTEXT;
            } else {
                context.format = SerializeType.JSON;
            }
        } else {
            context.format = SerializeType.JSON;
        }
    }

    /**
     * 解析调用者身份(在验证签名正确前此身份不受信任)
     */
    private void parseToken(ApiContext context, HttpServletRequest request) {
        try {
            // user token存在时解析出调用者信息
            if (context.token != null && context.token.length() > 0) {
                context.caller = parseCallerInfo(context,context.token);
            }
            // 查看是否可以根据dtk确认设备身份
            if (context.caller == null) {
                if (context.deviceToken != null) {
                    context.caller = parseCallerInfo(context, context.deviceToken);
                }
            }
        } catch (Exception e) {
            logger.error(SERVLET_MARKER, "parse token failed.", e);
        }
    }

    /**
     * 处理请求
     *
     * @param name   请求名称
     * @param params 参数列表
     * @return 返回结果
     */
    protected abstract Object processCall(String name, String[] params);

    /**
     * 执行具体的api接口调用
     */
    private void executeApiCall(ApiMethodCall call, HttpServletRequest request, HttpServletResponse response) {
        try {
            ApiContext context = ApiContext.getCurrent();
            // 下传调用ID
            RpcContext.getContext().setAttachment(CommonParameter.callId, context.cid);
            RpcContext.getContext().setAttachment(CommonParameter.clientIp, context.clientIP);
            if (context.caller == null) {
                RpcContext.getContext().setAttachment(CommonParameter.cookieDeviceId, context.deviceIdStr);
                RpcContext.getContext().setAttachment(CommonParameter.applicationId, String.valueOf(context.appid));
            } else {
                if (context.caller.uid != 0) {
                    RpcContext.getContext().setAttachment(CommonParameter.userId, String.valueOf(context.caller.uid));
                }
                if (context.caller.deviceId != 0) {
                    RpcContext.getContext().setAttachment(CommonParameter.deviceId, String.valueOf(context.caller.deviceId));
                }
                RpcContext.getContext().setAttachment(CommonParameter.applicationId, String.valueOf(context.caller.appid));
            }
            RpcContext.getContext().setAttachment(ExtendParameter.terminal,ExtendParameter.getTerminal(String.valueOf(context.appid)));
            initExtendParameters(request,context);
            if (call.businessId != null && call.businessId.length() < 4096) {
                RpcContext.getContext().setAttachment(CommonParameter.businessId, call.businessId);
            }

            // 当接口声明了静态 mock 返回值或被标记为短路时
            if (call.method.staticMockValue != null) {
                call.result = call.method.staticMockValue;
            } else {
                // 调试情况下可以通过cookie指定处理该请求的dubbo服务实例
                if (CompileConfig.isDebug) {
                    Cookie[] cs = request.getCookies();
                    String[] parameters = new String[call.parameters == null ? 2 : call.parameters.length + 2];
                    if (call.parameters != null) {
                        for (int i = 0; i < call.parameters.length; i++) {
                            parameters[i] = call.parameters[i];
                        }
                    }
                    parameters[call.parameters.length] = request.getHeader(DEBUG_DUBBOVERSION);
                    parameters[call.parameters.length + 1] = request.getHeader(DEBUG_DUBBOSERVICE_URL);
                    if (cs != null) {
                        for (Cookie c : cs) {
                            if (DEBUG_DUBBOVERSION.equals(c.getName())) {
                                if (c.getValue() != null && !c.getValue().isEmpty()) {
                                    parameters[call.parameters.length] = c.getValue();
                                }
                            }
                            if (DEBUG_DUBBOSERVICE_URL.equals(c.getName())) {
                                if (c.getValue() != null && !c.getValue().isEmpty()) {
                                    parameters[call.parameters.length + 1] = c.getValue();
                                }
                            }
                        }
                    }
                    call.result = processCall(call.method.methodName, parameters);
                } else {
                    call.result = processCall(call.method.methodName, call.parameters);
                }
            }
            //dubbo接口能够获取到RpcContext中的notification,device的接口errorCode不是通过RpcContext传递的。
            Map<String, String> notifications = DubboExtProperty.getCurrentNotifications();
            if (notifications != null && notifications.size() > 0) {
                for (Entry<String, String> entry : notifications.entrySet()) {
                    String value = entry.getValue();
                    if (ConstField.SET_COOKIE_TOKEN.equals(entry.getKey())) {
                        HashMap<String, String> map = CommonConfig.getInstance().getOriginWhiteList();
                        if (value != null && value.length() > 0) {
                            Cookie tk_cookie = new Cookie(context.appid + CommonParameter.token, URLEncoder.encode(value, "utf-8"));
                            tk_cookie.setMaxAge(-1);
                            tk_cookie.setHttpOnly(true);
                            tk_cookie.setSecure(false);
                            tk_cookie.setPath("/");

                            // 用于提示客户端当前token是否存在
                            Cookie ct_cookie = new Cookie(context.appid + "_ct", "1");
                            ct_cookie.setMaxAge(-1);
                            ct_cookie.setHttpOnly(false);
                            ct_cookie.setSecure(false);
                            ct_cookie.setPath("/");

                            if (CompileConfig.isDebug) {
                                logger.info(
                                        "host:" + context.host + " in map:" + map.containsKey(context.host) + " domain:" + map.get(context.host));
                            }

                            if (context.host != null && map.containsKey(context.host)) {
                                tk_cookie.setDomain(map.get(context.host));
                                ct_cookie.setDomain(map.get(context.host));
                            }
                            response.addCookie(tk_cookie);
                            response.addCookie(ct_cookie);
                            context.clearUserToken = false; // user token will be override.
                        } else { // 删除cookie
                            context.clearUserToken = true;
                        }
                    } else if (ConstField.SET_COOKIE_USER_INFO.equals(entry.getKey())) {
                        HashMap<String, String> map = CommonConfig.getInstance().getOriginWhiteList();
                        if (value != null) {
                            Cookie userInfo_cookie = new Cookie(context.appid + "_uinfo", URLEncoder.encode(value, "utf-8"));
                            userInfo_cookie.setMaxAge(Integer.MAX_VALUE);
                            userInfo_cookie.setHttpOnly(false);
                            userInfo_cookie.setSecure(false);
                            userInfo_cookie.setPath("/");

                            if (context.host != null && map.containsKey(context.host)) {
                                userInfo_cookie.setDomain(map.get(context.host));
                            }
                            response.addCookie(userInfo_cookie);
                        }
                    } else if (ConstField.ERROR_CODE_EXT.equals(entry.getKey())) {
                        try {
                            int c = Integer.parseInt(value);
                            call.setReturnCode(c, c, "ServiceException");
                        } catch (Exception e) {
                            logger.error("service return an illegal code " + value, e);
                            call.setReturnCode(ApiReturnCode.INTERNAL_SERVER_ERROR);
                        }
                    } else if (ConstField.SERVICE_LOG.equals(entry.getKey())) {
                        call.serviceLog = value;
                    } else if (ConstField.REDIRECT_TO.equals(entry.getKey())) {
                        response.sendRedirect(entry.getValue());
                    } else {
                        context.addNotification(new KeyValuePair(entry.getKey(), JSONARRAY_PREFIX + value + JSONARRAY_SURFIX));
                    }
                }
            }
            call.setReturnCode(ApiReturnCode.SUCCESS);
        } catch (ReturnCodeException rce) {//APIGW内部异常传递,RuntimeException
            call.setReturnCode(rce.getCode());
            if (rce.getCode() == ApiReturnCode.PARAMETER_ERROR) {
                logger.error(SERVLET_MARKER, "servlet catch an api error. " + rce.getMessage());
            } else {
                logger.error(SERVLET_MARKER, "servlet catch an api error.", rce);
            }
        } catch (Throwable t) {
            if (t instanceof ServiceException) {
                ServiceException se = (ServiceException)t;
                logger.error(SERVLET_MARKER, "service exception. code:" + se.getCode() + " msg:" + se.getMsg());
                call.setReturnCode(se.getCode(), se.getDisplayCode(), se.getMsg());
            } else if (t.getCause() instanceof com.alibaba.dubbo.remoting.TimeoutException) {
                logger.error(SERVLET_MARKER, "dubbo timeout.", t);
                call.setReturnCode(ApiReturnCode.DUBBO_SERVICE_TIMEOUT_ERROR);
            } else if (t.getCause() instanceof com.alibaba.dubbo.remoting.RemotingException || t instanceof com.alibaba.dubbo.rpc.RpcException) {
                logger.error(SERVLET_MARKER, "dubbo exception.", t);
                call.setReturnCode(ApiReturnCode.DUBBO_SERVICE_NOTFOUND_ERROR);
            } else {
                logger.error(SERVLET_MARKER, "internal error.", t);
                call.setReturnCode(ApiReturnCode.INTERNAL_SERVER_ERROR);
            }
        } finally {
             DubboExtProperty.clearNotificaitons();
        }

        int display = call.getReturnCode();

        if (display > 0) {
            if (call.method.errors == null) {
                call.replaceReturnCode(ApiReturnCode.UNKNOWN_ERROR);
            } else {
                // 异常编码过滤，保证接口只返回其声明过的异常编码给客户端
                if (Arrays.binarySearch(call.method.errors, display) < 0) {
                    call.replaceReturnCode(ApiReturnCode.UNKNOWN_ERROR);
                }
            }
        }
    }

    private void initExtendParameters(HttpServletRequest request,ApiContext apiContext) {
        RpcContext context = getCurrentContext();
        context.setAttachment(ExtendParameter.appVersion,request.getParameter(ExtendParameter.appVersion));
        context.setAttachment(ExtendParameter.appChannel,request.getParameter(ExtendParameter.appChannel));
        context.setAttachment(ExtendParameter.idfa,request.getParameter(ExtendParameter.idfa));
        context.setAttachment(ExtendParameter.latitude,request.getParameter(ExtendParameter.latitude));
        context.setAttachment(ExtendParameter.longitude,request.getParameter(ExtendParameter.longitude));
        context.setAttachment(ExtendParameter.platform,request.getParameter(ExtendParameter.platform));
        context.setAttachment(ExtendParameter.systemVersion,request.getParameter(ExtendParameter.systemVersion));
        context.setAttachment(ExtendParameter.model,request.getParameter(ExtendParameter.model));
        if(StringUtils.isEmpty(request.getParameter(ExtendParameter.idfa))){
            if(apiContext.idfa == null)
                context.setAttachment(ExtendParameter.idfa,context.getAttachment(CommonParameter.cookieDeviceId));
            else
                context.setAttachment(ExtendParameter.idfa,apiContext.idfa);
        }
    }

    private RpcContext getCurrentContext() {
        return RpcContext.getContext();
    }


    /**
     * 从userToken中解析调用者信息
     */
    abstract protected CallerInfo parseCallerInfo(ApiContext context, byte[] token);

    abstract protected CallerInfo parseCallerInfo(ApiContext context,String token);

    /**
     * 解析调用方法
     */
    abstract protected AbstractReturnCode parseMethodInfo(ApiContext context, HttpServletRequest request);

    /**
     * 输出返回到调用端
     */
    private Exception output(ApiContext apiContext, AbstractReturnCode code, ApiMethodCall[] calls, HttpServletResponse response) {
        Exception outputException = null;

        try {
            if (calls.length == 1 && calls[0].method.returnType == RawString.class) {// rawString的处理，将dubbo service返回的结果直接输出
                OutputStream output = response.getOutputStream();
                if (code == ApiReturnCode.SUCCESS && calls[0].getReturnCode() == ApiReturnCode.SUCCESS.getCode()) {
                    apiContext.outputStream.writeTo(output);
                } else if (code != ApiReturnCode.SUCCESS) {
                    output.write(code.getName().getBytes(ConstField.UTF8));
                } else {
                    output.write(calls[0].getReturnMessage().getBytes(ConstField.UTF8));
                }
            } else {
                Response apiResponse = new Response();
                apiResponse.systime = System.currentTimeMillis();
                apiResponse.code = code.getDisplay().getCode();
                if (apiContext.localException != null) {
                    apiResponse.data = apiContext.localException.getData();
                }
                apiResponse.stateList = new ArrayList<CallState>(calls.length);
                if (apiContext.cid != null) {
                    apiResponse.cid = apiContext.cid;
                }
                for (ApiMethodCall call : calls) {
                    CallState state = new CallState();
                    state.code = call.getReturnCode();
                    state.msg = call.getReturnMessage();
                    if (CompileConfig.isDebug) {
                        if (call.getReturnCode() != call.getOriginCode()) {
                            state.msg = state.msg + ":" + call.getOriginCode();// debug模式将实际errorcode外露到msg中
                        }
                    }
                    // TODO: get message i10n
                    state.length = call.resultLen;
                    apiResponse.stateList.add(state);
                }
                apiResponse.notificationList = apiContext.getNotifications();

                OutputStream output = response.getOutputStream();

                switch (apiContext.format) {
                    case XML:
                        output.write(ConstField.XML_START);
                        apiResponseSerializer.toXml(apiResponse, output, true);
                        apiContext.outputStream.writeTo(output);
                        output.write(ConstField.XML_END);
                        break;
                    case JSON:
                        if (apiContext.jsonpCallback != null) {
                            output.write(apiContext.jsonpCallback.getBytes(ConstField.UTF8));
                            output.write(ConstField.JSONP_START);
                        }
                        output.write(ConstField.JSON_START);
                        apiResponseSerializer.toJson(apiResponse, output, true);
                        output.write(ConstField.JSON_CONTENT);
                        apiContext.outputStream.writeTo(output);
                        output.write(ConstField.JSON_END);
                        if (apiContext.jsonpCallback != null) {
                            output.write(ConstField.JSONP_END);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            outputException = e;
        }
        return outputException;
    }
}
