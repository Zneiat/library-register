package com.qwqaq.schoollibrarysignuptask.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.schoollibrarysignuptask.Adapters.CategoryAdapter;
import com.qwqaq.schoollibrarysignuptask.Beans.CategoryBean;
import com.qwqaq.schoollibrarysignuptask.Kernel;
import com.qwqaq.schoollibrarysignuptask.Utils.DisplayUtil;
import com.qwqaq.schoollibrarysignuptask.R;
import com.qwqaq.schoollibrarysignuptask.Utils.HttpReqEvents;
import com.qwqaq.schoollibrarysignuptask.Utils.SoftInputUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.utils.Exceptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Call;

public class CategoryActivity extends Activity {

    private ListView mListView;
    private ArrayList<CategoryBean> mCategoryBeansCloud;
    private ArrayList<CategoryBean> mCategoryBeansLocal;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView(R.layout.category_activity);
    }

    @Override
    protected void initView(int layoutResID) {
        super.initView(layoutResID);

        // 设置标题
        getSupportActionBar().setTitle(getTitle());

        // 判断是否设置了登记员名字
        if (Kernel.getRegistrarName().equals("")) {
            editRegistrarNameAction();
        }

        mCategoryBeansCloud = Kernel.Data.CategoryBeansCloud;
        mCategoryBeansLocal = Kernel.Data.CategoryBeansLocal;

        mListView = (ListView) findViewById(R.id.category_list_view);
        mAdapter = new CategoryAdapter(this, mCategoryBeansCloud);

        CategoryItemOnClickEvent onClickEvent = new CategoryItemOnClickEvent();
        mListView.setOnItemClickListener(onClickEvent);
        mListView.setOnItemLongClickListener(onClickEvent);

        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.category_activity_top_tool_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            addAction();
        }
        if (id == R.id.action_upload) {

        }
        if (id == R.id.action_download) {
            showYesOrNoDialog("下载数据操作会覆盖本地数据，是否继续？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cloudCategoryDataDownload();
                }
            });
        }
        if (id == R.id.action_edit_registrar_name) {
            editRegistrarNameAction();
        }

        return true;
    }

    /**
     * 添加一个类目
     */
    private void addAction() {
        final EditTextDialog editDialog = new EditTextDialog();
        editDialog.show("填入图书类目，例如：Z", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editDialog.getText();
                addOne(text);
            }
        });
    }

    private void addOne(String name) {
        name = name.trim();
        for (CategoryBean item : mCategoryBeansCloud) {
            if (item.getName().trim().equals(name)) {
                showMsg("同名类目已存在，无需重复创建");
                return;
            }
        }
        CategoryBean category = new CategoryBean();
        category.setName(name);
        category.setRegistrarName(Kernel.getRegistrarName());

        mAdapter.add(category);
        mCategoryBeansLocal.add(category);

        Kernel.Data.categoryBeansStore();
    }

    /**
     * 登记员名字修改
     */
    private void editRegistrarNameAction() {

        final EditTextDialog editDialog = new EditTextDialog();
        editDialog.show("Your Name ? 填入你的名字", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editDialog.getText();
                Kernel.setRegistrarName(newName);
            }
        });
        String afterText = Kernel.getRegistrarName();
        editDialog.getTextEditView().setText(afterText);
        editDialog.getTextEditView().setSelection(afterText.length());
    }

    /**
     * 类目 单个项目点按事件
     */
    private class CategoryItemOnClickEvent implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

        /**
         * 点击 打开进行工作
         */
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
            final CategoryBean categoryCloud = mCategoryBeansCloud.get(i);

            // 限制访问
            if (!categoryCloud.getRegistrarName().equals(Kernel.getRegistrarName())) {
                showMsg("由" + categoryCloud.getRegistrarName() + "全权负责，以免发生混乱所以禁止编辑");
                return;
            }

            // 数据云端/本地获取
            if (categoryCloud.getBooks().size() == 0) {
                final ProgressDialog progressDialog = new ProgressDialog(CategoryActivity.this);
                progressDialog.setMessage("从云端下载数据中...");
                progressDialog.show();
                // 此方法会从云端下载 并 CategoryBeansCloud.getBooks().addAll(...)
                Kernel.Data.cloudDownloadBooks(getApplicationContext(), categoryCloud.getName(), new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        progressDialog.cancel();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        progressDialog.cancel();

                        // > CategoryBeansCloud 设置到 CategoryBeanLocal
                        CategoryBean cloudBean = Kernel.Data.CategoryBeansCloud.get(i);
                        cloudBean.setBookEditStartIndex(cloudBean.getBooks().size() - 1);
                        Kernel.Data.CategoryBeansLocal.add(i, cloudBean);
                        startWorking(i);
                    }
                });
            } else {
                CategoryBean cloudBean = Kernel.Data.CategoryBeansCloud.get(i);
                Kernel.Data.CategoryBeansLocal.set(i, cloudBean);
                startWorking(i);
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            final CategoryBean category = mCategoryBeansCloud.get(i);
            if (!category.getCanDelete()) {
                showMsg("若要删除该类目，请直接来告诉我");
                return true;
            }

            showYesOrNoDialog("是否确定删除该类图书？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showMsg("类目 " + category.getName() + " 删除成功");
                }
            });

            return true;
        }
    }

    /**
     * 图书数据已准备好了 开始工作
     */
    public void startWorking(int categoryBeansLocalPosition) {
        Intent intent = new Intent(CategoryActivity.this, SignupActivity.class);

        // 传参
        Bundle bundle = new Bundle();
        bundle.putInt("CategoryBeansLocalPosition", categoryBeansLocalPosition);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long TOUCH_TIME = 0;

    @Override
    public void onBackPressed() {
        // 连按两次返回键来 退出 APP
        if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
            this.finish();
        } else {
            TOUCH_TIME = System.currentTimeMillis();
            showMsg("再使劲摁一次离家出走");
        }
    }

    /**
     * 可编辑文本的对话框
     */
    public class EditTextDialog {
        public EditText editTextView;

        public void show(String msg, DialogInterface.OnClickListener okClickListener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
            builder.setMessage(msg);
            int leftRightPadding = DisplayUtil.dipToPx(CategoryActivity.this, 20);
            int topBottomPadding = DisplayUtil.dipToPx(CategoryActivity.this, 10);
            editTextView = new EditText(CategoryActivity.this);
            editTextView.setSingleLine(true);
            editTextView.setGravity(Gravity.CENTER);
            builder.setView(editTextView, leftRightPadding, topBottomPadding, leftRightPadding, topBottomPadding);

            builder.setPositiveButton("确定", okClickListener);
            builder.setNegativeButton("取消", null);
            builder.show();
        }

        public EditText getTextEditView() {
            return editTextView;
        }

        public String getText() {
            if (editTextView != null) {
                return editTextView.getText().toString().trim();
            } else {
                return null;
            }
        }
    }

    /**
     * 显示 是或否 确认对话框
     */
    public void showYesOrNoDialog(String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 显示一条消息
     */
    public void showMsg(String msg) {
        Snackbar.make(mListView, msg, Snackbar.LENGTH_LONG).show();
    }

    /**
     * 云端数据下载
     */
    public void cloudCategoryDataDownload() {
        mAdapter.clear(); // 首先删除所有数据

        final ProgressDialog progressDialog = new ProgressDialog(CategoryActivity.this);
        progressDialog.setMessage("从云端下载数据中...");
        progressDialog.show();

        Kernel.Data.cloudDownloadCategoryList(getApplicationContext(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                progressDialog.cancel();
            }

            @Override
            public void onResponse(String response, int id) {
                progressDialog.cancel();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 云端数据上传
     */
    public void cloudDataUpload() {

    }
}