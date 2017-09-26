package com.qwqaq.schoollibrarysignuptask.Activities;

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
    private View mLoadingProgress;
    private ArrayList<CategoryBean> mCategoryBeans;
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

        mCategoryBeans = Kernel.gCategoryBeans;
        mListView = (ListView) findViewById(R.id.category_list_view);
        mLoadingProgress = (View) findViewById(R.id.loading_progress);
        mAdapter = new CategoryAdapter(this, mCategoryBeans);

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
        for (CategoryBean item : mCategoryBeans) {
            if (item.getName().trim().equals(name)) {
                showMsg("同名类目已存在，无需重复创建");
                return;
            }
        }
        CategoryBean category = new CategoryBean();
        category.setName(name);
        category.setRegistrarName(Kernel.getRegistrarName());
        mAdapter.add(category);
        Kernel.localSaveCategoryData();
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
     * 类目单个项目点按事件
     */
    private class CategoryItemOnClickEvent implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(CategoryActivity.this, SignupActivity.class);

            final CategoryBean category = mCategoryBeans.get(i);
            if (!category.getRegistrarName().equals(Kernel.getRegistrarName())) {
                showMsg("本类目由 " + category.getRegistrarName() + " 全权负责\n以免发生混乱 禁止编辑");
                return;
            }

            // 传参
            Bundle bundle = new Bundle();
            bundle.putInt("CategoryBeanIndex", i);
            intent.putExtras(bundle);

            startActivity(intent);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            final CategoryBean category = mCategoryBeans.get(i);
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

        if (Kernel.isNetworkAvailable(getApplicationContext())) {
            // 显示加载
            mLoadingProgress.setVisibility(View.VISIBLE);
            // 下发请求指令
            OkHttpUtils
                    .get()
                    .url(Kernel.URL_CATEGORY_RES)
                    // .addParams("user", "")
                    .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            mLoadingProgress.setVisibility(View.GONE);
                            showMsg("厉害了... 数据下载失败，请检查网络连接 \n" + e.getMessage());
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            mLoadingProgress.setVisibility(View.GONE);
                            ArrayList<CategoryBean> categories = new ArrayList<CategoryBean>();
                            try {
                                JSONObject json = new JSONObject(response);
                                if (!json.getBoolean("success")) {
                                    showMsg(json.getString("msg"));
                                    return;
                                }
                                String dataJsonArr = json.getString("data");
                                categories = (new Gson()).fromJson(dataJsonArr, new TypeToken<ArrayList<CategoryBean>>() {}.getType());
                                for (int i=0; i < categories.size(); i++) {
                                    CategoryBean item = categories.get(i);
                                    item.setCanDelete(false); // 不允许删除
                                    item.setIsUpload(true); // 设置告知内容已在云端
                                    categories.set(i, item);
                                }
                            } catch (Exception e) {
                                showMsg("出现一个野生的错误\n" + e.getMessage());
                                Log.e("请求云端数据下载", "出现错误", e);
                            }

                            mAdapter.addAll(categories);
                            Kernel.localSaveCategoryData();
                        }
                    });
        } else {
            showMsg("当前处于离线工作状态，无力从云端下载数据");
        }
    }

    /**
     * 云端数据上传
     */
    public void cloudDataUpload() {

    }
}