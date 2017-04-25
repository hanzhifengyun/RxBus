package com.hanzhifengyun.rxbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.reactivex.disposables.Disposable;

/**
 *
 */

public class SubscriberMethod {

    private final Method method;
    private final Object subscriber;
    private final int code;
    private final ThreadType threadType;
    private final Class dataType;
    private Disposable disposable;

    public SubscriberMethod(Object subscriber, Method method) {
        this.method = method;
        this.subscriber = subscriber;
        OnSubscribe onSubscribe = method.getAnnotation(OnSubscribe.class);
        this.code = onSubscribe.code();
        this.threadType = onSubscribe.threadType();

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            //无参
            this.dataType = EmptyData.class;
        } else if (parameterTypes.length == 1) {
            Class dateType = parameterTypes[0];
            if (dateType == int.class) {
                this.dataType = Integer.class;
            } else if (dateType == long.class) {
                this.dataType = Long.class;
            } else if (dateType == double.class) {
                this.dataType = Double.class;
            } else if (dateType == float.class) {
                this.dataType = Float.class;
            } else if (dateType == boolean.class) {
                this.dataType = Boolean.class;
            } else {
                this.dataType = dateType;
            }
        } else {
            this.dataType = null;
            throw new IllegalStateException("the annotation method param length can't > 1, current is " + parameterTypes.length);
        }
    }


    public static SubscriberMethod newInstance(Object subscriber, Method method) {
        return new SubscriberMethod(subscriber, method);
    }

    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    public void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public Method getMethod() {
        return method;
    }

    public Object getSubscriber() {
        return subscriber;
    }

    public int getCode() {
        return code;
    }

    public ThreadType getThreadType() {
        return threadType;
    }

    public Class getDataType() {
        return dataType;
    }

    public void invoke(Object data) {
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes == null || parameterTypes.length == 0) {
                method.invoke(subscriber);
            } else if (parameterTypes.length == 1) {
                method.invoke(subscriber, data);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
