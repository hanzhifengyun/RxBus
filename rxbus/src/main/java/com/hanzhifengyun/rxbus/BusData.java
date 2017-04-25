package com.hanzhifengyun.rxbus;

/**
 * 数据
 */

public class BusData {
    private final int code;
    private final Object data;

    public BusData(int code, Object data) {
        this.code = code;
        this.data = data;
    }


    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}
