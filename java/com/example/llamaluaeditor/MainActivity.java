package com.example.llamaluaeditor;

import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.LlamaLuaEditor.LlamaHttp;
import com.LlamaLuaEditor.LlamaLuaEditor;
import com.LlamaLuaEditor.StreamAPI;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    public LlamaLuaEditor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("AI·Lua补全(在线模型)");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) ||
                    (checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                        "android.permission.WRITE_EXTERNAL_STORAGE",
                        "android.permission.READ_EXTERNAL_STORAGE"
                }, 0);
            }
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        editor = new LlamaLuaEditor(this);
        setContentView(editor);
        editor.setText("do\n--打印 你好世界\n\nend");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    new LlamaHttp().test();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem actionButton = menu.add(Menu.NONE, 1, Menu.NONE, "重做");
        actionButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        actionButton = menu.add(Menu.NONE, 2, Menu.NONE, "撤销");
        actionButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        actionButton = menu.add(Menu.NONE, 3, Menu.NONE, "打开");
        actionButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case 1: {
                editor.redo();
                return true;
            }
            case 2: {
                editor.undo();
                return true;
            }
            case 3: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // 可以根据需要指定文件类型，例如 "text/plain" 只选择文本文件
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "选择文件"), PICK_FILE_REQUEST);
                } catch (android.content.ActivityNotFoundException ex) {
                    // 提示用户没有安装文件管理器
                    Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    // 读取文件内容
                    StringBuilder stringBuilder = new StringBuilder();
                    try (InputStream inputStream = getContentResolver().openInputStream(uri);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                    }
                    editor.setText(stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "无法读取文件", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static final int PICK_FILE_REQUEST = 1;
}
