package com.qwqaq.schoollibrarysignuptask.Beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private Object remarks;

    // 更新日期
    @SerializedName("update_at")
    @Expose
    private String updateAt;

    // 创建日期
    @SerializedName("created_at")
    @Expose
    private String createdAt;

    // 类目下的图书
    private ArrayList<BookBean> mBooks = new ArrayList<BookBean>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrarName() {
        return registrarName;
    }

    public void setRegistrarName(String registrarName) {
        this.registrarName = registrarName;
    }

    public Object getRemarks() {
        return remarks;
    }

    public void setRemarks(Object remarks) {
        this.remarks = remarks;
    }

    public String getUpdateAt() {
        return updateAt;
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

    public ArrayList<BookBean> getBooks() {
        return mBooks;
    }

    // 是否可删除
    private boolean mCanDelete = true;

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
}
