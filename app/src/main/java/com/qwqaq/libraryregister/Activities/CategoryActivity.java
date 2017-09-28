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
import com.qwqaq.libraryregister.Beans.BookBean;
import com.qwqaq.libraryregister.Beans.CategoryBean;
import com.qwqaq.libraryregister.Http;
import com.qwqaq.libraryregister.Kernel;
import com.qwqaq.libraryregister.Utils.DisplayUtil;
import com.qwqaq.libraryregister.R;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

public class CategoryActivity extends Activity {

    private ListView mListView;
    private ArrayList<CategoryBean> mCategoryBeansBasic;
    private HashMap<String, CategoryBean> mCategoryBeansLocal;
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
        if (Kernel.Data.getRegistrarName().equals("")) {
            editRegistrarNameAction();
        }

        mCategoryBeansBasic = Kernel.Data.Basic;
        mCategoryBeansLocal = Kernel.Data.Local;

        mListView = (ListView) findViewById(R.id.category_list_view);
        mAdapter = new CategoryAdapter(this, mCategoryBeansBasic);

        CategoryItemOnClickEvent onClickEvent = new CategoryItemOnClickEvent();
        mListView.setOnItemClickListener(onClickEvent);
        mListView.setOnItemLongClickListener(onClickEvent);

        mListView.setAdapter(mAdapter);
    }

    /**
     * 登记员名字修改
     */
    private void editRegistrarNameAction() {
        final EditTextDialog editDialog = new EditTextDialog();
        editDialog.show("Your Name ?", "填入你的名字来和本程序签订契约", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 后来新的名字
                String newName = editDialog.getText();
                Kernel.Data.setRegistrarName(newName);
            }
        });
        // 附加弹出编辑窗内容
        // 原来的名字
        String beforeName = Kernel.Data.getRegistrarName();
        editDialog.getTextEditView().setText(beforeName);
        editDialog.getTextEditView().setSelection(beforeName.length());
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
            final CategoryBean category = mCategoryBeansBasic.get(i);

            // 限制访问
            if (!category.getRegistrarName().equals(Kernel.Data.getRegistrarName())) {
                showMsg("由" + category.getRegistrarName() + "全权负责，以免发生混乱所以禁止编辑");
                return;
            }

            if (category.getBooks().size() == 0) {

                /**
                 * 云端获取图书数据
                 */
                final ProgressDialog progressDialog = new ProgressDialog(CategoryActivity.this);
                progressDialog.setMessage("从云端下载数据中...");
                progressDialog.show();
                // 从云端下载 并 Kernel.Data.Basic.getBooks().addAll(...)
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
                // 本地数据
                startWorking(i);
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

            final CategoryBean category = mCategoryBeansBasic.get(i);
            if (!category.getCanDelete()) {
                showMsg("若要删除该类目，请直接来跟我讲");
                return true;
            }

            showYesOrNoDialog("是否确定删除该类图书？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mCategoryBeansLocal.containsKey(category.getName())) {
                        mCategoryBeansLocal.remove(category.getName());
                    }
                    mCategoryBeansBasic.remove(i);
                    mAdapter.notifyDataSetChanged();
                    Kernel.Data.dataPrefStore();
                    showMsg("类目 " + category.getName() + " 删除成功");
                }
            });

            return true;
        }
    }

    /**
     * 关键 > 图书数据已准备好了 开始工作
     */
    public void startWorking(int categoryBeansLocalPosition) {
        CategoryBean basicBean = mCategoryBeansBasic.get(categoryBeansLocalPosition);

        // 数据准备
        // 关键 > Basic Beans 设置到 Local Beans 的过程
        if (basicBean.getBooks().size() > 0) {
            // 将最后一个项目作为开始编辑的第一个项目（编辑进度修改）
            basicBean.setBookEditStartIndex(basicBean.getBooks().size() - 1);
        }
        mCategoryBeansLocal.put(basicBean.getName(), basicBean);

        // 打开 Activity
        Intent intent = new Intent(CategoryActivity.this, EditorActivity.class);

        // 传参
        Bundle bundle = new Bundle();
        bundle.putString("CategoryName", basicBean.getName());
        bundle.putInt("CategoryInBasicBeansPosition", categoryBeansLocalPosition);
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
            showMsg("再使劲摁一次二话不说就离家出走");
        }
    }

    /**
     * 可编辑文本的对话框
     */
    public class EditTextDialog {
        public EditText editTextView;
        public AlertDialog.Builder alertDialog;

        public void show(String title, String msg, DialogInterface.OnClickListener okClickListener) {
            alertDialog = new AlertDialog.Builder(CategoryActivity.this);
            if (title.trim().length() > 0) {
                alertDialog.setTitle(title);
            }
            alertDialog.setMessage(msg);
            int leftRightPadding = DisplayUtil.dipToPx(CategoryActivity.this, 20);
            int topBottomPadding = DisplayUtil.dipToPx(CategoryActivity.this, 10);
            editTextView = new EditText(CategoryActivity.this);
            editTextView.setSingleLine(true);
            editTextView.setGravity(Gravity.CENTER);
            alertDialog.setView(editTextView, leftRightPadding, topBottomPadding, leftRightPadding, topBottomPadding);

            alertDialog.setPositiveButton("确定", okClickListener);
            alertDialog.setNegativeButton("取消", null);
            alertDialog.show();
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
            addDialog();
        }
        if (id == R.id.action_upload) {
            showYesOrNoDialog("在上传数据到云端之前，需先确认本地编辑数据正确无误，是否继续？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    uploadDataToCloud();
                }
            });
        }
        if (id == R.id.action_download) {
            showYesOrNoDialog("云端下载的数据会覆盖本地数据，所以若本地数据已修改，需先上传后才能执行此操作，不然会造成数据丢失，是否继续？", new DialogInterface.OnClickListener() {
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
     * 新建类目对话框
     */
    private void addDialog() {
        final EditTextDialog editDialog = new EditTextDialog();
        editDialog.show("新建类目", "填入图书类目名，例如：Z", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editDialog.getText();
                if (text.trim().length() <= 0) {
                    showMsg("类目名不能为空");
                    return;
                }
                addCategory(text);
            }
        });
    }

    /**
     * 新建一个类目
     */
    private void addCategory(String name) {
        name = name.trim();
        for (CategoryBean item : mCategoryBeansBasic) {
            if (item.getName().trim().equals(name)) {
                showMsg("与 " + name +" 同名类目已存在，无需重复创建");
                return;
            }
        }
        CategoryBean categoryBean = new CategoryBean();
        categoryBean.setName(name);
        categoryBean.setRegistrarName(Kernel.Data.getRegistrarName());

        BookBean newBook = new BookBean();
        newBook.setNumbering(1);
        newBook.setName("");
        newBook.setPress("");
        newBook.setRemarks("");
        categoryBean.getBooks().add(0, newBook);

        mAdapter.add(categoryBean);
        mCategoryBeansLocal.put(categoryBean.getName(), categoryBean);

        Kernel.Data.dataPrefStore();

        showMsg("类目 " + name + " 创建成功");
    }

    /**
     * 下载云端类目数据（不含图书的）
     */
    public void cloudCategoryDataDownload() {
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
    public void uploadDataToCloud() {

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
}