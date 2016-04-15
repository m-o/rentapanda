package com.example.rendapanda;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JobsAdapter extends RecyclerView.Adapter {

    List<JobModel> jobs;
    Context context;

    public JobsAdapter(Context context){
        this.context = context;
    }

    class JobViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.job_item) RelativeLayout jobItem;
        @Bind(R.id.customer_name) TextView customerName;
        @Bind(R.id.job_date) TextView jobDate;
        @Bind(R.id.job_distance) TextView jobDistance;


        public JobViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void putData(List<JobModel> jobs){
        this.jobs = jobs;
        notifyDataSetChanged();
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_item, parent, false);
        JobViewHolder vh = new JobViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        JobViewHolder jobViewHolder = (JobViewHolder) holder;
        jobViewHolder.jobItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),DetailActivity.class);
                intent.putExtra("JOB_ID",jobs.get(position).getOrder_id());
                v.getContext().startActivity(intent);
            }
        });
        JobModel job = jobs.get(position);
        Date jobDate = job.getJob_date();
        SimpleDateFormat sm = new SimpleDateFormat("dd.MM.yyyy");
        String strDate = sm.format(jobDate);

        jobViewHolder.customerName.setText(job.getCustomer_name());
        jobViewHolder.jobDate.setText(strDate);

        if(job.getDistance().equals("")){
            jobViewHolder.jobDistance.setVisibility(View.GONE);
        }
        else{
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            Float distance = Float.parseFloat(job.getDistance());
            jobViewHolder.jobDistance.setText(df.format(distance) + " " + context.getResources().getString(R.string.distance_unit));
        }

    }

    @Override
    public int getItemCount() {
        if (jobs != null) {
            return jobs.size();
        }
        return 0;
    }

}
