package com.macrowing.indaas.upm.utils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Response {

    private boolean success;

    private Integer code;

    private String message;

    private Map<String, Object> data = new HashMap<String, Object>();

    private Response() {
    }

    public static Response ok(){
        Response r = new Response();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功!");
        return r;
    }

    //失败 静态方法
    public static Response error(){
        Response r = new Response();
        r.setSuccess(false);
        r.setCode(400);
        r.setMessage("失败!");
        return r;
    }

    public Response success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public Response code(Integer code){
        this.setCode(code);
        return this;
    }

    public Response message(String message){
        this.setMessage(message);
        return this;
    }

    public Response data(String key,Object value){
        this.data.put(key,value);
        return this;
    }

    public Response data(Map<String,Object> map){
        this.setData(map);
        return this;
    }

}
