package com.LlamaLuaEditor.cookiebar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Eric on 2017/3/2.
 */
final class Cookie extends LinearLayout {

    public static final String TAG = "Cookie";

    private Animation slideInAnimation;
    private Animation slideOutAnimation;

    private LinearLayout layoutCookie;
    private TextView tvTitle;
    private TextView tvMessage;
    private ImageView ivIcon;
    private TextView btnAction;
    private ImageView btnActionWithIcon;
    private long duration = 2000;
    private int layoutGravity = Gravity.BOTTOM;

    public void setTitle(final String title) {
        if (tvTitle != null) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTitle.setText(title);
                }
            });
        }
    }

    public void setMessage(final String message) {
        if (tvMessage != null) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessage.setText(message);
                }
            });
        }
    }

    public Cookie(final Context context) {
        this(context, null);
    }

    public Cookie(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Cookie(final Context context, final AttributeSet attrs,
                  final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    public int getLayoutGravity() {
        return layoutGravity;
    }

    //    private void initViews(Context context) {
//        inflate(getContext(), R.layout.layout_cookie, this);
//
//        layoutCookie = (LinearLayout) findViewById(R.id.cookie);
//        tvTitle = (TextView) findViewById(R.id.tv_title);
//        tvMessage = (TextView) findViewById(R.id.tv_message);
//        ivIcon = (ImageView) findViewById(R.id.iv_icon);
//        btnAction = (TextView) findViewById(R.id.btn_action);
//        btnActionWithIcon = (ImageView) findViewById(R.id.btn_action_with_icon);
//        initDefaultStyle(context);
//    }
    private void initViews(Context context) {
        // Create the main LinearLayout
        layoutCookie = new LinearLayout(context);
        layoutCookie.setClickable(true);
        layoutCookie.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutCookie.setPadding(16, 40, 16, 16);
        layoutCookie.setGravity(Gravity.CENTER_VERTICAL);
        layoutCookie.setOrientation(LinearLayout.HORIZONTAL);
        layoutCookie.setBackgroundColor(default_bg_color);

        // Create ImageView for the icon
        ivIcon = new ImageView(context);
        ivIcon.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ivIcon.setVisibility(View.GONE);
        ((LinearLayout.LayoutParams) ivIcon.getLayoutParams()).setMargins(0, 0, 16, 0);

        // Create a LinearLayout for the title and message
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                0, // Weight-based width
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Weight is 1
        textContainer.setOrientation(LinearLayout.VERTICAL);

        // Create TextView for the title
        tvTitle = new TextView(context);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvTitle.setText("Title");
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        tvTitle.setMaxLines(1);
        tvTitle.setVisibility(View.GONE);

        // Create TextView for the message
        tvMessage = new TextView(context);
        tvMessage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) tvMessage.getLayoutParams()).setMargins(0, 8, 0, 0);
        tvMessage.setText("message...");
        tvMessage.setTextColor(Color.BLACK);
        tvMessage.setTextSize(14);
        tvMessage.setVisibility(View.GONE);

        // Add title and message to the text container
        textContainer.addView(tvTitle);
        textContainer.addView(tvMessage);

        // Create TextView for the action button
        btnAction = new TextView(context);
        btnAction.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ((LinearLayout.LayoutParams) btnAction.getLayoutParams()).setMargins(24, 0, 0, 0);
        btnAction.setText("ACTION");
        btnAction.setTextColor(default_action_color);
        btnAction.setTextSize(14);
        btnAction.setTypeface(btnAction.getTypeface(), Typeface.BOLD);
        btnAction.setGravity(Gravity.CENTER);
        btnAction.setVisibility(View.GONE);

        // Create ImageView for the action button with icon
        btnActionWithIcon = new ImageView(context);
        btnActionWithIcon.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) btnActionWithIcon.getLayoutParams()).setMargins(24, 0, 0, 0);
        btnActionWithIcon.setVisibility(View.GONE);

        // Add all views to the main layout
        layoutCookie.addView(ivIcon);
        layoutCookie.addView(textContainer);
        layoutCookie.addView(btnAction);
        layoutCookie.addView(btnActionWithIcon);

        // Add the main layout to the parent view or activity
        // Assuming 'this' is an Activity or a ViewGroup
        addView(layoutCookie);

        // Initialize default styles
        initDefaultStyle(context);
    }

    public static final int default_bg_color = 0xBDBDBD;
    public static final int default_title_color = 0xffffff;
    public static final int default_message_color = 0xffffff;
    public static final int default_action_color = 0x26a69a;
    public static final int activity_horizontal_margin = 16;
    public static final int activity_vertical_margin = 16;
    public static final int default_padding = 16;

    private void initDefaultStyle(Context context) {
        //Custom the default style of a cookie
        int titleColor = Color.BLACK;
        int messageColor = Color.BLACK;
        int actionColor = Color.BLACK;
        int backgroundColor = default_bg_color;

        tvTitle.setTextColor(titleColor);
        tvMessage.setTextColor(messageColor);
        btnAction.setTextColor(actionColor);
        layoutCookie.setBackgroundColor(backgroundColor);
    }

    public void setParams(final CookieBar.Params params) {
        if (params != null) {
            duration = params.duration;
            layoutGravity = params.layoutGravity;

            //Icon
            if (params.iconResId != 0) {
                ivIcon.setVisibility(VISIBLE);
                ivIcon.setBackgroundResource(params.iconResId);
            }
            //IconDrawable
            if (params.iconDrawable != null) {
                ivIcon.setVisibility(VISIBLE);
                ivIcon.setBackground(params.iconDrawable);
            }

            //Title
            if (!TextUtils.isEmpty(params.title)) {
                tvTitle.setVisibility(VISIBLE);
                tvTitle.setText(params.title);
                if (params.titleColor != 0) {
                    tvTitle.setTextColor(params.titleColor);
                }
            }

            //Message
            if (!TextUtils.isEmpty(params.message)) {
                tvMessage.setVisibility(VISIBLE);
                tvMessage.setText(params.message);
                if (params.messageColor != 0) {
                    tvMessage.setTextColor(params.messageColor);
                }

                if (TextUtils.isEmpty(params.title)) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvMessage
                            .getLayoutParams();
                    layoutParams.topMargin = 0;
                }
            }

            //Action
            if ((!TextUtils.isEmpty(params.action) || params.actionIcon != 0)
                    && params.onActionClickListener != null) {
                btnAction.setVisibility(VISIBLE);
                btnAction.setText(params.action);
                btnAction.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        params.onActionClickListener.onClick();
                        try {
                            dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });

                //Action Color
                if (params.actionColor != 0) {
                    btnAction.setTextColor(params.actionColor);
                }
            }

            if (params.actionIcon != 0 && params.onActionClickListener != null) {
                btnAction.setVisibility(GONE);
                btnActionWithIcon.setVisibility(VISIBLE);
                btnActionWithIcon.setBackgroundResource(params.actionIcon);
                btnActionWithIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        params.onActionClickListener.onClick();
                        try {
                            dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            //Background
            if (params.backgroundColor != 0) {
                layoutCookie.setBackgroundColor(params.backgroundColor);
            }

            int padding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, default_padding, getResources().getDisplayMetrics());
            layoutCookie.setPadding(padding, padding, padding, padding);


            createInAnim();
            createOutAnim();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (layoutGravity == Gravity.TOP) {
            super.onLayout(changed, l, 0, r, layoutCookie.getMeasuredHeight());
        } else {
            super.onLayout(changed, l, t, r, b);
        }
        if (layoutGravity == Gravity.BOTTOM) {
            DisplayMetrics realMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);
            int realHeight = realMetrics.heightPixels;
            layoutCookie.setY(realHeight - layoutCookie.getMeasuredHeight() * 2);
        } else {
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            layoutCookie.setY(statusBarHeight);
        }
    }

    private void createInAnim() {
        if (layoutGravity == Gravity.BOTTOM) {
            // 从屏幕底部移入
            slideInAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 1, // 从屏幕下方开始
                    Animation.RELATIVE_TO_PARENT, 0); // 移动到其最终位置
        } else {
            // 从屏幕顶部移入
            slideInAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, -1, // 从屏幕上方开始
                    Animation.RELATIVE_TO_PARENT, 0); // 移动到其最终位置
        }
        slideInAnimation.setDuration(700);
        slideInAnimation.setInterpolator(new OvershootInterpolator());

        slideInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (duration != -1) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    }, duration);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        setAnimation(slideInAnimation);
    }

    private void createOutAnim() {
        if (layoutGravity == Gravity.BOTTOM) {
            // Animation for sliding out to the bottom of the parent view
            slideOutAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 1);
        } else {
            // Animation for sliding out to the top of the parent view
            slideOutAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, -1);
        }


        slideOutAnimation.setDuration(500);
        slideOutAnimation.setFillAfter(true);
        slideOutAnimation.setInterpolator(new AnticipateInterpolator());
        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void dismiss() {
        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                destroy();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
            }
        });
        startAnimation(slideOutAnimation);
    }

    private void destroy() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewParent parent = getParent();
                if (parent != null && isAttachedToWindow()) {
                    Cookie.this.clearAnimation();
                    ((ViewGroup) parent).removeView(Cookie.this);
                }
            }
        }, 200);
    }
}
