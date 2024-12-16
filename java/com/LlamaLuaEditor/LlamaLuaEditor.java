package com.LlamaLuaEditor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import android.widget.RadioGroup.*;
import android.widget.TextView.*;

import com.LlamaLuaEditor.android.*;
import com.LlamaLuaEditor.common.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_COPY;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CUT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_PASTE;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_SELECTION;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT;

public class LlamaLuaEditor extends FreeScrollingTextField {

    private Document _inputtingDoc;

    private boolean _isWordWrap;

    private Context mContext;

    private String _lastSelectedFile;
    private int _index;
    private LinearSearchStrategy finder;
    private int idx;
    private String mKeyword;
    private StreamAPI mStreamAPI = null;

    public StreamAPI getStreamAPI() {
        return mStreamAPI;
    }

    public void setStreamAPI(StreamAPI mStreamAPI) {
        this.mStreamAPI = mStreamAPI;
    }

    public StreamAPI findStreamAPI(String host, String path, String model) {
        return new LlamaHttp().findStreamAPI(host, path, model);
    }

    //    public void setStreamAPIByDialog() {
//        // 创建LinearLayout
//        final LinearLayout layout = new LinearLayout(mContext);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        layout.setPadding(16, 16, 16, 16);
//
//        // 创建EditText for Host
//        final EditText etHost = new EditText(mContext);
//        etHost.setHint("请求主机");
//        layout.addView(etHost);
//
//        // 创建EditText for Path
//        final EditText etPath = new EditText(mContext);
//        etPath.setHint("请求路径");
//        layout.addView(etPath);
//
//        // 创建EditText for Model
//        final EditText etModel = new EditText(mContext);
//        etModel.setHint("模型");
//        layout.addView(etModel);
//
//        // 创建EditText for Key
//        final EditText etKey = new EditText(mContext);
//        etKey.setHint("接口秘钥");
////        etKey.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
//        layout.addView(etKey);
//
//        // 创建AlertDialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle("请输入参数");
//        builder.setView(layout);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String host = etHost.getText().toString();
//                String path = etPath.getText().toString();
//                String model = etModel.getText().toString();
//                String key = etKey.getText().toString();
//
//                if (host.isEmpty() || path.isEmpty() || model.isEmpty() || key.isEmpty()) {
//                    Toast.makeText(mContext, "请输入所有参数", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                StreamAPI api = findStreamAPI(host, path, model);
//                if (api != null) {
//                    setStreamAPI(api);
//                }
//
//            }
//        });
//        builder.setNegativeButton("取消", null);
//        builder.show();
//    }
    public void setStreamAPIByDialog() {
        final LlamaHttp llamaHttp = new LlamaHttp();
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        final Spinner spinnerModel = new Spinner(mContext);
        final List<String> modelList = Arrays.asList(llamaHttp.sapis_names);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, modelList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(adapter);
        layout.addView(spinnerModel);
        final EditText etKey = new EditText(mContext);
        etKey.setHint("接口秘钥");
        layout.addView(etKey);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("请选择AI服务");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final StreamAPI selectedModel = llamaHttp.sapis[spinnerModel.getSelectedItemPosition()];
                if (selectedModel != null) {
                    selectedModel.bind();
                    final String key = etKey.getText().toString();
                    selectedModel.setApiKey(key);
                    setStreamAPI(selectedModel);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }


    @SuppressLint("StaticFieldLeak")
    public LlamaLuaEditor(final Context context) {
        super(context);
        mContext = context;
        setTypeface(Typeface.MONOSPACE);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, BASE_TEXT_SIZE_PIXELS, dm);
        setTextSize((int) size);
        setShowLineNumbers(true);
        setHighlightCurrentRow(true);
        setWordWrap(false);
        setAutoIndentWidth(2);
        Lexer.setLanguage(LanguageLua.getInstance());
        if (isAccessibilityEnabled())
            setNavigationMethod(new TrackpadNavigationMethod(this));
        else
            setNavigationMethod(new YoyoNavigationMethod(this));
        TypedArray array = mContext.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground,
                android.R.attr.textColorPrimary,
                android.R.attr.textColorHighlight,
        });
        int backgroundColor = array.getColor(0, 0xFF00FF);
        int textColor = array.getColor(1, 0xFF00FF);
        int textColorHighlight = array.getColor(2, 0xFF00FF);
        array.recycle();
        setTextColor(textColor);
        setTextHighlightColor(textColorHighlight);
        /*
        new AsyncTask<String, String, String[]>(){
            @Override
            protected String[] doInBackground(String... strings) {
                String[] cls = LuaUtil.getAllName(context, libDir);
                for (int i = 0; i < cls.length; i++) {
                    String cl = cls[i];
                    int d = cl.lastIndexOf("$");
                    if(d<0)
                      d = cl.lastIndexOf(".");
                    if(d>0)
                        cls[i]=cl.substring(d);
                }
                 return cls;
            }

            @Override
            protected void onPostExecute(String[] cls) {
                addNames(cls);
            }
        }.execute();*/
        PackageUtil.load(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO: Implement this method
        super.onLayout(changed, left, top, right, bottom);
        if (_index != 0 && right > 0) {
            moveCaret(_index);
            _index = 0;
        }
    }

    public void setDark(boolean isDark) {
        if (isDark)
            setColorScheme(new ColorSchemeDark());
        else
            setColorScheme(new ColorSchemeLight());
    }

    public void addNames(String[] names) {
        LanguageLua lang = (LanguageLua) Lexer.getLanguage();
        String[] old = lang.getNames();
        String[] news = new String[old.length + names.length];
        System.arraycopy(old, 0, news, 0, old.length);
        System.arraycopy(names, 0, news, old.length, names.length);
        lang.setNames(news);
        Lexer.setLanguage(lang);
        respan();
        invalidate();
    }

    public void addPackage(String pkg, String[] names) {
        LanguageLua lang = (LanguageLua) Lexer.getLanguage();
        lang.addBasePackage(pkg, names);
        Lexer.setLanguage(lang);
        respan();
        invalidate();
    }

    public void removePackage(String pkg) {
        LanguageLua lang = (LanguageLua) Lexer.getLanguage();
        lang.removeBasePackage(pkg);
        Lexer.setLanguage(lang);
        respan();
        invalidate();
    }

    public void setPanelBackgroundColor(int color) {
        // TODO: Implement this method
        _autoCompletePanel.setBackgroundColor(color);
    }

    public void setPanelTextColor(int color) {
        // TODO: Implement this method
        _autoCompletePanel.setTextColor(color);
    }

    public void setKeywordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.KEYWORD, color);
    }

    public void setUserwordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.LITERAL, color);
    }

    public void setBasewordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.NAME, color);
    }

    public void setStringColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.STRING, color);
    }

    public void setCommentColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.COMMENT, color);
    }

    public void setBackgroundColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.BACKGROUND, color);
    }

    public void setTextColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.FOREGROUND, color);
    }

    public void setTextHighlightColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.SELECTION_BACKGROUND, color);
    }

    public String getSelectedText() {
        // TODO: Implement this method
        return _hDoc.subSequence(getSelectionStart(), getSelectionEnd() - getSelectionStart()).toString();
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        final int filteredMetaState = event.getMetaState() & ~KeyEvent.META_CTRL_MASK;
        if (KeyEvent.metaStateHasNoModifiers(filteredMetaState)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                    selectAll();
                    return true;
                case KeyEvent.KEYCODE_X:
                    cut();
                    return true;
                case KeyEvent.KEYCODE_C:
                    copy();
                    return true;
                case KeyEvent.KEYCODE_V:
                    paste();
                    return true;
                case KeyEvent.KEYCODE_L:
                    format();
                    return true;
                case KeyEvent.KEYCODE_S:
                    search();
                    return true;
                case KeyEvent.KEYCODE_G:
                    gotoLine();
                    return true;
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

    public void gotoLine() {
        // TODO: Implement this method
        startGotoMode();
    }

    public void search() {
        // TODO: Implement this method
        startFindMode();
    }

    public void startGotoMode() {
        // TODO: Implement this method
        startActionMode(new ActionMode.Callback() {

            private int idx;

            private EditText edit;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO: Implement this method
                mode.setTitle("转到");
                mode.setSubtitle(null);

                edit = new EditText(mContext) {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) {
                            idx = 0;
                            _gotoLine();
                        }
                    }

                };

                edit.setSingleLine(true);
                edit.setInputType(2);
                edit.setImeOptions(2);
                edit.setOnEditorActionListener(new OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView p1, int p2, KeyEvent p3) {
                        // TODO: Implement this method
                        _gotoLine();
                        return true;
                    }
                });
                edit.setLayoutParams(new LayoutParams(getWidth() / 3, -1));
                menu.add(0, 1, 0, "").setActionView(edit);
                menu.add(0, 2, 0, mContext.getString(android.R.string.ok));
                edit.requestFocus();
                return true;
            }

            private void _gotoLine() {
                String s = edit.getText().toString();
                if (s.isEmpty())
                    return;

                int l = Integer.valueOf(s);
                if (l > _hDoc.getRowCount()) {
                    l = _hDoc.getRowCount();
                }
                gotoLine(l);
                // TODO: Implement this method
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO: Implement this method
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO: Implement this method
                switch (item.getItemId()) {
                    case 1:
                        break;
                    case 2:
                        _gotoLine();
                        break;

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode p1) {
                // TODO: Implement this method
            }
        });

    }

    public void startFindMode() {
        // TODO: Implement this method
        startActionMode(new ActionMode.Callback() {

            private LinearSearchStrategy finder;

            private int idx;

            private EditText edit;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO: Implement this method
                mode.setTitle("搜索");
                mode.setSubtitle(null);

                edit = new EditText(mContext) {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) {
                            idx = 0;
                            findNext();
                        }
                    }
                };
                edit.setSingleLine(true);
                edit.setImeOptions(3);
                edit.setOnEditorActionListener(new OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView p1, int p2, KeyEvent p3) {
                        // TODO: Implement this method
                        findNext();
                        return true;
                    }
                });
                edit.setLayoutParams(new LayoutParams(getWidth() / 3, -1));
                menu.add(0, 1, 0, "").setActionView(edit);
                menu.add(0, 2, 0, mContext.getString(android.R.string.search_go));
                edit.requestFocus();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO: Implement this method
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO: Implement this method
                switch (item.getItemId()) {
                    case 1:
                        break;
                    case 2:
                        findNext();
                        break;

                }
                return false;
            }

            private void findNext() {
                // TODO: Implement this method
                finder = new LinearSearchStrategy();
                String kw = edit.getText().toString();
                if (kw.isEmpty()) {
                    selectText(false);
                    return;
                }
                idx = finder.find(getText(), kw, idx, getText().length(), false, false);
                if (idx == -1) {
                    selectText(false);
                    Toast.makeText(mContext, "未找到", Toast.LENGTH_SHORT).show();
                    idx = 0;
                    return;
                }
                setSelection(idx, edit.getText().length());
                idx += edit.getText().length();
                moveCaret(idx);
            }

            @Override
            public void onDestroyActionMode(ActionMode p1) {
                // TODO: Implement this method
            }
        });

    }

    @Override
    public void setWordWrap(boolean enable) {
        // TODO: Implement this method
        _isWordWrap = enable;
        super.setWordWrap(enable);
    }

    public DocumentProvider getText() {
        return createDocumentProvider();
    }

    public void setText(CharSequence c) {
        //TextBuffer text=new TextBuffer();
        Document doc = new Document(this);
        doc.setWordWrap(_isWordWrap);
        doc.setText(c);
        setDocumentProvider(new DocumentProvider(doc));
        //doc.analyzeWordWrap();
    }

    public void insert(int idx, String text) {
        selectText(false);
        moveCaret(idx);
        paste(text);
        //_hDoc.insert(idx,text);
    }

    public void setText(CharSequence c, boolean isRep) {
        replaceText(0, getLength() - 1, c.toString());
    }

    public void setSelection(int index) {
        selectText(false);
        if (!hasLayout())
            moveCaret(index);
        else
            _index = index;
    }

    public void gotoLine(int line) {
        if (line > _hDoc.getRowCount()) {
            line = _hDoc.getRowCount();
        }
        int i = getText().getLineOffset(line - 1);
        setSelection(i);
    }

    public void undo() {
        DocumentProvider doc = createDocumentProvider();
        int newPosition = doc.undo();

        if (newPosition >= 0) {
            setEdited(true);
            respan();
            selectText(false);
            moveCaret(newPosition);
            invalidate();
        }

    }

    public void redo() {
        DocumentProvider doc = createDocumentProvider();
        int newPosition = doc.redo();

        if (newPosition >= 0) {
            setEdited(true);

            respan();
            selectText(false);
            moveCaret(newPosition);
            invalidate();
        }
    }

    public String getFilePath() {
        return _lastSelectedFile;
    }

    public void open(String filename) throws IOException {
        _lastSelectedFile = filename;
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            buf.append(line).append("\n");
        if (buf.length() > 1)
            buf.setLength(buf.length() - 1);
        setText(buf);
        /*
        File inputFile = new File(filename);
        _inputtingDoc = new Document(this);
        _inputtingDoc.setWordWrap(this.isWordWrap());
        ReadTask _taskRead = new ReadTask(this, inputFile);
        _taskRead.start();*/
    }

    public boolean save() throws IOException {
        return save(_lastSelectedFile);
    }

    public boolean save(String filename) throws IOException {
        if (filename == null)
            return true;
        File outputFile = new File(filename);

        if (outputFile.exists()) {
            if (!outputFile.canWrite()) {
                return false;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(getText().toString());
        writer.close();
        return true;
    }

    public boolean findNext(String keyword) {
        if (!keyword.equals(mKeyword)) {
            mKeyword = keyword;
            idx = 0;
        }
        // TODO: Implement this method
        finder = new LinearSearchStrategy();
        String kw = mKeyword;
        if (kw.isEmpty()) {
            selectText(false);
            return false;
        }
        idx = finder.find(getText(), kw, idx, getText().length(), false, false);
        if (idx == -1) {
            selectText(false);
            Toast.makeText(mContext, "未找到", Toast.LENGTH_SHORT).show();
            idx = 0;
            return false;
        }
        setSelection(idx, mKeyword.length());
        idx += mKeyword.length();
        moveCaret(idx);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {

        switch (action) {
            case AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY:
                switch (arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT)) {
                    case AccessibilityNodeInfo.MOVEMENT_GRANULARITY_PAGE:
                        return false;
                    case AccessibilityNodeInfo.MOVEMENT_GRANULARITY_LINE:
                        moveCaretDown();
                        break;
                    case AccessibilityNodeInfo.MOVEMENT_GRANULARITY_CHARACTER:
                        if (getCaretPosition() >= getLength() - 1)
                            return false;
                        moveCaretRight();
                        break;
                }
                return true;
            case ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY:
                switch (arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT)) {
                    case AccessibilityNodeInfo.MOVEMENT_GRANULARITY_PAGE:
                        return false;
                    case AccessibilityNodeInfo.MOVEMENT_GRANULARITY_LINE:
                        moveCaretUp();
                        break;
                    case AccessibilityNodeInfo.MOVEMENT_GRANULARITY_CHARACTER:
                        if (getCaretPosition() <= 0)
                            return false;
                        moveCaretLeft();
                        return true;
                }
                return true;
            case ACTION_SET_SELECTION:
                if (arguments == null)
                    return true;
                int start = arguments.getInt(ACTION_ARGUMENT_SELECTION_START_INT, 0);
                int end = arguments.getInt(ACTION_ARGUMENT_SELECTION_END_INT, 0);
                boolean sel = arguments.getBoolean(ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN, false);
                if (sel)
                    setSelectionRange(start, end);
                else
                    setSelection(start);
                return true;
            case ACTION_SET_TEXT:
                selectText(false);
                if (arguments == null)
                    setText("", true);
                else
                    setText(arguments.getCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE), true);
                return true;
            case ACTION_PASTE:
                paste();
                return true;
            case ACTION_COPY:
                copy();
                return true;
            case ACTION_CUT:
                cut();
                return true;
        }
        return super.performAccessibilityAction(action, arguments);
    }

}
