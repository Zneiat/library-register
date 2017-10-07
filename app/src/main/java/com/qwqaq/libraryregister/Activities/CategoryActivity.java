package com.qwqaq.libraryregister.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qwqaq.libraryregister.adapters.CategoryAdapter;
import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.beans.BookBean;
import com.qwqaq.libraryregister.beans.CategoryBean;
import com.qwqaq.libraryregister.Http;
import com.qwqaq.libraryregister.R;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;

public class CategoryActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public ListView mListView;
    public CategoryAdapter mAdapter;

    public static final int EDITOR_ACTIVITY_CODE = 601;
    public static final int JSON_EDITOR_ACTIVITY_CODE = 602;

    // 用于存放 CSV 表格的目录路径
    public static final String CSV_DATA_DIR_PATH = "/QwqBookRegister";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 权限 检测/申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请 WRITE_EXTERNAL_STORAGE 权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        // 若还未设置登记员名字
        if (App.Data.getRegistrarName().equals("")) {
            showRegistrarNameEditDialog();
        }

        initView(R.layout.category_activity);
    }

    @Override
    protected void initView(int layoutResID) {
        super.initView(layoutResID);

        mListView = (ListView) findViewById(R.id.category_list_view);
        mAdapter = new CategoryAdapter(this, App.Data.Basic);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mListView.setAdapter(mAdapter);

        dataEmptyCheck();
    }

    // 离线提醒
    private boolean offlineNotification = true;

    /**
     * 数据为空检测
     */
    private void dataEmptyCheck() {
        // 数据为空，但是网络可以用
        if (!App.isNetworkAvailable(this) && offlineNotification) {
            Snackbar.make(mContentArea, "离线状态，相关云端操作已禁用", Snackbar.LENGTH_LONG)
                    .setAction("不再提醒", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            offlineNotification = false;
                        }
                    }).show();
        }
        if (App.Data.Basic.size() < 1 && App.isNetworkAvailable(this)) {
            showMsg("看起来似乎什么都没有呀~\n正在从云端下载数据");
            cloudGetCategories();
        }
    }

    /**
     * 单击项目操作
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        final CategoryBean category = App.Data.Basic.get(i);
        int categorySize = category.getBooks().size();
        boolean isMine = category.getRegistrarName().trim().equals(App.Data.getRegistrarName().trim());

        // 有图书数据 并且 是自己的，或有数据待上传到云端
        if ((categorySize > 0 && isMine) || App.Data.Local.containsKey(category.getName())) {
            startWorking(i);
            return;
        }

        // 有图书数据 并且 不是自己的，在 网络无法连接 情况下
        if (categorySize > 0 && !isMine && !App.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络无法连接，当前类目数据可能不是最新的...", Toast.LENGTH_LONG).show();
            startWorking(i);
            return;
        }

        // 无图书数据 或 不是自己的类目，在 网络可连接 情况下
        // 从云端下载图书数据 并 App.Data.Basic.getBooks().addAll(...)
        cloudGetCategoryBooks(category.getName(), new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
                startWorking(i);
            }

            @Override
            public void onError(Call call, Exception e, int id) {}
        });
    }

    /**
     * 长按项目操作
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

        displayCategoryActionsDialog(i);

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

        startActivityForResult(intent, EDITOR_ACTIVITY_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_activity_top_tool_bar_menu, menu);
        super.onCreateOptionsMenu(menu);
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
                    cloudUploadCategoryBooks();
                }
            });
        }
        if (id == R.id.action_download) {
            showConfirm("下载数据", "云端下载的数据会覆盖本地数据，所以若本地数据已修改，需先上传后才能执行此操作，不然会造成数据丢失，是否继续？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cloudGetCategories();
                }
            });
        }
        if (id == R.id.action_edit_registrar_name) {
            showRegistrarNameEditDialog();
        }
        if (id == R.id.action_json_editor) {
            actionStartJsonEditor();
        }
        if (id == R.id.action_about) {
            displayAboutDialog();
        }

        return true;
    }

    /**
     * 登记员名字修改
     */
    private void showRegistrarNameEditDialog() {
        final TextEditDialog editDialog = new TextEditDialog(this, "Your Name ?", "填入你的名字和我签订契约，成为名副其实的图书登记员吧！");

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

                dataEmptyCheck();
            }
        });

        editDialog.show();
    }

    /**
     * 创建类目对话框
     */
    private void showCreateCategoryDialog() {
        if (App.Data.getRegistrarName().trim().length() < 1) {
            showMsg("请先和本程序签订契约");
            return;
        }

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
        categoryBean.setCanDelete(false);
        categoryBean.setCreatedAt(new Date().getTime() + "");

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
     * 删除一个类目
     */
    private void deleteCategory(final int categoryIndex) {
        final CategoryBean category = App.Data.Basic.get(categoryIndex);

        showConfirm("删除图书类目", "是否确定删除该类图书？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!category.getRegistrarName().equals(App.Data.getRegistrarName())) {
                    showMsg("对该类目下手... 你做不到");return;
                }
                if (!category.getCanDelete()) {
                    showMsg("因该类目已上传到云端，若需删除该类目，可向我报告");return;
                }

                if (App.Data.Local.containsKey(category.getName())) {
                    App.Data.Local.remove(category.getName());
                }
                App.Data.Basic.remove(categoryIndex);
                mAdapter.notifyDataSetChanged();
                App.Data.dataPrefStore();
                showMsg("类目 " + category.getName() + " 删除成功");
            }
        });
    }

    /**
     * 从云端获取所有类目（包括图书）
     */
    public void cloudGetCategories() {
        (new Http(this)).getCategories();
    }

    /**
     * 从云端获取单个类目所有图书
     */
    public void cloudGetCategoryBooks(final String categoryName, final StringCallback callbackEvent) {
        (new Http(this)).getCategoryBooks(categoryName, new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
                callbackEvent.onResponse(response, id);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                callbackEvent.onError(call, e, id);
            }
        });
    }

    /**
     * 同上 - 从云端获取单个类目所有图书
     */
    public void cloudGetCategoryBooks(final String categoryName) {
        (new Http(this)).getCategoryBooks(categoryName, new StringCallback() {
            @Override
            public void onResponse(String response, int id) {}

            @Override
            public void onError(Call call, Exception e, int id) {}
        });
    }

    /**
     * 上传数据到云端
     */
    public void cloudUploadCategoryBooks() {
        if (App.Data.getRegistrarName().trim().length() < 1) {
            showMsg("请先和本程序签订契约");
            return;
        }

        (new Http(this)).uploadCategoryBooks();
    }

    /**
     * 手动控制数据，JSON 编辑器
     */
    public void actionStartJsonEditor() {
        Intent intent = new Intent(CategoryActivity.this, JsonEditorActivity.class);

        // 传参
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);

        startActivityForResult(intent, JSON_EDITOR_ACTIVITY_CODE);
    }

    /**
     * 构建一个类目的 CSV 表格数据
     */
    private File buildCategoryBookCsvData(final int categoryIndex) throws Exception {
        CategoryBean category = App.Data.Basic.get(categoryIndex);
        File dir = new File(Environment.getExternalStorageDirectory(), CSV_DATA_DIR_PATH);
        if (!dir.exists() && !dir.mkdirs())
            throw new Exception("无法创建目录");
        File file = new File(dir, "/" + category.getName() + "类图书.csv");
        if (file.exists() && !file.delete())
            throw new Exception("无法删除原来的文件");
        if (!file.exists() && !file.createNewFile())
            throw new Exception("无法创建文件");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), "GBK");
        writer.write(App.Data.dataBasicBookDataToCsvStr(categoryIndex));
        writer.close();
        return file;
    }

    /**
     * 用其他 APP 打开类目 CSV 文件
     */
    private void openCategoryCsv(final int categoryIndex) {
        CategoryBean category = App.Data.Basic.get(categoryIndex);
        if (category.getBooks() == null || category.getBooks().size() < 1) {
            showMsg("该类目没有图书数据，无法生成表格文档");
            return;
        }

        File file;
        try {
            file = buildCategoryBookCsvData(categoryIndex);
        } catch (Exception e) {
            Log.d("", "", e);
            showMsg("Emm... 文件准备失败... 无法分享");
            return;
        }
        // 打开
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(App.getUriForFile(this, file), "text/*");
        startActivity(Intent.createChooser(intent, "选择 APP 打开表格文档"));
    }

    /**
     * 分享类目 CSV 文件
     */
    private void shareCategoryCsv(final int categoryIndex) {
        CategoryBean category = App.Data.Basic.get(categoryIndex);
        if (category.getBooks() == null || category.getBooks().size() < 1) {
            showMsg("该类目没有图书数据，无法生成表格文档");
            return;
        }

        File file;
        try {
            file = buildCategoryBookCsvData(categoryIndex);
        } catch (Exception e) {
            Log.d("", "", e);
            showMsg("Emm... 文件准备失败... 无法分享");
            return;
        }
        // 调用分享
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, App.getUriForFile(this, file));
        startActivity(Intent.createChooser(sharingIntent, "分享到"));
    }

    /**
     * 显示类目操作对话框
     */
    public void displayCategoryActionsDialog(final int categoryIndex) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final CategoryBean category = App.Data.Basic.get(categoryIndex);

        builder.setTitle("你想对 ‘类目 " + category.getName() + "’ 做什么？");
        builder.setItems(new String[]{"用其他 APP 打开", "作为文件分享", "删除"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        openCategoryCsv(categoryIndex);
                        break;
                    case 1:
                        shareCategoryCsv(categoryIndex);
                        break;
                    case 2:
                        deleteCategory(categoryIndex);
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * 显示 关于 对话框
     */
    public void displayAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("返回", null);
        builder.setNeutralButton("作者 GITHUB", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse("https://github.com/Zneiat");
                intent.setData(content_url);
                startActivity(Intent.createChooser(intent, "选择一个浏览器打开链接"));
            }
        });
        AlertDialog alertDialog = builder.create();
        View layout = LayoutInflater.from(this).inflate(R.layout.about_dialog, null);
        alertDialog.setView(layout);
        alertDialog.show();

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        ((TextView) alertDialog.findViewById(R.id.date_number)).setText(day+"");

        // 版本号
        String version = "";
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            version = " " + info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView title = (TextView) alertDialog.findViewById(R.id.about_title);
        title.setText(title.getText() + version);
    }

    /**
     * Activity 结束，获取响应结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDITOR_ACTIVITY_CODE:
                if (resultCode == EditorActivity.RESULT_CODE_SAVED) {
                    String msg = data.getStringExtra("ResultMessage");
                    if (msg != null && msg.length() > 0) showMsg(msg);
                    mAdapter.notifyDataSetChanged();
                }
                break;

            case JSON_EDITOR_ACTIVITY_CODE:
                mAdapter.notifyDataSetChanged();

            default:
                break;
        }

        dataEmptyCheck();
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
            showMsg("再使劲摁一次二话不说离家出走");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "QAQ 未获得存储权限将，程序无法运行呐~", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}