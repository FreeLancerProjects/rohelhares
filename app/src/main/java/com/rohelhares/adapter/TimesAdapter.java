package com.rohelhares.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.rohelhares.R;
import com.rohelhares.activity_map.MapActivity;
import com.rohelhares.databinding.TimeRowBinding;
import com.rohelhares.model.TimesModel;

import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class TimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String lang;
    private List<TimesModel> list;
    private Context context;
    private LayoutInflater inflater;
    private int i = -1;
    private MapActivity activity;

    public TimesAdapter(List<TimesModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);

        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        activity = (MapActivity) context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        TimeRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.time_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));
        myHolder.binding.setLang(lang);

        myHolder.binding.rbChoose1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = position;
                notifyDataSetChanged();
            }
        });
        if (i == position) {
            myHolder.binding.rbChoose1.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            activity.settime(list.get(i).getTitle());
        } else {
            myHolder.binding.rbChoose1.setTextColor(context.getResources().getColor(R.color.gray8));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        public TimeRowBinding binding;

        public MyHolder(@NonNull TimeRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


}
