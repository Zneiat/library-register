package com.qwqaq.schoollibrarysignuptask.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.schoollibrarysignuptask.Beans.BookBean;
import com.qwqaq.schoollibrarysignuptask.Beans.CategoryBean;
import com.qwqaq.schoollibrarysignuptask.Kernel;
import com.qwqaq.schoollibrarysignuptask.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Call;

public class SignupActivity extends Activity {

    private int mCategoryPosition;
    private CategoryBean mWorkCategoryBean = new CategoryBean();
    private LinearLayout mWorkAreaView;

    private TextInputEditText mInputNumbering;
    private TextInputEditText mInputName;
    private TextInputEditText mInputPress;
    private TextInputEditText mInputRemarks;

    private Button btnItemPre;
    private Button btnItemNxt;

    // 当前编辑的图书 Index
    private int mCurrentBookIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.signup_activity);

        // 接收参数
        Bundle bundle = this.getIntent().getExtras();
        mCategoryPosition = bundle.getInt("CategoryBeansLocalPosition");
        mWorkCategoryBean = Kernel.Data.CategoryBeansLocal.get(mCategoryPosition);

        mCurrentBookIndex = mWorkCategoryBean.getBookEditStartIndex();

        // 编辑界面初始化
        bookEditUiRefresh();

        // 设置标题
        getSupportActionBar().setTitle(getWorkCategoryName() + " 类图书");
    }

    @Override
    protected void initView(int layoutResID) {
        super.initView(layoutResID);

        // 工具条
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTopToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupExit();
            }
        });

        // 工作界面
        mWorkAreaView = (LinearLayout) findViewById(R.id.work_area);

        // 设置 Input
        mInputNumbering = (TextInputEditText) findViewById(R.id.input_numbering);
        mInputName = (TextInputEditText) findViewById(R.id.input_name);
        mInputPress = (TextInputEditText) findViewById(R.id.input_press);
        mInputRemarks = (TextInputEditText) findViewById(R.id.input_remarks);

        // 设置 Button
        btnItemPre = (Button) findViewById(R.id.btn_item_pre);
        btnItemNxt = (Button) findViewById(R.id.btn_item_nxt);

        btnItemPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preBtnClick();
            }
        });
        btnItemNxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nxtBtnClick();
            }
        });
    }

    /**
     * 上一本书按钮
     */
    private void preBtnClick() {
        if (mCurrentBookIndex + 1 <= 1) {
            showMsg("没有上一本书了...");
            return;
        }
        bookEditSave();

        mCurrentBookIndex--;
        bookEditUiRefresh();
    }

    /**
     * 下一本书按钮
     */
    private void nxtBtnClick() {
        bookEditSave();

        mCurrentBookIndex++;
        bookEditUiRefresh();
    }

    /**
     * 图书编辑界面刷新
     */
    public void bookEditUiRefresh() {
        BookBean book;
        if (getWorkBooks().size() < mCurrentBookIndex + 1) {
            getWorkBooks().add(mCurrentBookIndex, new BookBean());
            book = getWorkBooks().get(mCurrentBookIndex);
            book.setNumbering(getWorkBooks().get(mCurrentBookIndex - 1).getNumbering() + 1);
        } else {
            book = getWorkBooks().get(mCurrentBookIndex);
        }

        mInputNumbering.setText(getWorkCategoryName() + "" + book.getNumbering());
        mInputName.setText(book.getName());
        mInputPress.setText(book.getPress());
        mInputRemarks.setText(book.getRemarks());

        mInputName.requestFocus();
    }

    /**
     * 当前编辑的图书保存
     */
    public void bookEditSave() {
        BookBean book = getWorkBooks().get(mCurrentBookIndex);
        book.setName(mInputName.getText().toString().trim());
        book.setPress(mInputPress.getText().toString().trim());
        book.setRemarks(mInputName.getText().toString().trim());
        book.setRegistrarName(Kernel.getRegistrarName());
    }

    /**
     * 获取类目名
     */
    private String getWorkCategoryName() {
        return mWorkCategoryBean.getName();
    }

    /**
     * 获取图书
     */
    private ArrayList<BookBean> getWorkBooks() {
        return mWorkCategoryBean.getBooks();
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

        // 工具条保存按钮
        if (id == R.id.action_save) {
            saveData();
            showMsg(getWorkCategoryName() + " 类数据已保存");
        }

        return true;
    }

    /**
     * 保存数据
     */
    public void saveData() {
        // 保存当前编辑的图书
        bookEditSave();
        // 保存当前编辑的图书 index 以便下次返回当前进度
        mWorkCategoryBean.setBookEditStartIndex(mCurrentBookIndex);
        // > CategoryBeanLocal 设置到 CategoryBeansCloud
        Kernel.Data.CategoryBeansCloud.set(mCategoryPosition, mWorkCategoryBean);
    }

    @Override
    public void onBackPressed() {
        signupExit();
    }

    /**
     * 结束编辑
     */
    public void signupExit() {
        showYesOrNoDialog("现在保存并返回主页？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveData();
                Toast.makeText(getApplicationContext(), getWorkCategoryName() + " 类数据已保存", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * 显示 是或否 确认对话框
     */
    public void showYesOrNoDialog(String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 显示一条消息
     */
    public void showMsg(String msg) {
        Snackbar.make(mWorkAreaView, msg, Snackbar.LENGTH_LONG).show();
    }
}