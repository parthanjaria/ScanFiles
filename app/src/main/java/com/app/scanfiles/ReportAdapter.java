package com.app.scanfiles;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.MyViewHolder> {

    ArrayList<File> files;
    DecimalFormat form ;

    public ReportAdapter(ArrayList<File> files) {
        this.files = files;
        form = new DecimalFormat("0.00");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_row, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindView(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView file_name;
        TextView file_size;
        public MyViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.file_name);
            file_size = itemView.findViewById(R.id.file_size);
        }
        public void bindView(File file)
        {
            file_name.setText(file.getName());
            file_size.setText(form.format(file.length()/1000000.0)+" MB");
        }
    }
}
