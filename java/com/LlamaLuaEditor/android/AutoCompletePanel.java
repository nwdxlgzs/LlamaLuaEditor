package com.LlamaLuaEditor.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.LlamaLuaEditor.EventStreamAPI;
import com.LlamaLuaEditor.LlamaHttp;
import com.LlamaLuaEditor.LlamaLuaEditor;
import com.LlamaLuaEditor.StreamAPI;
import com.LlamaLuaEditor.common.DocumentProvider;
import com.LlamaLuaEditor.common.Flag;
import com.LlamaLuaEditor.common.Language;
import com.LlamaLuaEditor.common.LanguageNonProg;
import com.LlamaLuaEditor.common.Lexer;
import com.LlamaLuaEditor.common.AluaParser;
import com.LlamaLuaEditor.common.PackageUtil;
import com.LlamaLuaEditor.cookiebar.CookieBar;
import com.LlamaLuaEditor.cookiebar.OnActionClickListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoCompletePanel {

    private final DisplayMetrics dm;
    private FreeScrollingTextField _textField;
    private Context _context;
    private static Language _globalLanguage = LanguageNonProg.getInstance();
    private ListPopupWindow _autoCompletePanel;
    private AutoCompletePanel.MyAdapter _adapter;
    private Filter _filter;

    private int _verticalOffset;

    private int _height;

    private int _horizontal;

    private CharSequence _constraint;

    private int _backgroundColor;

    private GradientDrawable gd;

    private int _textColor;
    private Drawable _background;
    private int _width;

    public AutoCompletePanel(FreeScrollingTextField textField) {
        _textField = textField;
        _context = textField.getContext();
        dm = _context.getResources().getDisplayMetrics();
        initAutoCompletePanel();

    }

    public void setTextColor(int color) {
        _textColor = color;
        gd.setStroke(dp(1), color);
        _autoCompletePanel.setBackgroundDrawable(gd);
    }

    public void setBackgroundColor(int color) {
        _backgroundColor = color;
        gd.setColor(color);
        _autoCompletePanel.setBackgroundDrawable(gd);
    }

    public void setBackground(Drawable color) {
        _background = color;
        //_autoCompletePanel.setBackgroundDrawable(color);
    }

    public void infill(int position) {
        final long StartTime = System.currentTimeMillis();
        final LlamaLuaEditor editor = (LlamaLuaEditor) _textField;
        final StreamAPI mStreamAPI = editor.getStreamAPI();
        if (mStreamAPI == null||!mStreamAPI.hasApiKey()) {
            editor.setStreamAPIByDialog();
            return;
        }
        final Activity activity = (Activity) _context;
        final DocumentProvider docp = _textField.createDocumentProvider();
        final String fim_prefix = (String) docp.subSequence(0, position + 1);
        final String fim_suffix = (String) docp.subSequence(position + 1, docp.docLength() - position - 2);
        final Boolean[] isAbort_infill = {false};
        final CookieBar mCookieBar = new CookieBar.Builder(activity)
                .setTitle("AI补全")
                .setMessage("请求中")
                .setBackgroundColor(0xfffafafa)
                .setLayoutGravity(Gravity.BOTTOM)
                .setDuration(-1)
                .setIconDrawable(activity.getDrawable(android.R.drawable.ic_menu_edit))
                .setAction("强制结束", new OnActionClickListener() {
                    @Override
                    public void onClick() {
                        isAbort_infill[0] = true;
                    }
                })
                .show();
        // 获取当前焦点视图
        final IBinder token = activity.getCurrentFocus() != null ? activity.getCurrentFocus().getWindowToken() : null;
        if (token != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final boolean stream = true;
                try {
                    mStreamAPI.setESApi(new EventStreamAPI() {
                        @Override
                        public void onFinished() {
                            mCookieBar.dismiss();
                        }

                        @Override
                        public void onNewContent(String s) {
                            if (s == null) s = "";
                            long secUsed = (System.currentTimeMillis() - StartTime) / 1000;
                            mCookieBar.setMessage("代码补全中（" + secUsed + "s）:" + s);
                            if (s.length() > 0) {
                                final String code = s;
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        editor.paste(code);
                                    }
                                });
                            }
                        }

                        @Override
                        public boolean isAbort() {
                            return isAbort_infill[0];
                        }
                    });
                    URL url = new URL(mStreamAPI.getUrl());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    mStreamAPI.post(conn, stream, fim_prefix, fim_suffix);
                    conn.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    mStreamAPI.recv(br, stream);
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        final long StartTime = System.currentTimeMillis();
//        final LlamaHttp http = new LlamaHttp();
//
//        Runnable task = new Runnable() {
//            @Override
//            public void run() {
//                if (isAbort_infill[0]) return;
//                    mCookieBar.setMessage("代码补全任务发起中……");
//
//                    try {
//                        final long InfillStartTime = System.currentTimeMillis();
//                        http.Qweninfill(inputPrefix, inputSuffix, null, ctx, new LlamaHttp.StreamAPI() {
//
//                            @Override
//                            public boolean isAbort() {
//                                return isAbort_infill[0];
//                            }
//
//                            @Override
//                            public void onFinished() {
//                                mCookieBar.dismiss();
//                            }
//
//                            private StringBuilder headStr = new StringBuilder();
//                            private int codeArea = 0;
//                            private StringBuilder totalStr = null;
//
//                            @Override
//                            public void onNewContent(String s) {
//                                long fsecUsed = (System.currentTimeMillis() - InfillStartTime) / 1000;
//                                mCookieBar.setMessage("代码补全中（" + fsecUsed + "s）:" + s);
//                                if (s == null) return;
//                                if (headStr != null) {
//                                    headStr.append(s);
//                                    if ("```lua\n".equals(headStr.toString())) {
//                                        headStr = null;
//                                    }
//                                    return;
//                                }
//                                if (s.length() > 0) {
//                                    //拦截代码块
//                                    if ("```".equals(s) || "``".equals(s) || "`".equals(s)) {
//                                        codeArea = s.length();
//                                        return;
//                                    }
//                                    //如果代码块后面有内容那就还原代码块
//                                    if (codeArea >= 3) {
//                                        s = String.join("", Collections.nCopies(codeArea, "`")) + s;
//                                        codeArea = 0;
//                                    }
//                                    final boolean firstpaste = totalStr == null;
//                                    if (firstpaste) {
//                                        totalStr = new StringBuilder().append(s);
//                                    } else {
//                                        totalStr.append(s);
//                                    }
//                                    activity.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (!firstpaste) {
//                                                editor.undo();
//                                            }
//                                            editor.paste(totalStr.toString());
//                                        }
//                                    });
//
//                                }
//                            }
//                        });
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//                    executor.shutdown();
//            }
//        };
//        executor.scheduleWithFixedDelay(task, 0, 300, TimeUnit.MILLISECONDS);
    }

    private void initAutoCompletePanel() {
        _autoCompletePanel = new ListPopupWindow(_context);
        _autoCompletePanel.setAnchorView(_textField);
        _adapter = new MyAdapter(_context, android.R.layout.simple_list_item_1);
        _autoCompletePanel.setAdapter(_adapter);
        //_autoCompletePanel.setDropDownGravity(Gravity.BOTTOM | Gravity.LEFT);
        _filter = _adapter.getFilter();
        setHeight(300);

        TypedArray array = _context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground,
                android.R.attr.textColorPrimary,
        });
        int backgroundColor = array.getColor(0, 0xFF00FF);
        int textColor = array.getColor(1, 0xFF00FF);
        array.recycle();
        gd = new GradientDrawable();
        gd.setColor(backgroundColor);
        gd.setCornerRadius(dp(4));
        gd.setStroke(dp(1), textColor);
        setTextColor(textColor);
        _autoCompletePanel.setBackgroundDrawable(gd);
        _autoCompletePanel.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
                // TODO: Implement this method
                String text = ((TextView) p2).getText().toString();
                if (p3 == 0 && "[AI补全]".equals(text)) {
                    infill(_textField.getCaretPosition() - _constraint.length());
                    _adapter.abort();
                    dismiss();
                } else {
                    int i = text.indexOf(":");
                    if (i > 0) {
                        text = text.substring(0, i - 1);
                    }
                    _textField.replaceText(_textField.getCaretPosition() - _constraint.length(), _constraint.length(), text);
                    _adapter.abort();
                    dismiss();
                }
            }
        });

    }

    private int dp(float n) {
        // TODO: Implement this method
        int ret = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, dm);
        if (ret <= 0)
            return (int) n;
        return ret;
    }

    public void setWidth(int width) {
        // TODO: Implement this method
        _autoCompletePanel.setWidth(width);
        ListView listView = _autoCompletePanel.getListView();
        if (listView != null) {
            ViewGroup.LayoutParams lp = listView.getLayoutParams();
            lp.width = width;
            listView.setLayoutParams(lp);
        }
        _width = width;
    }

    private void setHeight(int height) {
        // TODO: Implement this method

        if (_height != height) {
            _height = height;
            _autoCompletePanel.setHeight(height);
        }
    }

    private void setHorizontalOffset(int horizontal) {
        // TODO: Implement this method
        horizontal = Math.min(horizontal, _textField.getWidth() / 2);
        if (_horizontal != horizontal) {
            _horizontal = horizontal;
            _autoCompletePanel.setHorizontalOffset(horizontal);
        }
    }


    private void setVerticalOffset(int verticalOffset) {
        // TODO: Implement this method
        //verticalOffset=Math.min(verticalOffset,_textField.getWidth()/2);
        int max = 0 - _autoCompletePanel.getHeight();
        if (verticalOffset > max) {
            _textField.scrollBy(0, verticalOffset - max);
            verticalOffset = max;
        }
        if (_verticalOffset != verticalOffset) {
            _verticalOffset = verticalOffset;
            _autoCompletePanel.setVerticalOffset(verticalOffset);
        }
    }

    public void update(CharSequence constraint) {
        _adapter.restart();
        _filter.filter(constraint);
    }

    public void show() {
        if (!_autoCompletePanel.isShowing()) {
            _autoCompletePanel.show();
            ListView list = _autoCompletePanel.getListView();
            if (list != null) {
                list.setFadingEdgeLength(0);
                if (_background != null)
                    list.setBackground(_background);
            }
        }
    }

    public void dismiss() {
        if (_autoCompletePanel.isShowing()) {
            _autoCompletePanel.dismiss();
        }
    }

    synchronized public void setLanguage(Language lang) {
        _globalLanguage = lang;
    }

    synchronized public static Language getLanguage() {
        return _globalLanguage;
    }

    /**
     * Adapter定义
     */
    class MyAdapter extends ArrayAdapter<CharSequence> implements Filterable {

        private int _h;
        private Flag _abort;

        private DisplayMetrics dm;

        public MyAdapter(android.content.Context context, int resource) {
            super(context, resource);
            _abort = new Flag();
            setNotifyOnChange(false);
            dm = context.getResources().getDisplayMetrics();

        }

        public void abort() {
            _abort.set();
        }


        private int dp(float n) {
            // TODO: Implement this method
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, dm);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO: Implement this method
            TextView view = (TextView) super.getView(position, convertView, parent);
            /*TextView view=null;
            if(convertView==null){
				 view=new TextView(_context);
				 view.setTextSize(16);
				 view.setPadding(dp(8),dp(3),dp(8),dp(3));
			}
			else{
				view=(TextView) convertView;
			}
			view.setText(getItem(position));*/

            view.setWidth(_width);
            view.setSingleLine(false);
            view.setTextColor(_textColor);
            return view;
        }


        public void restart() {
            // TODO: Implement this method
            _abort.clear();
        }

        public int getItemHeight() {
            if (_h != 0)
                return _h;

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView item = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
            item.measure(0, 0);
            _h = item.getMeasuredHeight();
            return _h;
        }

        /**
         * 实现自动完成的过滤算法
         */
        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                /**
                 * 本方法在后台线程执行，定义过滤算法
                 */
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    // 此处实现过滤
                    // 过滤后利用FilterResults将过滤结果返回
                    ArrayList<CharSequence> buf = new ArrayList<>();
                    buf.add(getColorText("[AI补全]", 0xFFB29600));
                    String key = String.valueOf(constraint);
                    String keyword = key.toLowerCase();
                    String[] ss = keyword.split("\\.");
                    keyword = ss[ss.length - 1];
                    if (key.endsWith(".")) {
                        _constraint = "";
                    } else {
                        _constraint = keyword;
                    }
                    int position = _textField.getCaretPosition() - key.length() - 1;
                    if (ss.length == 2) {
                        String pkg = ss[0];
                        if (_globalLanguage.isBasePackage(pkg)) {
                            String[] keywords = _globalLanguage.getBasePackage(pkg);
                            for (String k : keywords) {
                                if (k.toLowerCase().startsWith(keyword))
                                    buf.add(getColorText(k, _textField.getColorScheme().getTokenColor(Lexer.NAME)));
                            }
                        } else {
                            buf.addAll(AluaParser.filterJava(pkg, keyword, position));
                        }
                    } else if (ss.length == 1) {
                        if (key.charAt(key.length() - 1) == '.') {
                            //String pkg = keyword.substring(0, keyword.length() - 1);
                            if (_globalLanguage.isBasePackage(keyword)) {
                                String[] keywords = _globalLanguage.getBasePackage(keyword);
                                for (String k : keywords) {
                                    buf.add(getColorText(k, _textField.getColorScheme().getTokenColor(Lexer.NAME)));
                                }
                            } else {
                                buf.addAll(AluaParser.filterJava(keyword, position));
                            }
                        } else {
                            buf.addAll(AluaParser.filterLocal(keyword, position, _textField.getColorScheme()));
                            ArrayList<String> usewords = AluaParser.getUserWord();
                            for (String k : usewords) {
                                if (k.toLowerCase().startsWith(keyword))
                                    buf.add(getColorText(k, _textField.getColorScheme().getTokenColor(Lexer.LITERAL)));
                            }
                            String[] keywords = _globalLanguage.getKeywords();
                            for (String k : keywords) {
                                if (k.indexOf(keyword) == 0)
                                    buf.add(getColorText(k, _textField.getColorScheme().getTokenColor(Lexer.KEYWORD)));
                            }
                            keywords = _globalLanguage.getNames();
                            for (String k : keywords) {
                                if (k.toLowerCase().startsWith(keyword))
                                    buf.add(getColorText(k, _textField.getColorScheme().getTokenColor(Lexer.NAME)));
                            }
                        }
                    }

                    buf.addAll(PackageUtil.filter(key));
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = buf;   // results是上面的过滤结果
                    filterResults.count = buf.size();  // 结果数量
                    //Log.i("luaj", "performFiltering: "+key+";"+buf);
                    return filterResults;
                }

                private CharSequence getColorText(String text, int color) {
                    SpannableString ss = new SpannableString(text);
                    ss.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
                    return ss;
                }

                /**
                 * 本方法在UI线程执行，用于更新自动完成列表
                 */
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0 && !_abort.isSet()) {
                        // 有过滤结果，显示自动完成列表
                        MyAdapter.this.clear();   // 清空旧列表
                        MyAdapter.this.addAll((ArrayList<CharSequence>) results.values);
                        //int y = _textField.getPaintBaseline(_textField.getCaretRow()) - _textField.getScrollY();
                        int y = _textField.getCaretY() + _textField.rowHeight() / 2 - _textField.getScrollY();
                        setHeight(getItemHeight() * Math.min(3, results.count));
                        //setHeight((int)(Math.min(_textField.getContentHeight()*0.4,getItemHeight() * Math.min(6, results.count))));

                        setHorizontalOffset(_textField.getCaretX() - _textField.getScrollX());
                        setVerticalOffset(y - _textField.getHeight());//_textField.getCaretY()-_textField.getScrollY()-_textField.getHeight());
                        notifyDataSetChanged();
                        show();
                    } else {
                        // 无过滤结果，关闭列表
                        notifyDataSetInvalidated();
                    }
                }

            };
            return filter;
        }
    }
}
