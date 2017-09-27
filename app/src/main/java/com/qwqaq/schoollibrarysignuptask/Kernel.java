package com.qwqaq.schoollibrarysignuptask;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.schoollibrarysignuptask.Beans.BookBean;
import com.qwqaq.schoollibrarysignuptask.Beans.CategoryBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Zneia on 2017/4/16.
 */

public class Kernel extends Application {

    public static final String URL_CATEGORY_RES = "http://query.qwqaq.com/school-library-signup-task/get-category";
    public static final String URL_BOOK_RES = "http://query.qwqaq.com/school-library-signup-task/get-book";
    public static final String URL_UPLOAD = "http://query.qwqaq.com/school-library-signup-task/upload";

    public static SharedPreferences QWQ_PREFERENCES;

    @Override
    public void onCreate() {
        super.onCreate();

        // OkHttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                // .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                // 其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        // Preferences
        QWQ_PREFERENCES = getSharedPreferences("QWQ_PREFERENCES", Context.MODE_PRIVATE);
        // Data.categoryBeansStoreDel();
        // QWQ_PREFERENCES.edit().clear().apply(); // 清除所有内容
        // Log.i("", QWQ_PREFERENCES.getString("TESTING", "空"));

        Data.loadStorageDataToRAM();
    }

    /**
     * 数据操作
     */
    public static class Data {
        /**
         * static 变量也称作静态变量，静态变量和非静态变量的区别是：静态变量被所有的对象所共享，在内存中只有一个副本，它当且仅当在类初次加载时会被初始化。
         * 而非静态变量是对象所拥有的，在创建对象的时候被初始化，存在多个副本，各个对象拥有的副本互不影响。
         *
         * @link http://www.cnblogs.com/dolphin0520/p/3799052.html
         */

        // 已存在于云端的数据
        public static ArrayList<CategoryBean> CategoryBeansCloud = new ArrayList<CategoryBean>();

        // 本地编辑未上传的数据
        public static HashMap<String, CategoryBean> CategoryBeansLocal = new HashMap<String, CategoryBean>();

