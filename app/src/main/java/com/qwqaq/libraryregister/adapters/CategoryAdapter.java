package com.qwqaq.libraryregister.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qwqaq.libraryregister.App;
import com.qwqaq.libraryregister.activities.CategoryActivity;
import com.qwqaq.libraryregister.beans.CategoryBean;
import com.qwqaq.libraryregister.R;
import com.qwqaq.libraryregister.utils.DateUtil;

import java.util.ArrayList;

/**
 * Created by Zneia on 2017/9/26.
 */

public class CategoryAdapter extends ArrayAdapter<CategoryBean> {

    private CategoryActivity activity;

    public CategoryAdapter(CategoryActivity categoryActivity, ArrayList<CategoryBean> categoryBeans) {
        super(categoryActivity, R.layout.category_list_item, categoryBeans);
        activity = categoryActivity;
    }

    private static class ViewHolder {
        TextView name;
        ImageView isMine;
        TextView registrarName;
        TextView updateDatetime;
        ImageView dataStatus;
        ImageView isComplete;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final CategoryBean categoryBean = (CategoryBean) getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.category_list_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.category_name);
            viewHolder.isMine = (ImageView) convertView.findViewById(R.id.category_is_mine);
            viewHolder.registrarName = (TextView) convertView.findViewById(R.id.category_registrar_name);
            viewHolder.updateDatetime = (TextView) convertView.findViewById(R.id.category_update_datetime);
            viewHolder.dataStatus  = (ImageView) convertView.findViewById(R.id.data_status);
            viewHolder.isComplete = (ImageView) convertView.findViewById(R.id.category_is_complete);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        LinearLayout listItem = new LinearLayout(getContext());
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater view = (LayoutInflater)getContext().getSystemService(inflater);

        view.inflate(R.layout.category_list_item, listItem, true);

        /* Head Part */
        viewHolder.name.setText(categoryBean.getName());

        // 这个类目是不是该我管的？
        if (categoryBean.getIsMine()) {
            viewHolder.isMine.setVisibility(View.VISIBLE);
        } else {
            viewHolder.isMine.setVisibility(View.GONE);
        }

        /* Desc Part */
        viewHolder.registrarName.setText(categoryBean.getRegistrarName());

        // 更新时间

        viewHolder.updateDatetime.setText(categoryBean.getUpdateChineseShortTime());

        // 当前状态
        if (App.Data.Local.containsKey(categoryBean.getName())) {
            viewHolder.dataStatus.setImageResource(R.drawable.ic_data_status_local);
        } else {
            viewHolder.dataStatus.setImageResource(R.drawable.ic_data_status_cloud);
        }

        // 是否 已完成？
        if (categoryBean.getRemarks() != null && categoryBean.getRemarks().contains("已完成")) {
            viewHolder.dataStatus.setVisibility(View.GONE);
            viewHolder.isComplete.setVisibility(View.VISIBLE);
        } else {
            viewHolder.isComplete.setVisibility(View.GONE);
            viewHolder.dataStatus.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}