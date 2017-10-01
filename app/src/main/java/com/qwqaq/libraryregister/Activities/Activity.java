package com.qwqaq.libraryregister.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.R;
import com.qwqaq.libraryregister.Utils.DisplayUtil;


/**
 * Created by Zneia on 2017/9/15.
 */

public abstract class Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    |--------------------------------------------------------------------------
    | 关于 APP 中主界面的控制
    |--------------------------------------------------------------------------
    */

    protected View mContentArea;
    protected Toolbar mTopToolBar; // 顶部工具条

    protected void initView(int layoutResID) {
        setContentView(layoutResID);

        mContentArea = findViewById(R.id.content_area);
        // init TopToolBar
        mTopToolBar = (Toolbar) findViewById(R.id.top_tool_bar);
        setSupportActionBar(mTopToolBar);
    }

    /**
     * TopToolBar Options 创建
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 将每一个图标颜色改为白色
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            Drawable drawable = item.getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

    /**
     * TopToolBar Options 点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return true;
    }

    /**
     * 显示一条消息
     */
    protected void showMsg(String msg) {
        Snackbar.make(mContentArea, msg, Snackbar.LENGTH_LONG).show();
    }

    /**
     * 显示 是或否 确认对话框
     */
    protected void showConfirm(String title, String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title != null && title.trim().length() > 0) {
            builder.setTitle(title.trim());
        }
        builder.setMessage(msg.trim());
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 带文本编辑的对话框
     */
    protected class TextEditDialog extends AlertDialog.Builder {
        private EditText mEditTextView;

        public TextEditDialog(Context context, String title, String msg) {
            super(context);

            if (title.trim().length() > 0)
                this.setTitle(title);
            if (msg.trim().length() > 0)
                this.setMessage(msg);

            // EditTextView
            int leftRightPadding = DisplayUtil.dipToPx(getApplicationContext(), 20);
            int topBottomPadding = DisplayUtil.dipToPx(getApplicationContext(), 10);
            mEditTextView = new EditText(context);
            mEditTextView.setSingleLine(true);
            mEditTextView.setGravity(Gravity.CENTER);
            this.setView(mEditTextView, leftRightPadding, topBottomPadding, leftRightPadding, topBottomPadding);

            this.setNegativeButton("取消", null);

        }

        public void setPositiveButton(DialogInterface.OnClickListener listener) {
            this.setPositiveButton("完毕", listener);
        }

        public EditText getEditTextView() {
            return mEditTextView;
        }

        public String getText() {
            if (mEditTextView != null) {
                return mEditTextView.getText().toString().trim();
            }
            return "";
        }
    }
}