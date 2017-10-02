package com.qwqaq.libraryregister.Adapters;

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
import com.qwqaq.libraryregister.Beans.CategoryBean;
import com.qwqaq.libraryregister.R;

import java.util.ArrayList;

/**
 * Created by Zneia on 2017/9/26.
 */

public class CategoryAdapter extends ArrayAdapter<CategoryBean> {

    public CategoryAdapter(Context context, ArrayList<CategoryBean> categoryBeans) {
        super(context, R.layout.category_list_item, categoryBeans);
    }

    private static class ViewHolder {
        TextView name;
        TextView registrarName;
        ImageView dataStatus;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CategoryBean categoryBean = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.category_list_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.category_name);
            viewHolder.registrarName = (TextView) convertView.findViewById(R.id.category_registrar_name);
            viewHolder.dataStatus  = (ImageView) convertView.findViewById(R.id.data_status);
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

        viewHolder.name.setText(categoryBean.getName());
        viewHolder.registrarName.setText(categoryBean.getRegistrarName());

        if (App.Data.Local.containsKey(categoryBean.getName())) {
            viewHolder.dataStatus.setImageResource(R.drawable.ic_data_status_local);
        } else {
            viewHolder.dataStatus.setImageResource(R.drawable.ic_data_status_cloud);
        }

        return convertView;
    }
}