package com.hanzhifengyun.rxbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * RxBus
 */

public class RxBus {
    private static final String TAG = "RxBus";

    private final Subject<Object> bus;

    private RxBus() {
        this.bus = PublishSubject.create().toSerialized();
    }

    private static class RxBusHolder {
        private static final RxBus INSTANCE = new RxBus();
    }

    public static RxBus getInstance() {
        return RxBusHolder.INSTANCE;
    }



    private Map<Class, List<SubscriberMethod>> subscriberMethodMapWithSubscriber = new HashMap<>();


    /**
     * 注册
     *
     * @param subscriber 订阅者
     */
    public void register(Object subscriber) {
        Class<?> subClass = subscriber.getClass();
        Method[] methods = subClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnSubscribe.class)) {
                SubscriberMethod subscriberMethod = SubscriberMethod.newInstance(subscriber, method);

                addSubscriber(subscriberMethod);
            }
        }
    }

    private synchronized void addSubscriberMethodToMap(SubscriberMethod subscriberMethod) {
        Class<?> subscriberClass = subscriberMethod.getSubscriber().getClass();
        List<SubscriberMethod> subscriberMethodList =
                subscriberMethodMapWithSubscriber.get(subscriberClass);
        if (subscriberMethodList == null) {
            subscriberMethodList = new ArrayList<>();
            subscriberMethodMapWithSubscriber.put(subscriberClass, subscriberMethodList);
        }
        if (!subscriberMethodList.contains(subscriberMethod)) {
            subscriberMethodList.add(subscriberMethod);
        }
    }


    @SuppressWarnings("unchecked")
    private void addSubscriber(final SubscriberMethod subscriberMethod) {
        Flowable flowable;
        if (subscriberMethod.getCode() == -1) {
            flowable = toObservable(subscriberMethod.getDataType());
        } else {
            flowable = toObservable(subscriberMethod.getCode(), subscriberMethod.getDataType());
        }
        Disposable disposable = flowable.observeOn(getScheduler(subscriberMethod.getThreadType()))
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        subscriberMethod.invoke(o);
                    }
                });

        subscriberMethod.setDisposable(disposable);
        addSubscriberMethodToMap(subscriberMethod);
    }


    private Scheduler getScheduler(ThreadType threadType) {
        switch (threadType) {
            case UI:
                return AndroidSchedulers.mainThread();
            case IO:
                return Schedulers.newThread();
            case CURRENT_THREAD:
                return Schedulers.trampoline();
            default:
                throw new IllegalStateException("Unknown thread mode: " + threadType);
        }
    }

    private <T> Flowable<T> toObservable(Class<T> dataType) {
        return bus.toFlowable(BackpressureStrategy.BUFFER)
                .ofType(dataType);
    }

    private <T> Flowable<T> toObservable(final int code, final Class<T> dataType) {
        return bus.toFlowable(BackpressureStrategy.BUFFER)
                .ofType(BusData.class)
                .filter(new Predicate<BusData>() {
                    @Override
                    public boolean test(BusData busData) throws Exception {
                        return busData.getCode() == code;
                    }
                })
                .map(new Function<BusData, Object>() {
                    @Override
                    public Object apply(BusData busData) throws Exception {
                        return busData.getData();
                    }
                })
                .ofType(dataType);
    }


    /**
     * 取消注册
     *
     * @param subscriber 订阅者
     */
    public void unRegister(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethodList = subscriberMethodMapWithSubscriber.get(subscriberClass);
        if (subscriberMethodList != null) {
            for (SubscriberMethod subscriberMethod : subscriberMethodList) {
                subscriberMethod.dispose();
            }
            subscriberMethodMapWithSubscriber.remove(subscriberClass);
        }

    }


    public void send(Object data) {
        bus.onNext(data);
    }

    public void send(int code, Object data) {
        bus.onNext(new BusData(code, data));
    }

    public void send(int code) {
        bus.onNext(new BusData(code, new EmptyData()));
    }





}
