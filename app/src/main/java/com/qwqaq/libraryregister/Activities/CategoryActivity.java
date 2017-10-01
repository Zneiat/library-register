package com.qwqaq.libraryregister.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.qwqaq.libraryregister.Adapters.CategoryAdapter;
import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.Beans.BookBean;
import com.qwqaq.libraryregister.Beans.CategoryBean;
import com.qwqaq.libraryregister.Http;
import com.qwqaq.libraryregister.Utils.DisplayUtil;
import com.qwqaq.libraryregister.R;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

public class CategoryActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView mListView;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView(R.layout.category_activity);
    }

    @Override
    protected void initView(int layoutResID) {
        super.initView(layoutResID);

        // 若还未设置登记员名字
        if (App.Data.getRegistrarName().equals("")) {
            showRegistrarNameEditDialog();
        }

        mListView = (ListView) findViewById(R.id.category_list_view);
        mAdapter = new CategoryAdapter(this, App.Data.Basic);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mListView.setAdapter(mAdapter);
    }

    /**
     * 单击项目操作
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        final CategoryBean category = App.Data.Basic.get(i);

        if (category.getBooks().size() == 0) {
            /*
             * 云端获取图书数据
             */
            final ProgressDialog progressDialog = new ProgressDialog(CategoryActivity.this);
            progressDialog.setMessage("从云端下载数据中...");
            progressDialog.show();
            // 从云端下载 并 App.Data.Basic.getBooks().addAll(...)
            (new Http(getApplicationContext())).downloadBook(getApplicationContext(), category.getName(), new StringCallback() {
                @Override
                public void onResponse(String response, int id) {
                    progressDialog.cancel();

                    startWorking(i);
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    progressDialog.cancel();
                    showMsg("云端 " + category.getName() + " 类图书数据下载失败");
                }
            });
        } else {
            /*
             * 用本地数据
             */
            startWorking(i);
        }
    }

    /**
     * 长按项目操作
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

        final CategoryBean category = App.Data.Basic.get(i);
        if (!category.getCanDelete()) {
            showMsg("若要删除该类目，请直接来跟我讲");
            return true;
        }

        showConfirm("删除图书类目", "是否确定删除该类图书？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (App.Data.Local.containsKey(category.getName())) {
                    App.Data.Local.remove(category.getName());
                }
                App.Data.Basic.remove(i);
                mAdapter.notifyDataSetChanged();
                App.Data.dataPrefStore();
                showMsg("类目 " + category.getName() + " 删除成功");
            }
        });

        return true;
    }

    /**
     * 打开编辑器，开始工作
     */
    public void startWorking(int categoryBeansLocalPosition) {
        // 打开 Activity
        Intent intent = new Intent(CategoryActivity.this, EditorActivity.class);

        // 传参
        Bundle bundle = new Bundle();
        bundle.putInt("DataBasicIndex", categoryBeansLocalPosition);
        intent.putExtras(bundle);

        startActivity(intent);
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

        if (id == R.id.action_create) {
            showCreateCategoryDialog();
        }
        if (id == R.id.action_upload) {
            showConfirm("上传数据", "在上传数据到云端之前，需先确认本地编辑数据正确无误，是否继续？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    actionUploadDataToCloud();
                }
            });
        }
        if (id == R.id.action_download) {
            showConfirm("下载数据", "云端下载的数据会覆盖本地数据，所以若本地数据已修改，需先上传后才能执行此操作，不然会造成数据丢失，是否继续？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    actionCloudCategoryDataDownload();
                }
            });
        }
        if (id == R.id.action_edit_registrar_name) {
            showRegistrarNameEditDialog();
        }

        return true;
    }

    /**
     * 登记员名字修改
     */
    private void showRegistrarNameEditDialog() {
        final TextEditDialog editDialog = new TextEditDialog(this, "Your Name ?", "填入你的名字来和本程序签订契约");

        // 填入原来的名字
        String beforeName = App.Data.getRegistrarName();
        editDialog.getEditTextView().setText(beforeName);
        editDialog.getEditTextView().setSelection(beforeName.length());

        // 设置结束编辑按钮
        editDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 后来新的名字
                String newName = editDialog.getText();
                App.Data.setRegistrarName(newName);
            }
        });

        editDialog.show();
    }

    /**
     * 创建类目对话框
     */
    private void showCreateCategoryDialog() {
        final TextEditDialog editDialog = new TextEditDialog(this, "新建类目", "填入图书类目名，例如：Z");

        editDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editDialog.getText();
                if (text.length() <= 0) {
                    showMsg("类目名不能为空");
                    return;
                }
                createCategory(text);
            }
        });

        editDialog.show();
    }

    /**
     * 新建一个类目
     */
    private void createCategory(String name) {
        name = name.trim();

        // 是否有同名类目
        for (CategoryBean item : App.Data.Basic) {
            if (item.getName().trim().equals(name)) {
                showMsg("与 " + name +" 同名类目已存在，无需重复创建");
                return;
            }
        }

        CategoryBean categoryBean = new CategoryBean();
        categoryBean.setName(name);
        categoryBean.setRegistrarName(App.Data.getRegistrarName());

        BookBean newBook = new BookBean();
        newBook.setNumbering(1);
        newBook.setName("");
        newBook.setPress("");
        newBook.setRemarks("");

        categoryBean.getBooks().add(0, newBook);

        mAdapter.add(categoryBean);
        App.Data.Local.put(categoryBean.getName(), categoryBean);
        App.Data.dataPrefStore();

        showMsg("类目 " + name + " 创建成功");
    }

    /**
     * 下载云端类目数据（不含图书的）
     */
    public void actionCloudCategoryDataDownload() {
        final ProgressDialog progressDialog = new ProgressDialog(CategoryActivity.this);
        progressDialog.setMessage("正在从云端下载数据...");
        progressDialog.show();

        (new Http(getApplicationContext())).downloadCategory(new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
                progressDialog.cancel();
                mAdapter.notifyDataSetChanged();
                showMsg("云端类目数据下载成功");
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                progressDialog.cancel();
                showMsg("云端类目数据下载失败");
            }
        });
    }

    /**
     * 上传数据到云端
     */
    public void actionUploadDataToCloud() {
        final ProgressDialog progressDialog = new ProgressDialog(CategoryActivity.this);
        progressDialog.setMessage("正在上传数据到云端...");
        progressDialog.show();

        (new Http(getApplicationContext())).updateBook(getApplicationContext(), new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
                progressDialog.cancel();
                showMsg("数据上传到云端成功");
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                progressDialog.cancel();
                showMsg("数据上传到云端失败");
            }
        });
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
            showMsg("再使劲摁一次二话不说就离家出走");
        }
    }
}