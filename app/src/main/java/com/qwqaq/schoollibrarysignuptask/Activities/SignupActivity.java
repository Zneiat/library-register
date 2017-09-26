package com.qwqaq.schoollibrarysignuptask.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.schoollibrarysignuptask.Beans.CategoryBean;
import com.qwqaq.schoollibrarysignuptask.Kernel;
import com.qwqaq.schoollibrarysignuptask.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Call;

public class SignupActivity extends Activity {

    private CategoryBean mCategoryBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.signup_activity);

        // 接收参数
        Bundle bundle = this.getIntent().getExtras();
        int categoryBeanIndex = bundle.getInt("CategoryBeanIndex");
        mCategoryBean = Kernel.gCategoryBeans.get(categoryBeanIndex);

        getSupportActionBar().setTitle(mCategoryBean.getName());
        mTopToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFinish();
            }
        });
    }

    @Override
    protected void initView(int layoutResID) {
        super.initView(layoutResID);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.signup_activity_top_tool_bar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveData();
        }

        return true;
    }

    /**
     * 保存数据
     */
    public void saveData() {
        // TODO
        Toast.makeText(this, mCategoryBean.getName() + " 数据已保存", Toast.LENGTH_LONG).show();
    }

    /**
     * 结束编辑
     */
    public void changeFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否保存并返回主页？");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeFinish(true);
            }
        });
        builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeFinish(false);
            }
        });
        builder.setNeutralButton("取消", null);
        builder.show();
    }

    public void changeFinish(boolean enableSave) {
        if (enableSave) {
            saveData();
        }

        finish();
    }

    /**
     * 云端数据下载
     */
    public void cloudBooksDataDownload() {

    }
}