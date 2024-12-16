package com.LlamaLuaEditor.apis;

import com.LlamaLuaEditor.StreamAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class GLM4FlashStreamAPI extends StreamAPI {

    //输入地址和模型，用于判断是否使用本API
    public boolean bind(String host, String path, String model) {
        if ("open.bigmodel.cn".equals(host)) {//path=/api/paas/v4/chat/completions
            API_URL = "https://" + host + path;
            MODEL_NAME = model;
            return true;
        }
        return false;
    }

    public void bind() {
        API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        MODEL_NAME = "glm-4-flash";
    }


    public void post(HttpURLConnection conn, boolean stream, String fim_prefix, String fim_suffix, Object... others) throws JSONException, IOException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        JSONObject jsonObject = new JSONObject();
        JSONArray messages = new JSONArray();
        jsonObject.put("messages", messages);
        JSONObject msg = new JSONObject();
        msg.put("role", "system");
        msg.put("content", "你是一位智能编程助手，负责根据提示生成缺省位置的代码，你的回答不需要使用代码块包装直接输出即可，也只需要输出补全位置的代码，如果某个变量关键字只打了前几个字，补全时也只需要从之后的几个字开始输出，如for已经有f就只从or开始输出。");
        messages.put(msg);
        msg = new JSONObject();
        msg.put("role", "user");
        msg.put("content", "<|code_suffix|>" +
                fim_prefix
                + "<|code_prefix|>" +
                fim_suffix
                + "</|code_middle|>");
        messages.put(msg);
        jsonObject.put("model", MODEL_NAME);
        jsonObject.put("do_sample", true);
        jsonObject.put("stream", stream);
        jsonObject.put("temperature", 0.95);
        jsonObject.put("top_p", 0.7);
        jsonObject.put("max_tokens", 4095);
        jsonObject.put("stop", null);
        byte[] input = jsonObject.toString().getBytes("UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(input, 0, input.length);
        os.flush();
        os.close();
    }

    public void recv(BufferedReader bufferd_reader, boolean stream) throws IOException, JSONException {
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = bufferd_reader.readLine()) != null) {
            if (ESApi != null && ESApi.isAbort()) break;
            if (stream) {
                if (line.startsWith("data: ")) {
                    if (line.startsWith("data: [DONE]")) {
                        ESApi.onFinished();
                        break;
                    }
                    line = line.substring(6).trim();
                    JSONObject jsonObject = new JSONObject(line);
                    JSONArray jsonArray = jsonObject.getJSONArray("choices");
                    jsonObject = jsonArray.getJSONObject(0);
                    jsonObject = jsonObject.getJSONObject("delta");
                    String text = jsonObject.getString("content");
                    if (ESApi != null) {
                        ESApi.onNewContent(text);
                    }
                }
            } else {
                sb.append(line);
            }
        }
        if (!stream) {
            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("choices");
            jsonObject = jsonArray.getJSONObject(0);
            jsonObject = jsonObject.getJSONObject("message");
            String text = jsonObject.getString("content");
            if (ESApi != null) {
                ESApi.onNewContent(text);
                ESApi.onFinished();
            }
        }
    }
}