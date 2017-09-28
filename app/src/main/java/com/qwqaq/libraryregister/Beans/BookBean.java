package com.qwqaq.libraryregister.Beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 一本书
 */
public class BookBean {

    @SerializedName("numbering")
    @Expose
    private int numbering;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("press")
    @Expose
    private String press;

    @SerializedName("remarks")
    @Expose
    private String remarks;

    @SerializedName("category_id")
    @Expose
    private String categoryId;

    public int getNumbering() {
        return numbering;
    }

    public void setNumbering(int numbering) {
        this.numbering = numbering;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

}