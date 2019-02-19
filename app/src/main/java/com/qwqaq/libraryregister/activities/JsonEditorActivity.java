package com.qwqaq.libraryregister.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.beans.CategoryBean;
import com.qwqaq.libraryregister.R;
import com.qwqaq.libraryregister.utils.JsonFormatUtil;

import java.util.HashMap;

public class JsonEditorActivity extends Activity {

    EditText mDataEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.json_editor_activity);

        mDataEditText = (EditText) findViewById(R.id.data_edit_text);

        JsonFormatUtil json = new JsonFormatUtil();
        String jsonStr = (new Gson()).toJson(App.Data.Local);
        String result = json.formatJson(jsonStr);

        mDataEditText.setText(result);

        showConfirm("警告", "这是一个开发者功能，可能前方有怪兽哟！确定要返回吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
    }

    @Override
    public void initView(int layoutResID) {
        super.initView(layoutResID);

        // 工具条
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTopToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_control_activity_top_tool_bar_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            HashMap<String, CategoryBean> localObj;
            try {
                localObj = (new Gson()).fromJson(mDataEditText.getText().toString().trim(), new TypeToken<HashMap<String, CategoryBean>>() {}.getType());
            } catch (Exception e) {
                showMsg("数据有误，无法保存");
                return true;
            }

            App.Data.Local.clear();
            if (localObj != null)
                App.Data.Local.putAll(localObj);
            App.Data.dataPrefStore();
            showMsg("数据已保存");
        }

        return true;
    }
}
