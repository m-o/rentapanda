package com.example.rendapanda;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements NetworkCallback{

    private String apiURL = "http://private-anon-927eb4af2-rentapanda.apiary-mock.com/jobs";

    OkHttpClient client = new OkHttpClient();

    // Create a RealmConfiguration which is to locate Realm file in package's "files" directory.
    RealmConfiguration realmConfig;
    // Get a Realm instance for this thread


    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    private JobsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        realmConfig = new RealmConfiguration.Builder(this.getApplicationContext()).build();
        realm = Realm.getInstance(realmConfig);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(1));

        adapter = new JobsAdapter(this);

        RealmResults<JobModel> jobs = realm.where(JobModel.class).findAll();
        List<JobModel> copy = realm.copyFromRealm(jobs);

        adapter.putData(copy);

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume(){
        super.onResume();
        try {
            requestJobList(this,this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestJobList(final Context context, final NetworkCallback callback) throws IOException {
            Request request = new Request.Builder()
                    .url(apiURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            NetworkErrorDialogFragment fragment = new NetworkErrorDialogFragment(context,callback);
                            fragment.show(getSupportFragmentManager(),"NetworkErrorFragment");
                        }
                    });
                }

                @Override
                public void onResponse(Call call,Response response) throws IOException {
                    final String myResponse = response.body().string();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            saveResponseToRealm(myResponse);
                            showData();
                        }
                    });
                }
                Handler mainHandler = new Handler(context.getMainLooper());
        });
    }


    private void saveResponseToRealm(String responseBody){
        Realm realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        realm.clear(JobModel.class);
        realm.createAllFromJson(JobModel.class,responseBody);
        realm.commitTransaction();
        realm.close();
    }

    private void showData(){
        Realm realm = Realm.getInstance(realmConfig);
        RealmResults<JobModel> jobs = realm.where(JobModel.class).findAll();
        jobs.sort("job_date");
        List<JobModel> copy = realm.copyFromRealm(jobs);
        adapter.putData(copy);
        realm.close();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    @Override
    public void loadData() {
        try {
            requestJobList(this,this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if(parent.getChildAdapterPosition(view) == 0)
                outRect.top = space;
        }
    }

}
