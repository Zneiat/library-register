package com.qwqaq.libraryregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.libraryregister.activities.Activity;
import com.qwqaq.libraryregister.activities.CategoryActivity;
import com.qwqaq.libraryregister.beans.BookBean;
import com.qwqaq.libraryregister.beans.CategoryBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Zneia on 2017/9/28.
 */

public class Http {

    private Activity mParentActivity;

    private ProgressDialog loadingProgress;
    private boolean showLoading = true;

    public Http(Activity activity) {
        mParentActivity = activity;

        // 加载动画
        loadingProgress = new ProgressDialog(mParentActivity) {
            @Override
            public void show() {
                if (!showLoading) return;
                super.show();
            }

            @Override
            public void cancel() {
                if (!showLoading) return;
                super.cancel();
            }
        };
    }

    private Activity getActivity() {
        return mParentActivity;
    }

    private Context getContext() {
        return mParentActivity;
    }

    // 设置是否显示加载动画
    public void setShowLoading(boolean isShow) {
        showLoading = isShow;
    }

    // URLs
    public static final String API_BASE = "http://lr.qwqaq.com";

    /**
     * 获取 CategoryActivity
     */
    public CategoryActivity getCategoryActivity() {
        if (!(getActivity() instanceof CategoryActivity)) {
            Toast.makeText(getContext(), "禁止在非 CategoryActivity 中调用", Toast.LENGTH_LONG).show();
            return null;
        }

        if (!App.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "网络断开，你已与世界失联", Toast.LENGTH_LONG).show();
            return null;
        }

