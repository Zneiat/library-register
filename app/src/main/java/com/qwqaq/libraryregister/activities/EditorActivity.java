package com.qwqaq.libraryregister.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.qwqaq.libraryregister.beans.BookBean;
import com.qwqaq.libraryregister.beans.CategoryBean;
import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.R;

import java.util.ArrayList;

public class EditorActivity extends Activity {

    private Menu mMenu = null;

    // 是否正在编辑
    private boolean mIsEditing = false;

    // 是否已保存编辑
    private boolean mIsSaved = false;

    // 源数据在 App.Data.Basic 中的 index
    private int mDataBasicIndex;

    // 工作数据
    private CategoryBean mWorkCategory;

    // 当前正在编辑的图书 Index
    private int mCurrentBookIndex = 0;

    // 各种 View
    private TextInputEditText mInputNumbering;
    private TextInputEditText mInputName;
    private TextInputEditText mInputPress;
    private TextInputEditText mInputRemarks;
    private Button btnBookPre;
    private Button btnBookNxt;

    // Activity 返回结果 CODE
    public static final int RESULT_CODE_UNMODIFIED = 1;
    public static final int RESULT_CODE_SAVED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.editor_activity);

        // 接收参数
        Bundle bundle = this.getIntent().getExtras();
        mDataBasicIndex = bundle.getInt("DataBasicIndex");

        // 工作数据准备
        mWorkCategory = App.Data.categoryBeanClone(App.Data.Basic.get(mDataBasicIndex));

        // 当前正在编辑的图书 index
        mCurrentBookIndex = App.Data.Basic.get(mDataBasicIndex).getBookEditStartIndex();

        // 刚初始化时非编辑模式
        setEditing(false);

        // 图书编辑界面刷新
        bookEditUiRefresh();

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 获取当前工作类目 名
     */
    private String getCategoryName() {
        return mWorkCategory.getName();
    }

    /**
     * 获取当前工作 Category 所有图书
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

        // 设置 Input
        mInputNumbering = (TextInputEditText) findViewById(R.id.input_numbering);
        mInputName = (TextInputEditText) findViewById(R.id.input_name);
        mInputPress = (TextInputEditText) findViewById(R.id.input_press);
        mInputRemarks = (TextInputEditText) findViewById(R.id.input_remarks);

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mIsSaved = false;
            }
        };
        mInputName.setOnFocusChangeListener(onFocusChangeListener);
        mInputPress.setOnFocusChangeListener(onFocusChangeListener);
        mInputRemarks.setOnFocusChangeListener(onFocusChangeListener);

        // 设置 Button
        btnBookPre = (Button) findViewById(R.id.btn_item_pre);
        btnBookNxt = (Button) findViewById(R.id.btn_item_nxt);

        btnBookPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preBtnClick();
            }
        });
        btnBookNxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nxtBtnClick();
            }
        });

        // 长按快速跳转
        btnBookPre.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                redirectBook(0);
                return true;
            }
        });
        btnBookNxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                redirectBook(mWorkCategory.getLastNonNullBookIndex());
                return true;
            }
        });
    }

    /**
     * 设置是否为编辑模式
     */
    private boolean setEditing(boolean isEditing) {
        if (isEditing) {
            // 限制编辑
            if (!mWorkCategory.getIsMine()) {
                showMsg("由" + mWorkCategory.getRegistrarName() + "全权负责，以免发生混乱所以禁止编辑");
                return false;
            }
            setTitle(getCategoryName() + " 类图书 - 编辑");
            mInputName.setEnabled(true);
            mInputPress.setEnabled(true);
            mInputRemarks.setEnabled(true);
            mInputName.requestFocus();
            if (mMenu != null)
                mMenu.findItem(R.id.action_save).setVisible(true);

            // 编辑还未保存
            mIsSaved = false;
        } else {
            setTitle(getCategoryName() + " 类图书 - 浏览");
            mInputName.setEnabled(false);
            mInputPress.setEnabled(false);
            mInputRemarks.setEnabled(false);
            mInputName.clearFocus();
            mInputPress.clearFocus();
            mInputRemarks.clearFocus();
            if (mMenu != null)
                mMenu.findItem(R.id.action_save).setVisible(false);
        }
        mIsEditing = isEditing;
        return true;
    }

    /**
     * 上一本书 按钮点击
     */
    private void preBtnClick() {
        if (mCurrentBookIndex + 1 <= 1) {
            showMsg("没有上一本书了...");
            return;
        }
        // 保存当前图书
        currentBookSave();
        // 切换到下一本
        mCurrentBookIndex--;
        bookEditUiRefresh();
    }

    /**
     * 下一本书 按钮点击
     */
    private void nxtBtnClick() {
        if (!mIsEditing && (mCurrentBookIndex + 1 >= getBooks().size())) {
            showMsg("没有下一本书了...");
            return;
        }
        // 保存当前图书
        currentBookSave();
        // 切换到下一本
        mCurrentBookIndex++;
        bookEditUiRefresh();
    }

    /**
     * 保存 当前正在编辑的图书 到 工作 Category
     */
    private void currentBookSave() {
        bookSaveByIndex(mCurrentBookIndex);
    }

    /**
     * 保存指定 index 图书的编辑 到 工作 Category
     */
    private void bookSaveByIndex(int index) {
        // 非编辑模式不保存
        if (!mIsEditing)
            return;

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

        if (mCurrentBookIndex < getBooks().size()) {
            book = getBooks().get(mCurrentBookIndex);
        } else {
            // 若还未登记过这个 Index 的书（>=）
            for (int i = 0; i < 50; i++) {
                // 一次性就添加 50 个吧
                BookBean newBook = new BookBean();
                newBook.setNumbering(mCurrentBookIndex + i + 1);
                getBooks().add(mCurrentBookIndex + i, newBook);
            }
            book = getBooks().get(mCurrentBookIndex);
        }

        clearAllFocus();

        mInputNumbering.setText(getCategoryName() + " " + book.getNumbering()); // 索引号
        mInputName.setText(book.getName()); // 书名
        mInputPress.setText(book.getPress()); // 出版社
        mInputRemarks.setText(book.getRemarks()); // 备注

        mInputName.requestFocus();
    }

    /**
     * 清除所有 Focus
     */
    private void clearAllFocus() {
        mInputName.clearFocus();
        mInputPress.clearFocus();
        mInputRemarks.clearFocus();
    }

    /**
     * 保存当前类目编辑的数据 到 App.Data.Basic, App.Data.Local 中
     */
    public void saveWorkCategory() {
        // 保存当前正在编辑的图书
        currentBookSave();

        // 保存当前编辑的图书 index 以便下次返回当前进度
        mWorkCategory.setBookEditStartIndex(mCurrentBookIndex);

        // 关键 > Local Beans 设置到 Basic Beans
        //（这样下次 CategoryActivity 再创建 this 才是 现在 mWorkCategory 的数据）
        App.Data.Local.put(getCategoryName(), App.Data.categoryBeanClone(mWorkCategory));
        App.Data.Basic.set(mDataBasicIndex, App.Data.categoryBeanClone(mWorkCategory));
        App.Data.dataPrefStore();

        // 编辑已保存
        mIsSaved = true;
    }

    /**
     * 退出编辑
     */
    public void editExit() {
        if (!mIsEditing) {
            // 不处于编辑状态，不保存
            setResult(RESULT_CODE_UNMODIFIED);
            finish();
            return;
        }

        if (mIsSaved) {
            // 编辑状态已未保存
            setResult(RESULT_CODE_UNMODIFIED);
            finish();
            return;
        }

        // 询问保存编辑数据
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("工作已结束啦？");
        builder.setMessage("然而... 你的选择是？");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                saveWorkCategory();
                Intent intent = new Intent();
                intent.putExtra("ResultMessage", getCategoryName() + " 类数据保已保存");
                setResult(RESULT_CODE_SAVED, intent);
                finish();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("不保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                showConfirm(null, "真的不保存？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CODE_UNMODIFIED);
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

    /*
    |--------------------------------------------------------------------------
    | 各种编辑操作
    |--------------------------------------------------------------------------
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signup_activity_top_tool_bar_menu, menu);
        super.onCreateOptionsMenu(menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_go_edit) {
            if (setEditing(true)) {
                item.setVisible(false);
            }
        }

        if (id == R.id.action_save && mIsEditing) {
            saveWorkCategory();
            showMsg(getCategoryName() + " 类数据保已保存");
        }

        if (id == R.id.action_redirect_book) {
            showBookListRedirectBook();
        }

        if (id == R.id.action_copy_pre_book && mIsEditing) {
            copyBook(mCurrentBookIndex - 1);
        }

        if (id == R.id.action_copy_book && mIsEditing) {
            showBookListCopyEditBook();
        }

        if (id == R.id.action_current_del_all && mIsEditing) {
            changeCurrentBookData("", "", "");
        }

        return true;
    }

    /**
     * 显示图书列表跳转到一本书
     */
    private void showBookListRedirectBook() {
        bookListDialog("跳转到一本书", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                // 点击项目定位到指定 index 图书，进行编辑
                redirectBook(index);
                dialog.cancel();
            }
        });
    }

    /**
     * 显示图书列表快速复制某本书
     */
    private void showBookListCopyEditBook() {
        bookListDialog("复制某本书", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                copyBook(index);
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
            String bookName = "";
            bookName += getCategoryName() + " " + item.getNumbering() + ". ";
            if (item.getName() != null && item.getName().trim().length() > 0) {
                bookName += item.getName().trim();
            }
            items[i] = bookName;
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
     * 快速跳转到某本书
     */
    private void redirectBook(int bookIndex) {
        if (mWorkCategory.getBooks().size() < bookIndex + 1) {
            showMsg("跳转失败，没有 第 " + (bookIndex + 1) + " 本图书");
            return;
        }

        // 在跳转之前 保存当前图书
        if (mIsEditing) currentBookSave();

        mCurrentBookIndex = bookIndex;
        bookEditUiRefresh();
        showMsg("已跳转到 第 " + (bookIndex + 1) + " 本图书");
    }

    /**
     * 快速复制某本书
     */
    private void copyBook(int bookIndex) {
        if (bookIndex < 0 || bookIndex >= getBooks().size()) {
            showMsg("没这本书，不能复制");
            return;
        }
        BookBean book = getBooks().get(bookIndex);
        changeCurrentBookData(book.getName(), book.getPress(), book.getRemarks());
    }

    /**
     * 改变当前正在编辑图书的数据
     */
    private void changeCurrentBookData(String name, String press, String remarks) {
        mInputNumbering.setText(getCategoryName() + " " + getBooks().get(mCurrentBookIndex).getNumbering());
        if (name != null)
            mInputName.setText(name);
        if (press != null)
            mInputPress.setText(press);
        if (remarks != null)
            mInputRemarks.setText(remarks);
        // 保存当前这本书
        currentBookSave();
    }
}