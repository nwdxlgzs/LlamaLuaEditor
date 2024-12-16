package com.LlamaLuaEditor.apis;

import com.LlamaLuaEditor.StreamAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class DeepSeekStreamAPI extends StreamAPI {

    //输入地址和模型，用于判断是否使用本API
    public boolean bind(String host, String path, String model) {
        if ("api.deepseek.com".equals(host)) {
//            if ("api.deepseek.com".equals(host) &&
//                    "/beta/completions".equals(path) &&
//                    ("deepseek-coder".equals(model) || "deepseek-chat".equals(model))) {
            API_URL = "https://" + host + path;
            MODEL_NAME = model;
            return true;
        }
        return false;
    }

    public void bind() {
        API_URL = "https://api.deepseek.com/beta/completions";
        MODEL_NAME = "deepseek-coder";
    }


    public void post(HttpURLConnection conn, boolean stream, String fim_prefix, String fim_suffix, Object... others) throws JSONException, IOException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("prompt", fim_prefix);
        jsonObject.put("suffix", fim_suffix);
        jsonObject.put("model", MODEL_NAME);
        jsonObject.put("echo", false);
        jsonObject.put("frequency_penalty", 0);
        jsonObject.put("logprobs", 0);
        jsonObject.put("max_tokens", 4095);
        jsonObject.put("presence_penalty", 0);
        jsonObject.put("stop", null);
        jsonObject.put("stream", stream);
        jsonObject.put("stream_options", null);
        jsonObject.put("temperature", 1);
        jsonObject.put("top_p", 1);
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
                    String text = jsonObject.getString("text");
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
            String text = jsonObject.getString("text");
            if (ESApi != null) {
                ESApi.onNewContent(text);
                ESApi.onFinished();
            }
        }
    }
}