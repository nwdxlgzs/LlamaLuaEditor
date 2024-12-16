package com.LlamaLuaEditor;

import com.LlamaLuaEditor.apis.DeepSeekStreamAPI;
import com.LlamaLuaEditor.apis.GLM4FlashStreamAPI;
import com.LlamaLuaEditor.apis.MoonShotStreamAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LlamaHttp {
    public StreamAPI[] sapis = {
            new DeepSeekStreamAPI(),
            new GLM4FlashStreamAPI(),
            new MoonShotStreamAPI(),
    };
    public String[] sapis_names = {
            "深度求索API:deepseek-coder",
            "智谱清言API:GLM-4-Flash",
            "月之暗面(kimi)API:moonshot-v1-auto"
    };

    public StreamAPI findStreamAPI(String host, String path, String model) {
        StreamAPI mStreamAPI = null;
        for (StreamAPI sapi : sapis) {
            if (sapi.bind(host, path, model)) {
                mStreamAPI = sapi;
                break;
            }
        }
        return mStreamAPI;
    }
    public void test() throws IOException, JSONException {

    }
}
