package com.starglare.accasy.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.starglare.accasy.R;
import com.starglare.accasy.core.Helper;
import com.starglare.accasy.models.SubcategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<SubcategoryModel> mValues;
    private List<SubcategoryModel> copy;
    private int selectedItem = -1;
    private  CategorySelectCallback callback;

    public CategoryAdapter(List<SubcategoryModel> mValues, CategorySelectCallback callback) {
        this.mValues = mValues;
        this.copy = new ArrayList<>();
        copy.addAll(mValues);
        this.callback = callback;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.report_subgategory, viewGroup, false);
        return new CategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryAdapter.ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.textView.setText(mValues.get(position).getSubcategory());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), "Recycle list clicked", Toast.LENGTH_SHORT).show();
                selectedItem = position;
                callback.setCategory(holder.mItem.getCategory());
                callback.setSubCategory(holder.mItem.getSubcategory());
                Log.i(CategoryAdapter.class.getName(), "Category and Subcategory set!");
                notifyDataSetChanged();
            }
        });

        if(selectedItem == position){
            holder.textView.setBackgroundColor(Color.parseColor("#191B1F"));
            holder.textView.setTextColor(Color.parseColor("#ffffff"));
        }
        else
        {
            holder.textView.setBackgroundColor(Color.parseColor("#ffffff"));
            holder.textView.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void resetAdapter() {
        mValues.clear();
        for(SubcategoryModel subcategoryModel : copy) {
            mValues.add(subcategoryModel);
        }
        selectedItem = -1;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView textView;

        public SubcategoryModel mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            textView = view.findViewById(R.id.subcat_title);
        }
    }

    public void filterSubcategory(String category) {
        mValues.clear();
        for(SubcategoryModel subcategoryModel : copy) {
            if(subcategoryModel.getCategory().toLowerCase().equals(category.toLowerCase()))
                mValues.add(subcategoryModel);
        }
        selectedItem = -1;
        notifyDataSetChanged();
    }

    public interface CategorySelectCallback {
        void setCategory(String category);

        void setSubCategory(String subCategory);
    }
}
