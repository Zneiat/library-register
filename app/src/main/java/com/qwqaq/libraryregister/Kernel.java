package com.qwqaq.libraryregister;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.libraryregister.Beans.CategoryBean;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Zneia on 2017/4/16.
 */

public class Kernel extends Application {

    // Preferences
    public static SharedPreferences QWQ_PREF;

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
        QWQ_PREF = getSharedPreferences("QWQ_PREF", Context.MODE_PRIVATE);
        // QWQ_PREF.edit().clear().apply(); // 清除所有内容

        // Data.dataPrefDel();
        Data.importPref();
    }

    /**
     * 数据操作
     */
    public static class Data {

        // 称为 BasicBeans, 基本数据，已存在于云端的数据，其他数据以这个为基础，用于程序默认启动 Activity 显示出全部项目
        public static ArrayList<CategoryBean> Basic = new ArrayList<CategoryBean>();

        // 称为 LocalBeans, 本地已编辑但未上传的类目，上传到云端后将清空
        public static HashMap<String, CategoryBean> Local = new HashMap<String, CategoryBean>();

        // Keys Of Data In Pref
        public static final String PrefKeyBasic = "DATA_BASIC";
        public static final String PrefKeyLocal = "DATA_LOCAL";
        public static final String PrefKeyRegistrarName = "REGISTRAR_NAME";

        /**
         * static 变量也称作静态变量，静态变量和非静态变量的区别是：静态变量被所有的对象所共享，在内存中只有一个副本，它当且仅当在类初次加载时会被初始化。
         * 而非静态变量是对象所拥有的，在创建对象的时候被初始化，存在多个副本，各个对象拥有的副本互不影响。
         *
         * @link http://www.cnblogs.com/dolphin0520/p/3799052.html
         */

        /**
         * 数据从本地存储加载到内存
         */
        public static void importPref() {
            String basicJson = QWQ_PREF.getString(PrefKeyBasic, "[]").trim();
            String localJson = QWQ_PREF.getString(PrefKeyLocal, "[]").trim();

            ArrayList<CategoryBean> basicObj;
            HashMap<String, CategoryBean> localObj;
            try {
                basicObj = (new Gson()).fromJson(basicJson, new TypeToken<ArrayList<CategoryBean>>() {}.getType());
                localObj = (new Gson()).fromJson(localJson, new TypeToken<HashMap<String, CategoryBean>>() {}.getType());
            } catch (Exception e) {
                Log.e("从本地存储加载数据", "出现错误", e);
                return;
            }

            // 应用到内存中
            Basic = basicObj;
            Local = localObj;
        }

        /**
         * 数据保存到本地存储
         */
        public static void dataPrefStore() {
            dataBasicPrefStore();
            dataLocalPrefStore();
        }

        /**
         * 数据从本地存储全部删除
         */
        public static void dataPrefDel() {
            QWQ_PREF.edit().remove(PrefKeyBasic).remove(PrefKeyLocal).apply();
        }

        /**
         * Basic 数据保存到本地存储
         */
        public static void dataBasicPrefStore() {
            String json = (new Gson()).toJson(Basic);
            // Log.d("CategoryBeansCloud JSON", json);
            QWQ_PREF.edit().putString(PrefKeyBasic, json).apply();
        }

        /**
         * Local 数据保存到本地存储
         */
        public static void dataLocalPrefStore() {
            String json = (new Gson()).toJson(Local);
            QWQ_PREF.edit().putString(PrefKeyLocal, json).apply();
        }

        /**
         * 获取 登记员名字
         */
        @NonNull
        public static String getRegistrarName() {
            return QWQ_PREF.getString(PrefKeyRegistrarName, "").trim();
        }

        /**
         * 设置 登记员名字
         */
        public static void setRegistrarName(String value) {
            QWQ_PREF.edit().putString(PrefKeyRegistrarName, value).apply();
        }
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