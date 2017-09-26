package com.qwqaq.schoollibrarysignuptask.Activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.qwqaq.schoollibrarysignuptask.R;


/**
 * Created by Zneia on 2017/9/15.
 */

public abstract class Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    |--------------------------------------------------------------------------
    | 关于 APP 中主界面的控制
    |--------------------------------------------------------------------------
    */

    protected Toolbar mTopToolBar; // 顶部工具条

    protected void initView(int layoutResID) {
        setContentView(layoutResID);
        // init TopToolBar
        mTopToolBar = (Toolbar) findViewById(R.id.top_tool_bar);
        setSupportActionBar(mTopToolBar);
    }

    /**
     * TopToolBar Options 创建
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 将每一个图标颜色改为白色
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            Drawable drawable = item.getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

    /**
     * TopToolBar Options 点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return true;
    }
}