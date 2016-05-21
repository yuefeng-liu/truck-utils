package com.truck.utils.gateway.utils.util;

import com.truck.utils.gateway.utils.annotation.Description;
import com.truck.utils.gateway.utils.core.PocClassLoader;
import com.truck.utils.gateway.utils.define.CommonParameter;
import com.truck.utils.gateway.utils.define.HttpApiExecuter;
import com.truck.utils.gateway.utils.entity.ApiMethodInfo;
import com.truck.utils.gateway.utils.entity.ApiParameterInfo;
import com.truck.utils.gateway.utils.entity.CommonConfig;
import com.truck.utils.gateway.utils.entity.CompileConfig;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * 为无状态逻辑类的指定函数产生一个代理，代理接口接受字符串数组，转换后调用原函数
 * 生产 dubbo 消费者
 *
 * @author truck
 * @date 2015/11/30
 */
public class HttpApiProvider implements Opcodes {
    private static final String REGEX_PREFIX = "regex_";
    private static final String CONST_PREFIX = "const_";

    public synchronized static HttpApiExecuter getApiExecuter(String name, ApiMethodInfo method) {
        try {
            Class<?> clazz = method.proxyMethodInfo.getDeclaringClass();
            ApiParameterInfo[] parameterInfos = method.parameterInfos;
            String className = "com/truck/common/gateway/utils/autogen/ApiExecuter_" + name.replace('.', '_');
            className = className.replace('$', '_');
            String classDesc = "L" + className + ";";
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            FieldVisitor fv;
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[] { Type.getInternalName(HttpApiExecuter.class) });
            {
                fv = cw.visitField(ACC_PRIVATE, "instance", "Ljava/lang/Object;", null, null);
                fv.visitEnd();
            }
            for (int i = 0; i < parameterInfos.length; i++) {
                ApiParameterInfo parameterInfo = parameterInfos[i];
                if (parameterInfo.verifyRegex != null) {
                    fv = cw.visitField(ACC_PUBLIC, REGEX_PREFIX + parameterInfo.name, "Ljava/util/regex/Pattern;", null, null);
                    fv.visitEnd();
                }
                if (parameterInfo.needDefaultValueConstDefined) {
                    fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, CONST_PREFIX + parameterInfo.name, Type.getDescriptor(parameterInfo.type),
                            null, null);
                    fv.visitEnd();
                }
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
                for (int i = 0; i < parameterInfos.length; i++) {
                    ApiParameterInfo parameterInfo = parameterInfos[i];
                    if (parameterInfo.verifyRegex != null) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn(parameterInfo.verifyRegex);
                        mv.visitMethodInsn(INVOKESTATIC, "java/util/regex/Pattern", "compile", "(Ljava/lang/String;)Ljava/util/regex/Pattern;");
                        mv.visitFieldInsn(PUTFIELD, className, REGEX_PREFIX + parameterInfo.name, "Ljava/util/regex/Pattern;");
                    }
                }
                mv.visitInsn(RETURN);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLocalVariable("this", classDesc, null, l0, l1, 0);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                if (method.needDefaultValueConstDefined) {//结构化入参的默认值常量初始化
                    MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                    mv.visitCode();
                    for (ApiParameterInfo pinfo : method.parameterInfos) {
                        if (pinfo.needDefaultValueConstDefined) {
                            if (pinfo.defaultValue != null) {
                                mv.visitLdcInsn(pinfo.defaultValue);
                                if (pinfo.actuallyGenericType == null) {
                                    mv.visitLdcInsn(Type.getType(pinfo.type));
                                    mv.visitMethodInsn(INVOKESTATIC, "com/alibaba/fastjson/JSON", "parseObject",
                                            "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
                                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(pinfo.type));
                                } else {//仅支持List<XXX>,注意JSON.parseArray的实现用的是ArrayList
                                    mv.visitLdcInsn(Type.getType(pinfo.actuallyGenericType));
                                    mv.visitMethodInsn(INVOKESTATIC, "com/alibaba/fastjson/JSON", "parseArray",
                                            "(Ljava/lang/String;Ljava/lang/Class;)Ljava/util/List;");
                                    //                                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(pinfo.type));
                                }
                            } else {
                                mv.visitInsn(ACONST_NULL);
                            }
                            mv.visitFieldInsn(PUTSTATIC, className, CONST_PREFIX + pinfo.name, Type.getType(pinfo.type).getDescriptor());
                        }
                    }
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(2, 0);
                    mv.visitEnd();
                }
            }
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setInstance", "(Ljava/lang/Object;)V", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, className, "instance", "Ljava/lang/Object;");
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitInsn(RETURN);
                Label l2 = new Label();
                mv.visitLabel(l2);
                mv.visitLocalVariable("this", classDesc, null, l0, l2, 0);
                mv.visitLocalVariable("obj", "Ljava/lang/Object;", null, l0, l2, 1);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }
            {
                PoCMethodVisitor pmv = new PoCMethodVisitor(cw, ACC_PUBLIC, "execute", "([Ljava/lang/String;)Ljava/lang/Object;", null, null);
                pmv.visitCode();
                if (parameterInfos.length > 0) {
                    for (int i = 0; i < parameterInfos.length; i++) {
                        ApiParameterInfo parameterInfo = parameterInfos[i];
                        Label l1 = new Label();
                        Label l2 = new Label();
                        if (parameterInfo.isRequired || parameterInfo.verifyRegex != null) {
                            if (parameterInfo.isRequired) {
                                pmv.loadArg(1);
                                pmv.loadConst(i);
                                pmv.visitInsn(AALOAD);
                                if (parameterInfo.verifyRegex != null) {
                                    pmv.visitJumpInsn(IFNULL, l1);
                                } else {
                                    pmv.visitJumpInsn(IFNONNULL, l2);
                                }
                            }
                            if (parameterInfo.verifyRegex != null) {
                                if (!parameterInfo.isRequired) {
                                    pmv.loadArg(1);
                                    pmv.loadConst(i);
                                    pmv.visitInsn(AALOAD);
                                    pmv.visitJumpInsn(IFNULL, l2);
                                }
                                pmv.loadArg(0);
                                pmv.visitFieldInsn(GETFIELD, className, REGEX_PREFIX + parameterInfo.name, "Ljava/util/regex/Pattern;");
                                pmv.loadArg(1);
                                pmv.loadConst(i);
                                pmv.visitInsn(AALOAD);
                                pmv.visitMethodInsn(INVOKEVIRTUAL, "java/util/regex/Pattern", "matcher",
                                        "(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;");
                                pmv.visitMethodInsn(INVOKEVIRTUAL, "java/util/regex/Matcher", "matches", "()Z");
                                pmv.visitJumpInsn(IFNE, l2);
                            }
                            pmv.visitLabel(l1);
                            pmv.visitTypeInsn(NEW, "com/truck/utils/gateway/utils/entity/ReturnCodeException");
                            pmv.visitInsn(DUP);
                            pmv.visitFieldInsn(GETSTATIC, "com/truck/utils/gateway/utils/entity/ApiReturnCode", "PARAMETER_ERROR",
                                    "Lcom/truck/utils/gateway/utils/entity/AbstractReturnCode;");
                            pmv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                            pmv.visitInsn(DUP);
                            pmv.loadConst("method=" + method.methodName + " parameter validation failed : " + parameterInfo.name + "=");
                            pmv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                            pmv.loadArg(1);
                            pmv.loadConst(i);
                            pmv.visitInsn(AALOAD);
                            pmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                            pmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                            pmv.visitMethodInsn(INVOKESPECIAL, "com/truck/utils/gateway/utils/entity/ReturnCodeException", "<init>",
                                    "(Lcom/truck/utils/gateway/utils/entity/AbstractReturnCode;Ljava/lang/String;)V");
                            pmv.visitInsn(ATHROW);
                            pmv.visitLabel(l2);
                        }
                    }
                    pmv.declareLocal("e", Exception.class);
                    for (int i = 0; i < parameterInfos.length; i++) {
                        Label l1 = new Label();
                        Label l2 = new Label();
                        Label l3 = new Label();
                        ApiParameterInfo parameterInfo = parameterInfos[i];
                        Class<?> parameterType = parameterInfo.type;
                        pmv.declareLocal("" + i, parameterInfo.type);
                        if (parameterType != String.class) {
                            pmv.visitTryCatchBlock(l1, l2, l3, "java/lang/Exception");
                            pmv.visitLabel(l1);
                        }
                        String defaultValueString = null;
                        if (!parameterInfo.isRequired) {
                            defaultValueString = parameterInfo.defaultValue;
                            if (defaultValueString == null || defaultValueString.length() == 0) {
                                if (parameterType.isEnum()) {
                                    defaultValueString = null;
                                } else if (parameterType.isPrimitive()) {
                                    defaultValueString = "0";
                                } else if (parameterType == String.class) {
                                    defaultValueString = null;
                                } else if (parameterType.getAnnotation(Description.class) != null) {
                                    defaultValueString = null;//如果未设置值
                                }
                            }
                        }
                        pmv.loadArg(1);
                        pmv.loadConst(i);
                        pmv.visitInsn(AALOAD);
                        Label loopLabel2 = new Label();

                        if (!parameterInfo.isRequired) {
                            Label loopLabel1 = new Label();
                            pmv.visitJumpInsn(IFNONNULL, loopLabel1);
                            if (!parameterInfo.needDefaultValueConstDefined) {
                                pmv.loadConst(defaultValueString, parameterType);
                            } else {//加载定义的常量
                                pmv.visitFieldInsn(GETSTATIC, className, CONST_PREFIX + parameterInfo.name, Type.getDescriptor(parameterInfo.type));
                            }
                            pmv.visitJumpInsn(GOTO, loopLabel2);
                            pmv.visitLabel(loopLabel1);
                            pmv.visitVarInsn(ALOAD, 1);
                            pmv.loadConst(i);
                            pmv.visitInsn(AALOAD);
                        }

                        if (parameterType == boolean.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z");
                        } else if (parameterType == byte.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "parseByte", "(Ljava/lang/String;)B");
                        } else if (parameterType == char.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
                            pmv.visitInsn(I2C);
                        } else if (parameterType == short.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "parseShort", "(Ljava/lang/String;)S");
                        } else if (parameterType == int.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
                        } else if (parameterType == long.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "parseLong", "(Ljava/lang/String;)J");
                        } else if (parameterType == float.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F");
                        } else if (parameterType == double.class) {
                            pmv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D");
                        } else if (parameterType == String.class) {
                            // Do nothing
                        } else if (parameterType.isEnum()) {
                            pmv.visitMethodInsn(INVOKESTATIC, parameterType.getName().replace('.', '/'), "valueOf",
                                    "(Ljava/lang/String;)" + Type.getDescriptor(parameterType));
                        } else if (parameterType == Map.class && CommonParameter.cookie.equals(parameterInfo.name)) {
                            pmv.visitLdcInsn(Type.getType(parameterType));
                            pmv.visitMethodInsn(INVOKESTATIC, "com/alibaba/fastjson/JSON", "parseObject",
                                    "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
                            pmv.visitTypeInsn(CHECKCAST, Type.getInternalName(Map.class));
                        } else {
                            if (parameterInfo.actuallyGenericType == null) {
                                pmv.visitLdcInsn(Type.getType(parameterType));
                                pmv.visitMethodInsn(INVOKESTATIC, "com/alibaba/fastjson/JSON", "parseObject",
                                        "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
                                pmv.visitTypeInsn(CHECKCAST, Type.getInternalName(parameterType));
                            } else {//仅支持List<XXX>,注意JSON.parseArray的实现用的是ArrayList,返回的是List没必要在去cast了
                                pmv.visitLdcInsn(Type.getType(parameterInfo.actuallyGenericType));
                                pmv.visitMethodInsn(INVOKESTATIC, "com/alibaba/fastjson/JSON", "parseArray",
                                        "(Ljava/lang/String;Ljava/lang/Class;)Ljava/util/List;");
                                //                                pmv.visitTypeInsn(CHECKCAST, Type.getInternalName(parameterType));仅仅支持List这里不用cast了
                            }
                        }
                        if (!parameterInfo.isRequired) {
                            pmv.visitLabel(loopLabel2);
                        }
                        pmv.setLocal("" + i);
                        // 参数类型不为String时需要捕获类型转换异常
                        if (parameterType != String.class) {
                            pmv.visitLabel(l2);
                            Label label_end = new Label();
                            pmv.visitJumpInsn(GOTO, label_end);
                            pmv.visitLabel(l3);
                            pmv.setLocal("e");
                            pmv.visitTypeInsn(NEW, "com/truck/utils/gateway/utils/entity/ReturnCodeException");
                            pmv.visitInsn(DUP);
                            pmv.visitFieldInsn(GETSTATIC, "com/truck/utils/gateway/utils/entity/ApiReturnCode", "PARAMETER_ERROR",
                                    "Lcom/truck/utils/gateway/utils/entity/AbstractReturnCode;");
                            pmv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                            pmv.visitInsn(DUP);
                            pmv.loadConst("method=" + method.methodName + " parameter validation failed : " + parameterInfo.name + "=");
                            pmv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                            pmv.loadArg(1);
                            pmv.loadConst(i);
                            pmv.visitInsn(AALOAD);
                            pmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                            pmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                            pmv.loadLocal("e");
                            pmv.visitMethodInsn(INVOKESPECIAL, "com/truck/utils/gateway/utils/entity/ReturnCodeException", "<init>",
                                    "(Lcom/truck/utils/gateway/utils/entity/AbstractReturnCode;Ljava/lang/String;Ljava/lang/Exception;)V");
                            pmv.visitInsn(ATHROW);
                            pmv.visitLabel(label_end);
                        }
                    }
                }
                pmv.visitVarInsn(ALOAD, 0);
                pmv.visitFieldInsn(GETFIELD, className, "instance", "Ljava/lang/Object;");
                pmv.visitTypeInsn(CHECKCAST, clazz.getName().replace('.', '/'));
                for (int i = 0; i < parameterInfos.length; i++) {
                    pmv.loadLocal("" + i);
                }
                if (clazz.isInterface()) {
                    pmv.visitMethodInsn(INVOKEINTERFACE, clazz.getName().replace('.', '/'), method.proxyMethodInfo.getName(),
                            Type.getMethodDescriptor(method.proxyMethodInfo));
                } else {
                    pmv.visitMethodInsn(INVOKEVIRTUAL, clazz.getName().replace('.', '/'), method.proxyMethodInfo.getName(),
                            Type.getMethodDescriptor(method.proxyMethodInfo));
                }

                if (String.class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/StringResp", "convert",
                            "(Ljava/lang/String;)Lcom/truck/common/gateway/utils/responseEntity/StringResp;");
                } else if (String[].class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/StringArrayResp", "convert",
                            "([Ljava/lang/String;)Lcom/truck/common/gateway/utils/responseEntity/StringArrayResp;");
                } else if (boolean.class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/BoolResp", "convert", "(Z)Lcom/truck/common/gateway/utils/responseEntity/BoolResp;");
                } else if (boolean[].class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/BoolArrayResp", "convert",
                            "([Z)Lcom/truck/common/gateway/utils/responseEntity/BoolArrayResp;");
                } else if (byte.class == method.returnType || short.class == method.returnType || char.class == method.returnType
                        || int.class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/NumberResp", "convert",
                            "(" + Type.getDescriptor(method.returnType) + ")Lcom/truck/common/gateway/utils/responseEntity/NumberResp;");
                } else if (byte[].class == method.returnType || short[].class == method.returnType || char[].class == method.returnType
                        || int[].class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/NumberArrayResp", "convert",
                            "(" + Type.getDescriptor(method.returnType) + ")Lcom/truck/common/gateway/utils/responseEntity/NumberArrayResp;");
                } else if (long.class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/LongResp", "convert", "(J)Lcom/truck/common/gateway/utils/responseEntity/LongResp;");
                } else if (long[].class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/LongArrayResp", "convert",
                            "([J)Lcom/truck/common/gateway/utils/responseEntity/LongArrayResp;");
                } else if (double.class == method.returnType || float.class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/DoubleResp", "convert",
                            "(" + Type.getDescriptor(method.returnType) + ")Lcom/truck/common/gateway/utils/responseEntity/DoubleResp;");
                } else if (double[].class == method.returnType || float[].class == method.returnType) {
                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/DoubleArrayResp", "convert",
                            "(" + Type.getDescriptor(method.returnType) + ")Lcom/truck/common/gateway/utils/responseEntity/DoubleArrayResp;");
                } else if (Collection.class.isAssignableFrom(method.returnType)) {//support Collection
                    if (String.class == method.actuallyGenericReturnType) {
                        pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/StringArrayResp", "convert",
                                "(Ljava/util/Collection;)Lcom/truck/common/gateway/utils/responseEntity/StringArrayResp;");
                    } else {
                        pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/ObjectArrayResp", "convert",
                                "(Ljava/util/Collection;)Lcom/truck/common/gateway/utils/responseEntity/ObjectArrayResp;");
                    }
                } else if (method.returnType.isArray()) {
                    //下面这句话已经support了object array，不过pojo未支持
                    //                    pmv.visitMethodInsn(INVOKESTATIC, "com/truck/common/gateway/utils/responseEntity/ObjectArrayResp", "convert",
                    //                                        "([Ljava/lang/Object;)Lcom/truck/common/gateway/utils/responseEntity/ObjectArrayResp;");
                    throw new RuntimeException(
                            String.format("unsupport return type,object array is not support now.type:%s,groupName:%s,methodName:%s",
                                    method.returnType, method.groupName, method.methodName));
                }

                pmv.visitInsn(ARETURN);
                pmv.visitMaxs(0, 0);
                pmv.visitEnd();
            }
            cw.visitEnd();
            if (CompileConfig.isDebug) {
                FileOutputStream fos = null;
                try {
                    File folder = new File(CommonConfig.getInstance().getAutogenPath() + File.separator + "ApiExecuter" + File.separator);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    fos = new FileOutputStream(
                            CommonConfig.getInstance().getAutogenPath() + File.separator + "ApiExecuter" + File.separator + name + ".class");
                    fos.write(cw.toByteArray());
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
            HttpApiExecuter e = (HttpApiExecuter)new PocClassLoader(HttpApiProvider.class.getClassLoader()).defineClass(className.replace('/', '.'),
                    cw.toByteArray()).newInstance();
            e.setInstance(method.serviceInstance);
            return e;
        } catch (Exception e) {
            throw new RuntimeException("generator failed. " + name, e);
        } catch (Throwable t) {
            throw new RuntimeException("generator failed. " + name, t);
        }
    }
}
