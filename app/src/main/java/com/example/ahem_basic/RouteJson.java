package com.example.ahem_basic;

import java.util.List;

public class RouteJson {
    private int code;
    private String message;
    private List<RoutingData> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<RoutingData> getData() {
        return data;
    }

    public void setData(List<RoutingData> data) {
        this.data = data;
    }
}
