package com.abupdate.http_libs.data;

import java.io.Serializable;

public class NameValuePair implements Serializable {
    private final String name;
    private final String value;

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.format("%-20s", this.name) + "=  " + this.value;
    }

}
