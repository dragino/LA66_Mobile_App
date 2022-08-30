package com.sz.cp2102.bean;


public class MessageEvent<T>{

    private String id;

    private String name;
    private T body;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}