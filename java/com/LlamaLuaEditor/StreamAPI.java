package com.LlamaLuaEditor;

import android.content.Context;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;

public abstract class StreamAPI {
    protected EventStreamAPI ESApi = null;
    protected String API_KEY = null;
    protected String API_URL = null;
    protected String MODEL_NAME = null;

    public boolean hasApiKey() {
        return API_KEY != null;
    }

    public void setApiKey(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public void setESApi(EventStreamAPI ESApi) {
        this.ESApi = ESApi;
    }


    public String getUrl() {
        return API_URL;
    }


    //输入地址和模型，用于判断是否使用本API
    public boolean bind(String host, String path, String model) {
        return false;
    }
    //默认bind
    public void bind() {

    }
    //自定义带UI的bind
    public void bind(Context ctx) {

    }

    //由它创建提交
    public void post(HttpURLConnection conn, boolean stream, String fim_prefix, String fim_suffix, Object... others) throws JSONException, IOException {

    }

    //负责做接收处理
    public void recv(BufferedReader bufferd_reader, boolean stream) throws IOException, JSONException {

    }

}
