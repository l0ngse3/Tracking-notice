package com.example.noticeappmanager.adapter;


import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.noticeappmanager.R;
import com.example.noticeappmanager.databinding.CardViewShowPackageLayoutBinding;
import com.example.noticeappmanager.enity.ItemPackage;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> implements Filterable {

    List<ItemPackage> itemPackageList = new ArrayList<>();
    List<ItemPackage> itemPackageFullList = new ArrayList<>();

    public List<ItemPackage> getItemPackageList() {
        return itemPackageList;
    }

    public List<ItemPackage> getItemPackageFullList() {
        return itemPackageFullList;
    }

    public List<ItemPackage> getPackageInfoArrayList() {
        return itemPackageList;
    }

    public void setPackageInfoArrayList(List<ItemPackage> packageInfoArrayList) {
        itemPackageList.addAll(packageInfoArrayList);
        itemPackageFullList.addAll(packageInfoArrayList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CardViewShowPackageLayoutBinding binding = CardViewShowPackageLayoutBinding.inflate(layoutInflater, parent, false);
        //        .inflate(R.layout.card_view_show_package_layout, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(itemPackageList.get(position));
    }



    @Override
    public int getItemCount() {
        return itemPackageList.size();
    }

    @Override
    public Filter getFilter() {
        return filteredList;
    }

    private Filter filteredList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            Log.d("Notify App", "performFiltering");
            List<ItemPackage> filterList = new ArrayList<>();

            if(charSequence.length()==0 || charSequence==null)
            {
                filterList.addAll(itemPackageFullList);
            }
            else{
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (ItemPackage item : itemPackageList)
                {
                    if(item.getPackageInfo().packageName.toLowerCase().contains(filterPattern))
                    {
                        filterList.add(item);
                    }
                }

                if(filterList.size() == 0){
                    for (ItemPackage item : itemPackageFullList)
                    {
                        if(item.getPackageInfo().packageName.toLowerCase().contains(filterPattern))
                        {
                            filterList.add(item);
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filterList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            itemPackageList.clear();
            itemPackageList.addAll( (List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardViewShowPackageLayoutBinding binding;

        public ViewHolder(@NonNull CardViewShowPackageLayoutBinding _binding) {
            super(_binding.getRoot());
            binding = _binding;

            binding.packageImageView.setOnClickListener(this);
            binding.packageTextView.setOnClickListener(this);
            binding.packageCheckBox.setOnClickListener(this);
            binding.packageCardView.setOnClickListener(this);
        }

        public void bind(ItemPackage item){
            binding.setItem(item);
        }

        void process(){
            binding.packageCheckBox.setChecked(!binding.packageCheckBox.isChecked());
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.packageCheckBox:
                case R.id.packageImageView:
                case R.id.packageTextView:
                case R.id.packageCardView:
                    process();
                    int position = this.getAdapterPosition();
                    Log.d("Notify App", "On click at " + position + " -- checkbox: " + binding.packageCheckBox.isChecked());
                    itemPackageList.get(position).setChoosen(!itemPackageList.get(position).isChoosen());
                    notifyItemChanged(position);
                    //itemPackageFullList.get(position).setChoosen(itemPackageList.get(position).isChoosen());
                    break;
            }
        }
    }
}