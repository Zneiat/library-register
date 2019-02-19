package com.qwqaq.libraryregister.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.utils.DateUtil;

import java.util.ArrayList;

/**
 * 一个类目（包含图书）
 */

public class CategoryBean {

    // 类目名
    @SerializedName("name")
    @Expose
    private String name;

    // 记录员名字
    @SerializedName("registrar_name")
    @Expose
    private String registrarName;

    // 备注
    @SerializedName("remarks")
    @Expose
    private String remarks;

    // TODO: 更新日期
    @SerializedName("update_at")
    @Expose
    private String updateAt;

    // 创建日期
    @SerializedName("created_at")
    @Expose
    private String createdAt;

    /** @link https://howtoprogram.xyz/2016/10/16/ignore-or-exclude-field-in-gson/ */
    @SerializedName("books")
    @Expose
    private ArrayList<BookBean> books = new ArrayList<BookBean>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrarName() {
        return registrarName;
    }

    // 类目是不是自己管？
    public boolean getIsMine() {
        if (this.getRegistrarName() == null || this.getRegistrarName().length() < 1)
            return false;

        return this.getRegistrarName().equals(App.Data.getRegistrarName());
    }

    public void setRegistrarName(String registrarName) {
        this.registrarName = registrarName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    // 获取中文的更新时间（刚刚，几天前...）
    public String getUpdateChineseShortTime() {
        if (this.getUpdateAt() == null || this.getUpdateAt().length() < 1 || this.getUpdateAt().equals("0"))
            return "";

        String updateAtText;
        try {
            updateAtText = DateUtil.getShortTime(Long.parseLong(this.getUpdateAt() + "000"));
        } catch (Exception e) {
            updateAtText = "";
        }

        return updateAtText;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // 获取类目下的图书
    public ArrayList<BookBean> getBooks() {
        return books;
    }

    // 是否可删除
    private boolean mCanDelete = false;

    public boolean getCanDelete() {
        return mCanDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.mCanDelete = canDelete;
    }

    // 从哪本书开始编辑？
    private int mBookEditStartIndex = 0;

    public int getBookEditStartIndex() {
        return mBookEditStartIndex;
    }

    public void setBookEditStartIndex(int index) {
        this.mBookEditStartIndex = index;
    }

    // 获取图书有效的最后一本的 index
    public int getLastNonNullBookIndex() {
        if (this.getBooks() == null || this.getBooks().size() < 1)
            return 0;

        int index = 0;
        for (int bookIndex = this.getBooks().size() - 1; bookIndex > - 1; bookIndex--) {
            BookBean bookItem = this.getBooks().get(bookIndex);
            if (bookItem.getName() != null && bookItem.getName().trim().length() > 0) {
                index = bookIndex;
                break;
            }
        }

        return index;
    }
}