        return (CategoryActivity) getActivity();
    }


    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    /**
     * 从云端获取所有类目（覆盖原来的数据）
     */
    public void getCategories(final StringCallback callbackEvents) {
        final CategoryActivity activity = getCategoryActivity();
        if (activity == null) return;

        final RequestCall req = OkHttpUtils
                .get()
                .url(API_BASE + "/getCategories")
                .addParams("withBooks", "1")
                .build();

        activity.mSwipeView.setRefreshing(true);

        req.execute(new StringCallback() {
            @Override
            public void onResponse(String respStr, int id) {
                activity.mSwipeView.setRefreshing(false);

                ResponseReader resp = new ResponseReader(respStr);
                if (!resp.isSuccess()) {
                    callbackEvents.onError(null, new Exception(resp.getMsg()), id);
                    return;
                }

                // 通过 API 响应字符串 导入类目数据
                importCategoryByRespStr(activity, respStr);

                callbackEvents.onResponse(respStr, id);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                activity.mSwipeView.setRefreshing(false);

                Toast.makeText(getContext(), "厉害了... 数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();
                callbackEvents.onError(call, e, id);
            }
        });
    }

    public void getCategories() {
        getCategories(new StringCallback() {
            @Override
            public void onResponse(String response, int id) {}

            @Override
            public void onError(Call call, Exception e, int id) {}
        });
    }

    /**
     * 通过 API 响应字符串 导入类目数据（覆盖原来的数据）
     *
     * @param categoryActivity CategoryActivity
     * @param responseStr API 响应的字符串
     */
    public void importCategoryByRespStr(final CategoryActivity categoryActivity, String responseStr) {
        // 解析数据
        ResponseReader resp = new ResponseReader(responseStr);
        if (!resp.isSuccess()) return;

        // 数据导入
        ArrayList<CategoryBean> categories;

        try {
            String categoriesJson = resp.getData().getString("categories");
            categories = (new Gson()).fromJson(categoriesJson, new TypeToken<ArrayList<CategoryBean>>() {}.getType());
        } catch (Exception e) {
            Toast.makeText(getContext(), "解析云端类目列表错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("解析云端类目列表错误", "错误", e);
            return;
        }

        // 整体修改
        for (int i = 0; i < categories.size(); i++) {
            CategoryBean categoryItem = categories.get(i);
            categoryItem.setCanDelete(false); // 不允许删除

            // 处理类目中的图书
            handleCategoryBooks(categoryItem);
        }

        // 内存原来的数据全部清空
        App.Data.Basic.clear();
        App.Data.Local.clear();

        // 保存到内存
        App.Data.Basic.addAll(categories);

        // 保存到存储空间
        App.Data.dataPrefStore();

        // 刷新类目列表
        categoryActivity.mAdapter.notifyDataSetChanged();
    }


    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    /**
     * 从云端获取所有类目来进行更新（不 覆盖原来的数据）
     */
    public void getCategoriesUpdate() {
        final CategoryActivity activity = getCategoryActivity();
        if (activity == null) return;

        final RequestCall req = OkHttpUtils
                .get()
                .url(API_BASE + "/getCategories")
                .addParams("withBooks", "1")
                .build();

        req.execute(new StringCallback() {
            @Override
            public void onResponse(String respStr, int id) {
                activity.mSwipeView.setRefreshing(false);

                ResponseReader resp = new ResponseReader(respStr);
                if (!resp.isSuccess()) {
                    return;
                }

                // 通过 API 响应字符串 更新类目数据
                updateCategoryByRespStr(activity, respStr);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                activity.mSwipeView.setRefreshing(false);

                Toast.makeText(getContext(), "厉害了... 数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 通过 API 响应字符串 更新类目数据（不 覆盖原来的数据）
     *
     * @param categoryActivity CategoryActivity
     * @param responseStr API 响应的字符串
     */
    public void updateCategoryByRespStr(final CategoryActivity categoryActivity, String responseStr) {
        // 解析数据
        ResponseReader resp = new ResponseReader(responseStr);
        if (!resp.isSuccess()) return;

        // 数据导入
        ArrayList<CategoryBean> categoriesCloud;

        try {
            String categoriesJson = resp.getData().getString("categories");
            categoriesCloud = (new Gson()).fromJson(categoriesJson, new TypeToken<ArrayList<CategoryBean>>() {}.getType());
        } catch (Exception e) {
            Toast.makeText(getContext(), "解析云端类目列表错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("解析云端类目列表错误", "错误", e);
            return;
        }

        // 整体修改
        for (int i = 0; i < categoriesCloud.size(); i++) {
            CategoryBean categoryItem = categoriesCloud.get(i);
            categoryItem.setCanDelete(false); // 不允许删除

            // 处理类目中的图书
            handleCategoryBooks(categoryItem);
        }

        // 将云端 消失的 类目本地化
        for (int i = 0; i < App.Data.Basic.size(); i++) {
            CategoryBean itemBasic = App.Data.Basic.get(i);
            boolean cloudExist = false;

            for (CategoryBean itemCloud : categoriesCloud) {
                if (itemCloud.getName().equals(itemBasic.getName())) {
                    cloudExist = true;
                    break;
                }
            }

            // 若本地存在的类目，但是云端不存在
            if (!cloudExist) {
                // 是不是自己管的？
                if (itemBasic.getIsMine()) {
                    // 本地化
                    App.Data.Local.put(itemBasic.getName(), itemBasic);
                    itemBasic.setCanDelete(true);
                } else {
                    // 不是自己管的就删掉
                    App.Data.Basic.remove(i);
                }
            }
        }

        // 更新内存中的数据
        for (CategoryBean itemNew : categoriesCloud) {
            // Basic 中搜寻该类目
            int categoryBasicIndex = -1;

            for (int i = 0; i < App.Data.Basic.size(); i++) {
                CategoryBean categoryBasicItem = App.Data.Basic.get(i);

                // 名字吻合
                if (categoryBasicItem.getName().equals(itemNew.getName())) {
                    categoryBasicIndex = i;
                    break;
                }
            }

            // 旧数据是否存在
            if (categoryBasicIndex < 0) {
                // 旧数据不存在

                // 添加新数据
                categoryActivity.mAdapter.insert(itemNew, 0);
            } else {
                // 旧数据存在

                // 若该类目不能更新
                if (!isCategoryCanUpdate(App.Data.Basic.get(categoryBasicIndex), false))
                    continue; // 直接跳过

                // 替换旧数据
                App.Data.Basic.set(categoryBasicIndex, itemNew);
            }
        }

        // 保存到存储空间
        App.Data.dataPrefStore();

        // 刷新类目列表
        categoryActivity.mAdapter.notifyDataSetChanged();
    }


    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    /**
     * 从云端获取单个类目所有图书
     */
    public void getCategoryBooks(final String categoryName, final StringCallback callbackEvents) {
        final CategoryActivity activity = getCategoryActivity();
        if (activity == null) return;

        // 查找类目信息
        CategoryBean category = null;
        for (int i = 0; i < App.Data.Basic.size(); i++) {
            if (App.Data.Basic.get(i).getName().equals(categoryName)) {
                category = App.Data.Basic.get(i);
                break;
            }
        }
        if (category == null) {
            activity.showMsg("无法找到 类目" + categoryName + " 所以不能更新该类目的图书");
            return;
        }

        // 判断该类目是否能更新
        if (!isCategoryCanUpdate(category, true)) {
            return;
        }

        RequestCall req = OkHttpUtils
                .get()
                .url(API_BASE + "/getCategoryBooks")
                .addParams("categoryName", categoryName)
                .build();

        loadingProgress.setMessage("类目" + categoryName + " 数据下载中...");
        loadingProgress.show();

        req.execute(new StringCallback() {
            @Override
            public void onResponse(String respStr, int id) {
                loadingProgress.cancel();

                ResponseReader resp = new ResponseReader(respStr);
                if (!resp.isSuccess()) {
                    callbackEvents.onError(null, new Exception(resp.getMsg()), 0);
                    return;
                }

                // 解析数据
                CategoryBean category;
                ArrayList<BookBean> booksInCategory;
                try {
                    String categoryJson = resp.getData().getString("category");
                    String booksJson = resp.getData().getString("books");
                    category = (new Gson()).fromJson(categoryJson, CategoryBean.class);
                    booksInCategory = (new Gson()).fromJson(booksJson, new TypeToken<ArrayList<BookBean>>() {}.getType());
                } catch (Exception e) {
                    Toast.makeText(getContext(), "解析云端数据错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("解析云端图书数据错误", "错误", e);
                    return;
                }

                // 更新 Category 的图书数据
                category.getBooks().clear();
                category.getBooks().addAll(booksInCategory);

                // 处理类目中的图书
                handleCategoryBooks(category);

                // 更新内存中的数据
                for (int i = 0; i < App.Data.Basic.size(); i++) {
                    // 寻找类目
                    if (App.Data.Basic.get(i).getName().equals(category.getName())) {
                        // 应用数据
                        App.Data.Basic.set(i, category);
                        break;
                    }
                }

                // 保存到存储空间
                App.Data.dataPrefStore();

                // 刷新类目列表
                activity.mAdapter.notifyDataSetChanged();

                callbackEvents.onResponse(respStr, id);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                loadingProgress.cancel();

                Toast.makeText(getContext(), "有趣... " + categoryName + "类图书 数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();
                callbackEvents.onError(call, e, id);
            }
        });
    }

    public void getCategoryBooks(final String categoryName) {
        getCategoryBooks(categoryName, new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
            }

            @Override
            public void onError(Call call, Exception e, int id) {
            }
        });
    }


    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    /**
     * 类目加入图书数据处理
     */
    public void handleCategoryBooks(final CategoryBean category) {
        ArrayList<BookBean> booksInCategory = category.getBooks();

        if (booksInCategory.size() < 1) return;

        // 图书 整体修改
        for (int i = 0; i < booksInCategory.size(); i++) {
            BookBean item = booksInCategory.get(i);
            // ...
        }

        // 将最后一个有内容的项目作为开始编辑的第一个项目（编辑进度修改）
        category.setBookEditStartIndex(category.getLastNonNullBookIndex());
    }

    /**
     * 判断一个类目是否能更新（防止本地编辑数据丢失）
     */
    public boolean isCategoryCanUpdate(CategoryBean category, boolean isMsgDisplay) {
        // 判断该类目数据是否没上传
        if (App.Data.Local.containsKey(category.getName())) {
            if (isMsgDisplay)
                getActivity().showMsg("类目" + category.getName() + " 图书数据有待上传，所以无法更新");
            return false;
        }

        // 判断是否为本人管理
        if (category.getIsMine() && category.getBooks().size() > 0) {
            if (isMsgDisplay)
                getActivity().showMsg("类目" + category.getName() + " 该你管，所以不用更新哟");
            return false;
        }

        return true;
    }


    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    /**
     * 上传数据到云端
     */
    public void uploadCategoryBooks() {
        final CategoryActivity activity = getCategoryActivity();
        if (activity == null) return;

        // 准备将上传的数据
        HashMap<String, ArrayList<BookBean>> books = new HashMap<String, ArrayList<BookBean>>();
        for (final CategoryBean itemCategory : App.Data.Local.values())
            books.put(itemCategory.getName(), itemCategory.getBooks());
        String booksJson = (new Gson()).toJson(books);

        // 上传并清空 Local
        RequestCall req = OkHttpUtils
                .post()
                .url(API_BASE + "/uploadCategoryBooks")
                .addParams("registrarName", App.Data.getRegistrarName())
                .addParams("booksInCategoriesJson", booksJson)
                .build();

        loadingProgress.setMessage("正在上传数据到云端...");
        loadingProgress.show();

        req.execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                loadingProgress.cancel();
            }

            @Override
            public void onResponse(String respStr, int id) {
                ResponseReader resp = new ResponseReader(respStr);
                if (!resp.isSuccess()) {
                    return;
                }

                // 获取 Category update_at
                String updateAt = "";
                try {
                    updateAt = resp.getData().getString("update_at");
                } catch (Exception e) {
                    Toast.makeText(getContext(), "类目 update_at 获取错误 \n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                // 操作所有 Basic 项目
                for (CategoryBean item : App.Data.Basic) {
                    // 如果是刚刚已上传的项目
                    if (App.Data.Local.containsKey(item.getName())) {
                        item.setUpdateAt(updateAt);
                        item.setCanDelete(false);
                    }
                }

                // 清空 Local
                App.Data.Local.clear();

                // 保存到存储空间
                App.Data.dataPrefStore();

                // Success Info Dialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("上传数据完毕");
                dialog.setMessage(resp.getMsg());
                dialog.setPositiveButton("知道了", null);
                dialog.show();

                // 刷新类目列表
                activity.mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(getContext(), "什么鬼... 数据上传失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * 响应数据处理
     */
    public class ResponseReader {

        private String mRespJsonStr;

        public ResponseReader(String responseJsonStr) {
            mRespJsonStr = responseJsonStr;
        }

        public boolean isSuccess() {
            try {
                JSONObject json = new JSONObject(mRespJsonStr);
                if (!json.getBoolean("success")) {
                    if (getMsg().trim().length() > 0) {
                        Toast.makeText(getContext(), "云端程序响应错误：" + getMsg(), Toast.LENGTH_LONG).show();
                    }
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "解析云端响应内容错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("解析云端响应内容", "错误", e);
                return false;
            }
        }

        public String getMsg() {
            try {
                JSONObject json = new JSONObject(mRespJsonStr);
                return json.getString("msg");
            } catch (Exception e) {
                Toast.makeText(getContext(), "解析云端响应内容错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("解析云端响应内容", "错误", e);
                return "";
            }
        }

        public void makeMsgToast() {
            if (getMsg().trim().length() > 0) {
                Toast.makeText(getContext(), getMsg(), Toast.LENGTH_LONG).show();
            }
        }

        public JSONObject getData() {
            try {
                JSONObject json = new JSONObject(mRespJsonStr);
                return json.getJSONObject("data");
            } catch (Exception e) {
                Toast.makeText(getContext(), "解析云端响应内容错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("解析云端响应内容", "错误", e);
                return null;
            }
        }
    }
}
