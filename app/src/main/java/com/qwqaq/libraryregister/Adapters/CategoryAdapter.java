package com.qwqaq.libraryregister.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CategoryBean bean = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.category_list_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.category_name);
            viewHolder.registrarName = (TextView) convertView.findViewById(R.id.category_registrar_name);
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

        viewHolder.name.setText(bean.getName());
        viewHolder.registrarName.setText(bean.getRegistrarName());
        return convertView;
    }
}