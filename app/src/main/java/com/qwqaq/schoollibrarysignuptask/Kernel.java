package com.qwqaq.schoollibrarysignuptask;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.schoollibrarysignuptask.Beans.CategoryBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
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
    public static ArrayList<CategoryBean> gCategoryBeans = new ArrayList<CategoryBean>();
    public static SharedPreferences QWQ_PREFERENCES;

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                // 其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        initSharedPreferences();

        gCategoryBeans = getLocalCategoryData(); // 从本地加载图书类目
    }

    public void initSharedPreferences() {
        QWQ_PREFERENCES = getSharedPreferences("QWQ_PREFERENCES", Context.MODE_PRIVATE);
        // QWQ_PREFERENCES.edit().clear().apply(); // 清除所有内容
        // Log.i("", QWQ_PREFERENCES.getString("TESTING", "空"));
    }

    /**
     * 设置登记员名字
     */
    public static void setRegistrarName(String value) {
        QWQ_PREFERENCES
                .edit()
                .putString("RegistrarName", value)
                .apply();
    }

    /**
     * 获取登记员名字
     */
    public static String getRegistrarName() {
        return QWQ_PREFERENCES.getString("RegistrarName", "").trim();
    }

    /**
     * 本地保存图书类目数据
     */
    public static void localSaveCategoryData() {
        Gson gson = new Gson();
        String json = gson.toJson(gCategoryBeans);
        QWQ_PREFERENCES.edit().putString("CategoryData", json).apply();
        // Log.d("", json);
    }

    /**
     * 获取本地的图书类目数据
     */
    public static ArrayList<CategoryBean> getLocalCategoryData() {
        String json = QWQ_PREFERENCES.getString("CategoryData", "[]").trim();
        ArrayList<CategoryBean> categories = new ArrayList<CategoryBean>();
        try {
            categories = (new Gson()).fromJson(json, new TypeToken<ArrayList<CategoryBean>>() {}.getType());
        } catch (Exception e) {
            Log.e("加载本地保存的图书类目", "出现错误", e);
        }

        return categories;
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