package com.qwqaq.libraryregister;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.libraryregister.Beans.BookBean;
import com.qwqaq.libraryregister.Beans.CategoryBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by Zneia on 2017/9/28.
 */

public class Http {

    private Context mContext;

    public Http(Context context) {
        mContext = context;

        if (!App.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, "网络未连接，无法与世界连接", Toast.LENGTH_LONG).show();
            return;
        }
    }

    public static final String URL_BASE = "http://query.qwqaq.com/school-library-signup-task";

    public static final String URL_CATEGORY_RES = URL_BASE + "/get-category";
    public static final String URL_BOOK_RES = URL_BASE + "/get-book";
    public static final String URL_UPLOAD = URL_BASE + "/upload-book";

    /**
     * 下载 类目列表
     */
    public void downloadCategory(final StringCallback callbackEvents) {
        if (!App.isNetworkAvailable(mContext)) return;

        OkHttpUtils
                .get()
                .url(URL_CATEGORY_RES)
                // .addParams("user", "")
                .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        ResponseReader resp = new ResponseReader(response);
                        if (!resp.isSuccess()) {
                            callbackEvents.onError(null, new Exception(resp.getMsg()), 0);
                            return;
                        };

                        // 数据导入
                        ArrayList<CategoryBean> categories;

                        try {
                            categories = (new Gson()).fromJson(resp.getData(), new TypeToken<ArrayList<CategoryBean>>() {}.getType());
                        } catch (Exception e) {
                            Toast.makeText(mContext, "解析云端类目列表错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("解析云端类目列表错误", "错误", e);
                            return;
                        }

                        // 整体修改
                        for (int i=0; i < categories.size(); i++) {
                            CategoryBean item = categories.get(i);
                            item.setCanDelete(false); // 不允许删除
                            categories.set(i, item);
                        }

                        // 内存原来的数据全部清空
                        App.Data.Basic.clear();
                        App.Data.Local.clear();

                        // 保存到内存
                        App.Data.Basic.addAll(categories);

                        // 保存到存储空间
                        App.Data.dataPrefStore();

                        callbackEvents.onResponse(response, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(mContext, "厉害了... 数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();

                        callbackEvents.onError(call, e, id);
                    }
                });
    }

    /**
     * 下载单个类目图书
     */
    public void downloadBook(final Context context, final String categoryName, final StringCallback callbackEvents) {
        if (!App.isNetworkAvailable(context)) return;

        OkHttpUtils
                .get()
                .url(URL_BOOK_RES)
                .addParams("category_name", categoryName)
                .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        ResponseReader resp = new ResponseReader(response);
                        if (!resp.isSuccess()) {
                            callbackEvents.onError(null, new Exception(resp.getMsg()), 0);
                            return;
                        };

                        // 数据导入
                        ArrayList<BookBean> books;
                        try {
                            books = (new Gson()).fromJson(resp.getData(), new TypeToken<ArrayList<BookBean>>() {}.getType());
                        } catch (Exception e) {
                            Toast.makeText(context, "解析云端图书数据错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("解析云端图书数据错误", "错误", e);
                            return;
                        }

                        // 整体修改
                        for (int i=0; i < books.size(); i++) {
                            BookBean item = books.get(i);
                            // ...
                            books.set(i, item);
                        }

                        // 保存到内存
                        for (CategoryBean item : App.Data.Basic) {
                            if (item.getName().equals(categoryName)) {
                                item.getBooks().clear();
                                item.getBooks().addAll(books);
                                if (books.size() > 0) {
                                    // 将最后一个项目作为开始编辑的第一个项目（编辑进度修改）
                                    item.setBookEditStartIndex(books.size() - 1);
                                }
                                break;
                            }
                        }

                        // 保存到存储空间
                        App.Data.dataPrefStore();

                        callbackEvents.onResponse(response, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(context, "有趣... 数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();

                        callbackEvents.onError(call, e, id);
                    }
                });
    }

    /**
     * 上传数据
     */
    public void updateBook(final Context context, final StringCallback callbackEvents) {
        if (!App.isNetworkAvailable(context)) return;

        // Local Books
        HashMap<String, ArrayList<BookBean>> books = new HashMap<String, ArrayList<BookBean>>();
        for (final CategoryBean itemCategory : App.Data.Local.values())
            books.put(itemCategory.getName(), itemCategory.getBooks());

        String booksJson = (new Gson()).toJson(books);

        // 上传并清空 Local
        OkHttpUtils
                .post()
                .url(URL_UPLOAD)
                .addParams("registrar_name", App.Data.getRegistrarName())
                .addParams("book_data", booksJson)
                .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        ResponseReader resp = new ResponseReader(response);
                        if (!resp.isSuccess()) {
                            callbackEvents.onError(null, new Exception(resp.getMsg()), 0);
                            return;
                        };

                        // 响应 Msg
                        resp.makeMsgToast();

                        // 清空 Local
                        App.Data.Local.clear();

                        // 所有 Basic 项目设置为禁止删除
                        for (CategoryBean item : App.Data.Basic) {
                            item.setCanDelete(false);
                        }

                        // 保存到存储空间
                        App.Data.dataPrefStore();

                        callbackEvents.onResponse(response, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(context, "什么鬼... 数据上传失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();

                        callbackEvents.onError(call, e, id);
                    }
                });
    }

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
                        Toast.makeText(mContext, "云端程序响应错误：" + getMsg(), Toast.LENGTH_LONG).show();
                    }
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                Toast.makeText(mContext, "解析云端响应内容错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("解析云端响应内容", "错误", e);
                return false;
            }
        }

        public String getMsg() {
            try {
                JSONObject json = new JSONObject(mRespJsonStr);
                return json.getString("msg");
            } catch (Exception e) {
                Toast.makeText(mContext, "解析云端响应内容错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("解析云端响应内容", "错误", e);
                return "";
            }
        }

        public void makeMsgToast() {
            if (getMsg().trim().length() > 0) {
                Toast.makeText(mContext, getMsg(), Toast.LENGTH_LONG).show();
            }
        }

        public String getData() {
            try {
                JSONObject json = new JSONObject(mRespJsonStr);
                return json.getString("data");
            } catch (Exception e) {
                Toast.makeText(mContext, "解析云端响应内容错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("解析云端响应内容", "错误", e);
                return "";
            }
        }
    }
}