        /**
         * 云端请求 类目列表 数据，并保存到 内存 和 本地存储
         */
        public static void cloudDownloadCategoryList(final Context context, final StringCallback reqEvents) {
            if (!Kernel.isNetworkAvailable(context)) {
                Toast.makeText(context, "没有网，无法下载云端数据", Toast.LENGTH_LONG).show();
                return;
            }

            OkHttpUtils
                    .get()
                    .url(Kernel.URL_CATEGORY_RES)
                    // .addParams("user", "")
                    .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(context, "？？？数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();

                            reqEvents.onError(call, e, id);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            // 数据导入
                            ArrayList<CategoryBean> categories = new ArrayList<CategoryBean>();
                            try {
                                JSONObject json = new JSONObject(response);
                                if (!json.getBoolean("success")) {
                                    Toast.makeText(context, "服务器响应了错误\n" + json.getString("msg"), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                String dataJsonArr = json.getString("data");
                                categories = (new Gson()).fromJson(dataJsonArr, new TypeToken<ArrayList<CategoryBean>>() {}.getType());
                                for (int i=0; i < categories.size(); i++) {
                                    CategoryBean item = categories.get(i);
                                    item.setCanDelete(false); // 不允许删除
                                    categories.set(i, item);
                                }
                            } catch (Exception e) {
                                Toast.makeText(context, "出现一个野生的错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("请求云端数据下载", "出现错误", e);
                                return;
                            }

                            // 全部删除
                            CategoryBeansCloud.clear();
                            CategoryBeansLocal.clear();

                            // 保存到内存
                            CategoryBeansCloud.addAll(categories);

                            // 保存到存储空间
                            categoryBeansStore();

                            reqEvents.onResponse(response, id);
                        }
                    });
        }

        /**
         * 云端请求 图书 数据，并保存到 内存 和 本地存储
         */
        public static void cloudDownloadBooks(final Context context, final String categoryName, final StringCallback reqEvents) {
            if (!Kernel.isNetworkAvailable(context)) {
                Toast.makeText(context, "没有网，无法下载云端数据", Toast.LENGTH_LONG).show();
                return;
            }

            OkHttpUtils
                    .get()
                    .url(Kernel.URL_BOOK_RES)
                    .addParams("category_name", categoryName)
                    .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(context, "厉害了？数据下载失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();

                            reqEvents.onError(call, e, id);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            // 数据导入
                            ArrayList<BookBean> books = new ArrayList<BookBean>();
                            try {
                                JSONObject json = new JSONObject(response);
                                if (!json.getBoolean("success")) {
                                    Toast.makeText(context, "服务器响应了错误\n" + json.getString("msg"), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                String dataJsonArr = json.getString("data");
                                books = (new Gson()).fromJson(dataJsonArr, new TypeToken<ArrayList<BookBean>>() {}.getType());
                                for (int i=0; i < books.size(); i++) {
                                    BookBean item = books.get(i);
                                    // ...
                                    books.set(i, item);
                                }
                            } catch (Exception e) {
                                Toast.makeText(context, "出现一个野生的错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("请求云端数据下载", "出现错误", e);
                                return;
                            }

                            // 保存到内存
                            for (CategoryBean item : CategoryBeansCloud) {
                                if (item.getName().equals(categoryName)) {
                                    item.getBooks().clear();
                                    item.getBooks().addAll(books);
                                }
                            }

                            // 保存到存储空间
                            categoryBeansStore();

                            reqEvents.onResponse(response, id);
                        }
                    });
        }

        /**
         * 上传数据
         */
        public static void cloudUpdate(final Context context, final StringCallback reqEvents) {
            if (!Kernel.isNetworkAvailable(context)) {
                Toast.makeText(context, "没有网，无法上传数据到云端", Toast.LENGTH_LONG).show();
                return;
            }

            HashMap<String, ArrayList<BookBean>> books = new HashMap<String, ArrayList<BookBean>>();
            for (final CategoryBean itemCategory : CategoryBeansLocal.values())
                books.put(itemCategory.getName(), itemCategory.getBooks());

            Gson gson = new Gson();
            String booksJson = gson.toJson(books);

            OkHttpUtils
                    .post()
                    .url(Kernel.URL_UPLOAD)
                    .addParams("books_data", booksJson)
                    .addHeader("X-QWQ", "SchoolLibrarySignupTask")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(context, "什么鬼？数据上传失败 \n" + e.getMessage(), Toast.LENGTH_LONG).show();

                            reqEvents.onError(call, e, id);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                JSONObject json = new JSONObject(response);
                                if (!json.getBoolean("success")) {
                                    Toast.makeText(context, "服务器响应了错误\n" + json.getString("msg"), Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    Toast.makeText(context, json.getString("msg"), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(context, "出现一个野生的错误\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("请求上传数据到云端", "出现错误", e);
                                return;
                            }

                            // 清空 CategoryBeansLocal
                            CategoryBeansLocal.clear();

                            // 所有设置为禁止删除
                            for (CategoryBean item : CategoryBeansCloud) {
                                item.setCanDelete(false);
                            }

                            // 保存到存储空间
                            categoryBeansStore();

                            reqEvents.onResponse(response, id);
                        }
                    });
        }

        /**
         * CategoryBeansCloud 和 CategoryBeansLocal 数据从内存保存到本地存储
         */
        public static void categoryBeansStore() {
            categoryBeansCloudStore();
            categoryBeansLocalStore();
        }

        /**
         * CategoryBeansCloud 数据从内存保存到本地存储
         */
        public static void categoryBeansCloudStore() {
            Gson gson = new Gson();
            String json = gson.toJson(CategoryBeansCloud);
            QWQ_PREFERENCES.edit().putString("CategoryBeansCloud", json).apply();
            // Log.d("", json);
        }

        /**
         * CategoryBeansLocal 数据从内存保存到本地存储
         */
        public static void categoryBeansLocalStore() {
            Gson gson = new Gson();
            String json = gson.toJson(CategoryBeansLocal);
            QWQ_PREFERENCES.edit().putString("CategoryBeansLocal", json).apply();
            // Log.d("", json);
        }

        public static void categoryBeansStoreDel() {
            QWQ_PREFERENCES.edit().remove("CategoryBeansCloud").apply();
            QWQ_PREFERENCES.edit().remove("CategoryBeansLocal").apply();
        }

        /**
         * 数据从本地存储加载到内存
         */
        public static void loadStorageDataToRAM() {
            String categoryBeansCloudJson = QWQ_PREFERENCES.getString("CategoryBeansCloud", "[]").trim();
            String categoryBeansLocalJson = QWQ_PREFERENCES.getString("CategoryBeansLocal", "[]").trim();

            ArrayList<CategoryBean> categoryBeansCloud = new ArrayList<CategoryBean>();
            HashMap<String, CategoryBean> categoryBeansLocal = new HashMap<String, CategoryBean>();
            try {
                categoryBeansCloud = (new Gson()).fromJson(categoryBeansCloudJson,
                        new TypeToken<ArrayList<CategoryBean>>() {}.getType());
                categoryBeansLocal = (new Gson()).fromJson(categoryBeansLocalJson,
                        new TypeToken<HashMap<String, CategoryBean>>() {}.getType());
            } catch (Exception e) {
                Log.e("从本地存储加载数据", "出现错误", e);
            }

            // 应用到内存中
            CategoryBeansCloud = categoryBeansCloud;
            CategoryBeansLocal = categoryBeansLocal;
        }
    }

    /**
     * 设置登记员名字
     */
    public static void setRegistrarName(String value) {
        QWQ_PREFERENCES.edit().putString("RegistrarName", value).apply();
    }

    /**
     * 获取登记员名字
     */
    public static String getRegistrarName() {
        return QWQ_PREFERENCES.getString("RegistrarName", "").trim();
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }
}