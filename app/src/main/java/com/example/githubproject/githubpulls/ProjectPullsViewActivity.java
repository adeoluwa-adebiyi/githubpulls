package com.example.githubproject.githubpulls;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectPullsViewActivity extends AppCompatActivity {
    private String projectName,pullsUrl,response;
    private OkHttpClient client;
    private ArrayAdapter pullsViewAdapter;
    private ArrayList<PullsInfo> pullsInfoList;
    private ListView pullsInfoListView;
    private ProgressBar progressBar;

    public class PullsInfo{

        public String body,title,diffURL,userLogin,timeCreated,requestNo;
        public PullsInfo(String body, String title, String diffURL, String userLogin, String timeCreated, String requestNo){
            this.body = body; this.title = title; this.diffURL = diffURL; this.userLogin = userLogin; this.timeCreated = timeCreated; this.requestNo = requestNo;
        }
    }

    private class PullsInfoListAdapter extends ArrayAdapter{
        private LayoutInflater inflater;

        public PullsInfoListAdapter(@NonNull Context context, int resource, @NonNull List objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = inflater.inflate(R.layout.pulls_info_list_item,null);
            TextView title = view.findViewById(R.id.title);
//            TextView requestNo = view.findViewById(R.id.pull_request_no);
            TextView userLogin = view.findViewById(R.id.user_login);
            TextView timeCreated = view.findViewById(R.id.time);
            final PullsInfo pullsInfo = (PullsInfo)getItem(position);
            title.setText(pullsInfo.title);
//            requestNo.setText("#"+pullsInfo.requestNo);
            userLogin.setText(pullsInfo.userLogin);
            timeCreated.setText(pullsInfo.timeCreated);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        startDiffActivity(pullsInfo);
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Please check your Internet connection.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return view;
        }
    }

    private class GitHubPullsDataRetrieveTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Request request = new Request.Builder().url((String)objects[0]).get().build();
            Response response;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Please check your Internet connection.",Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            response = (String)o;
            JsonArray jsonArray = new JsonParser().parse(response).getAsJsonArray();
            pullsInfoList.clear();
            int count = 0;
            for(JsonElement element : jsonArray) {
                String requestNo = element.getAsJsonObject().get("number").getAsString();
               String body = element.getAsJsonObject().get("body").getAsString();
               String title = element.getAsJsonObject().get("title").getAsString();
               String diffURL = element.getAsJsonObject().get("diff_url").getAsString();
               //Toast.makeText(getApplicationContext(),diffURL,Toast.LENGTH_SHORT).show();
               String userLogin = element.getAsJsonObject().get("user").getAsJsonObject().get("login").getAsString();
               String timeCreated = element.getAsJsonObject().get("created_at").getAsString();
               pullsInfoList.add(new PullsInfo(body,title,diffURL,userLogin,timeCreated,requestNo));
               ++count;
            }
            if(count == 0){
                Toast.makeText(getApplicationContext(),"No available pulls for this repository",Toast.LENGTH_SHORT).show();
            }
            pullsViewAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            pullsInfoListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_pulls_view);
        client = new OkHttpClient();
        GitHubPullsDataRetrieveTask gitHubPullsDataRetrieveTask = new GitHubPullsDataRetrieveTask();
        pullsInfoList = new ArrayList<>();
        //pullsInfoList.add(new PullsInfo("kjfskjbfkjbkjen","sjb.knfkajnslkjfkdkbkfsjbdbfljblsdajbj","https://api.github.com/repos/shady/limevine/pulls","slimyK","27-09-2016","#153642"));
        pullsInfoListView = findViewById(R.id.pulls_view);

        progressBar = findViewById(R.id.second_progress);
        progressBar.setVisibility(View.VISIBLE);
        pullsViewAdapter = new PullsInfoListAdapter(this,R.layout.pulls_info_list_item,pullsInfoList);
        pullsInfoListView.setAdapter(pullsViewAdapter);
        projectName = this.getIntent().getStringExtra("full_name");
        pullsUrl = this.getIntent().getStringExtra("pull_url");
        getSupportActionBar().setTitle("Pulls - "+(projectName.length() > 18?projectName.substring(0,18)+"...":projectName));
        pullsUrl = pullsUrl.replace("{/number}","");
        //Toast.makeText(getApplicationContext(),pullsUrl,Toast.LENGTH_SHORT).show();
        gitHubPullsDataRetrieveTask.execute(pullsUrl);

    }

    public void startDiffActivity(PullsInfo pullsInfo){
        Bundle bundle = new Bundle();
        bundle.putCharSequence("diffUrl",pullsInfo.diffURL);
        bundle.putCharSequence("body",pullsInfo.body);
        bundle.putCharSequence("title",pullsInfo.title);
        Intent intent = new Intent(this,SplitDiffActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }
}
