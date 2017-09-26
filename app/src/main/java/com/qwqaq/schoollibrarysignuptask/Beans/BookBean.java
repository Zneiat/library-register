package com.qwqaq.schoollibrarysignuptask.Beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 一本书
 */
public class BookBean {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("numbering")
    @Expose
    private String numbering;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("press")
    @Expose
    private String press;

    @SerializedName("remarks")
    @Expose
    private String remarks;

    @SerializedName("registrar_name")
    @Expose
    private String registrarName;

    @SerializedName("category_id")
    @Expose
    private String categoryId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumbering() {
        return numbering;
    }

    public void setNumbering(String numbering) {
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

    public String getRegistrarName() {
        return registrarName;
    }

    public void setRegistrarName(String registrarName) {
        this.registrarName = registrarName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

}