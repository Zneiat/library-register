package com.qwqaq.libraryregister.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qwqaq.libraryregister.Beans.BookBean;
import com.qwqaq.libraryregister.Beans.CategoryBean;
import com.qwqaq.libraryregister.Kernel;
import com.qwqaq.libraryregister.R;

import java.util.ArrayList;

public class EditorActivity extends Activity {

    private int mCategoryInBasicBeansPosition;
    private CategoryBean mCategoryInBasicBeans;

    private CategoryBean mWorkCategory = new CategoryBean(); // In Local Beans
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
        initView(R.layout.editor_activity);

        // 接收参数
        Bundle bundle = this.getIntent().getExtras();

        // 获取指定的 在 BasicBeans 中的 CategoryBean
        mCategoryInBasicBeansPosition = bundle.getInt("CategoryInBasicBeansPosition");
        mCategoryInBasicBeans = Kernel.Data.Basic.get(mCategoryInBasicBeansPosition);

        // 设置标题
        getSupportActionBar().setTitle(getCategoryName() + " 类图书");

        // 当前工作 Category Bean, 在 LocalBeans 中的
        mWorkCategory = Kernel.Data.Local.get(bundle.getString("CategoryName"));

        // 当前编辑图书 index
        mCurrentBookIndex = mWorkCategory.getBookEditStartIndex();

        // 图书编辑界面刷新
        bookEditUiRefresh();
    }

    /**
     * 获取当前工作类目 名
     */
    private String getCategoryName() {
        return mCategoryInBasicBeans.getName();
    }

    /**
     * 获取当前工作类目 所有图书
     */
    private ArrayList<BookBean> getBooks() {
        return mWorkCategory.getBooks();
    }

    @Override
    protected void initView(int layoutResID) {
        super.initView(layoutResID);

        // 工具条
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTopToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editExit();
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
     * 上一本书 按钮点击
     */
    private void preBtnClick() {
        if (mCurrentBookIndex + 1 <= 1) {
            showMsg("没有上一本书了...");
            return;
        }
        oneBookEditSave();

        mCurrentBookIndex--;
        bookEditUiRefresh();
    }

    /**
     * 下一本书 按钮点击
     */
    private void nxtBtnClick() {
        oneBookEditSave();

        mCurrentBookIndex++;
        bookEditUiRefresh();
    }

    /**
     * 保存 工作类目 当前一本图书的编辑
     */
    private void oneBookEditSave() {
        oneBookEditSave(mCurrentBookIndex);
    }

    /**
     * 保存 工作类目 指定 index 图书的编辑
     */
    private void oneBookEditSave(int index) {
        BookBean book = getBooks().get(index);
        book.setName(mInputName.getText().toString().trim());
        book.setPress(mInputPress.getText().toString().trim());
        book.setRemarks(mInputRemarks.getText().toString().trim());
    }

    /**
     * 图书编辑界面根据 mCurrentBookIndex 刷新
     */
    private void bookEditUiRefresh() {
        BookBean book;
        if (getBooks().size() - 1 < mCurrentBookIndex) {
            // 若还未登记过这个 Index 的书
            getBooks().add(mCurrentBookIndex, new BookBean());
            book = getBooks().get(mCurrentBookIndex);
            book.setNumbering(mCurrentBookIndex + 1);
        } else {
            book = getBooks().get(mCurrentBookIndex);
        }

        mInputNumbering.setText(getCategoryName() + " " + book.getNumbering()); // 索引号
        mInputName.setText(book.getName()); // 书名
        mInputPress.setText(book.getPress()); // 出版社
        mInputRemarks.setText(book.getRemarks()); // 备注

        mInputName.requestFocus();
    }

    /**
     * 修改当前图书的数据
     */
    private void editCurrentBookData(int numbering, String name, String press, String remarks) {
        
        mInputNumbering.setText(getCategoryName() + " " + numbering); // 索引号
        mInputName.setText(name); // 书名
        mInputPress.setText(press); // 出版社
        mInputRemarks.setText(press); // 备注

        // 保存当前这本书
        oneBookEditSave();
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
            saveDataToBasicBeans();
            showMsg(getCategoryName() + " 类数据已保存");
        }

        // 快速跳转到一本书 列表
        if (id == R.id.action_book_list) {
            bookListRedirectEditBook();
        }

        if (id == R.id.action_copy_book) {
            bookListCopyEditBook();
        }

        return true;
    }

    /**
     * 快速复制一本书
     */
    private void bookListCopyEditBook() {
        bookListDialog("复制一本书", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                BookBean book = getBooks().get(index);
                editCurrentBookData(getBooks().get(mCurrentBookIndex).getNumbering(), book.getName(), book.getPress(), book.getRemarks());
                dialog.cancel();
            }
        });
    }

    /**
     * 跳转到一本书
     */
    private void bookListRedirectEditBook() {
        bookListDialog("跳转到一本书", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                // 点击项目定位到指定 index 图书，进行编辑
                mCurrentBookIndex = index;
                bookEditUiRefresh();
                dialog.cancel();
            }
        });
    }

    /**
     * 所有图书 列表对话框
     */
    private void bookListDialog(String title, DialogInterface.OnClickListener listItemOnClick) {
        ArrayList<BookBean> books = getBooks();
        // 准备列表数据
        final CharSequence[] items = new CharSequence[books.size()];
        for (int i = 0; i < books.size(); i++) {
            BookBean item = books.get(i);
            items[i] = getCategoryName() + " " + item.getNumbering() + ". " + item.getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title == null) {
            builder.setTitle("图书列表");
        } else {
            builder.setTitle(title);
        }
        builder.setSingleChoiceItems(items, mCurrentBookIndex, listItemOnClick);
        builder.show();
    }

    /**
     * 保存当前类目编辑的数据（In Local Beans）到 Basic Beans 中
     */
    public void saveDataToBasicBeans() {
        // 保存当前编辑的图书
        oneBookEditSave();

        // 保存当前编辑的图书 index 以便下次返回当前进度
        // mWorkCategory.setBookEditStartIndex(mCurrentBookIndex);

        // 关键 > Local Beans 设置到 Basic Beans
        //（这样下次 CategoryActivity 再创建 this 才是 现在 mWorkCategory 的数据）
        Kernel.Data.Basic.set(mCategoryInBasicBeansPosition, mWorkCategory);
        Kernel.Data.dataPrefStore();
    }

    /**
     * 结束编辑
     */
    public void editExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setTitle("工作已结束啦？");
        builder.setMessage("然而... 你的选择是？");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                saveDataToBasicBeans();
                Toast.makeText(getApplicationContext(), getCategoryName() + " 类数据保存成功", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("不保存", new DialogInterface.OnClickListener() {
            // TODO: 只要 set 到 getBooks() 里，就自动保存了！Local 变化 Basic 也跟着变了！
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                showYesOrNoDialog("真的不保存？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        editExit();
    }

    /**
     * 显示 是或否 确认对话框
     */
    public void showYesOrNoDialog(String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
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