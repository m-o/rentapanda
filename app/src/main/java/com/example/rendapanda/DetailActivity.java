package com.example.rendapanda;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    RealmConfiguration realmConfig;
    private Realm realm;

    MapView mapView;

    @Bind(R.id.customer_name) TextView customerName;
    @Bind(R.id.job_status) TextView jobStatus;
    @Bind(R.id.job_order_time) TextView orderTime;
    @Bind(R.id.job_order_id) TextView orderId;
    @Bind(R.id.job_date) TextView jobDate;
    @Bind(R.id.job_duration) TextView jobDuration;
    @Bind(R.id.job_recurrence) TextView jobRecurrence;
    @Bind(R.id.job_price) TextView jobPrice;
    @Bind(R.id.job_payment_method) TextView jobPaymentMethod;
    @Bind(R.id.job_street) TextView jobStreet;
    @Bind(R.id.job_city) TextView jobCity;
    @Bind(R.id.job_distance) TextView jobDistance;

    @Bind(R.id.job_extras_wrapper) LinearLayout jobExtrasWrapper;
    @Bind(R.id.job_extras) TextView jobExtras;

    JobModel job;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        String id = getIntent().getStringExtra("JOB_ID");

        realmConfig = new RealmConfiguration.Builder(this.getApplicationContext()).build();
        realm = Realm.getInstance(realmConfig);
        job = realm.where(JobModel.class)
                .equalTo("order_id", id).findAll().get(0);

        mapView = (MapView) findViewById(R.id.map);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
        setupValues(job);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(!job.getJob_latitude().equals("") || !job.getJob_longitude().equals("")) {
            Float lat = Float.parseFloat(job.getJob_latitude());
            Float lng = Float.parseFloat(job.getJob_longitude());
            LatLng position = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions().position(position).title("Job"));
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(position, 13);
            googleMap.moveCamera(cu);
        }
        else{
            mapView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        realm.close();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void setupValues(JobModel job){
        customerName.setText(job.getCustomer_name());
        jobStatus.setText(job.getStatus());

        orderTime.setText(job.getOrder_time());
        orderId.setText(job.getOrder_id());

        Date date = job.getJob_date();
        SimpleDateFormat sm = new SimpleDateFormat("dd.MM.yyyy");
        String strDate = sm.format(date);
        jobDate.setText(strDate);

        int duration = job.getOrder_duration();
        int durationText;
        if(duration == 1){
            durationText = R.string.duration_hour;
        }
        else{
            durationText = R.string.duration_hours;
        }
        jobDuration.setText(duration + " " +getResources().getString(durationText));

        int recurrencyString;
        switch(job.getRecurrency()){
            case 0:
                recurrencyString = R.string.recurrency_once;
                break;
            case 7:
                recurrencyString = R.string.recurrency_weekly;
                break;
            case 14:
                recurrencyString = R.string.recurrency_byweekly;
                break;
            case 28:
                recurrencyString = R.string.recurrency_monthly;
                break;
            default:
                recurrencyString = R.string.recurrency_once;
        }
        jobRecurrence.setText(getResources().getString(recurrencyString));

        String currency = getResources().getString(R.string.currency);
        jobPrice.setText(currency + " " + job.getPrice());
        jobPaymentMethod.setText(job.getPayment_method());
        jobStreet.setText(job.getJob_street());
        jobCity.setText(job.getJob_postalcode() + " " + job.getJob_city());
        if(job.getDistance().equals("")){
            jobDistance.setVisibility(View.GONE);
        }
        else{
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            Float distance = Float.parseFloat(job.getDistance());
            jobDistance.setText(df.format(distance) + " " + getResources().getString(R.string.distance_unit));
        }

        if(job.getExtras().equals("")){
            jobExtrasWrapper.setVisibility(View.GONE);
        }
        else{
            jobExtras.setText(job.getExtras().replace(";",", "));
        }

    }

}
