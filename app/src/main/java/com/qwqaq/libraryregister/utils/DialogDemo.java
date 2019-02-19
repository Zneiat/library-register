package com.qwqaq.libraryregister.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Button;

/**
 * Created by Zneia on 2017/9/28.
 */

public class DialogDemo {
    // 错误消息对话框,这是你自己写的方法，传参，设置参数。穿进去参数
    public static void builder(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", null);
        builder.show();// 学习方法
    }
    /*
     * 保存功能专用
     */
    public static void builder(final Context context, String title, String message,
                               final Button button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                button.performClick();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("不保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                Activity activity = (Activity) context;
                activity.finish();
            }
        });
        builder.show();// 学习方法
    }
    /*
     * 删除功能
     */
    public static void builder(Context context, String title, String message,
                               OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", onClickListener);
        builder.setNeutralButton("取消",null);
        builder.show();// 学习方法
    }

    /*
     * 多选列表功能
     */
    public static void builder(Context context, String title, String[] items,
                               OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        //builder.setMessage(message);
        builder.setItems(items, onClickListener);
        builder.show();// 学习方法
    }

};
