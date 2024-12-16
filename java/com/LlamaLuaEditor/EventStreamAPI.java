package com.LlamaLuaEditor;

public interface EventStreamAPI {
    //结束
    public void onFinished();

    //解析完每次新内容再返回
    public void onNewContent(String s);

    //是否阻断
    public boolean isAbort();
}
