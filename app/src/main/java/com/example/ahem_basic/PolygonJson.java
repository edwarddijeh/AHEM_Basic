package com.example.ahem_basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class PolygonJson {
    private int code;
    private String message;
    private PolygonData data;

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

    public PolygonData getData() {
        return data;
    }

    public void setData(PolygonData data) {
        this.data = data;
    }
}
