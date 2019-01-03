package com.example.githubproject.githubpulls;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private String response;
    private ProgressBar progressBar;
    private EditText queryEntry;
    private Button searchButton;
    private ListView projectListView;
    private String queryText;
    private ProjectListAdapter projectListAdapter;
    private ArrayList projectlist;

    public class ProjectInfo{
        public String projectTitle,projectFullName,projectPullURL;
        public ProjectInfo(String projectTitle, String projectFullName, String projectPullURL){
            this.projectTitle = projectTitle;
            this.projectFullName = projectFullName;
            this.projectPullURL = projectPullURL;
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public class ProjectListAdapter extends ArrayAdapter{
        private LayoutInflater inflater;

        public ProjectListAdapter(@NonNull Context context, int resource, @NonNull List objects) {
            super(context, resource, objects);
            this.inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = this.inflater.inflate(R.layout.project_result_item,null);
            TextView projectTitle = view.findViewById(R.id.project_title);
            TextView projectFullName = view.findViewById(R.id.project_fullname);
            final ProjectInfo projectInfo = (ProjectInfo)this.getItem(position);
            projectTitle.setText(projectInfo.projectTitle);
            projectFullName.setText(projectInfo.projectFullName);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startProjectPullsViewActivity(projectInfo);
                }
            });
            return view;
        }
    }

    private final OkHttpClient client = new OkHttpClient();

    private class GitHubSearchTask extends AsyncTask {
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
            try{
            JsonParser jsonParser = new JsonParser();
            JsonArray projectArray = jsonParser.parse(response).getAsJsonObject().get("items").getAsJsonArray();
            //Toast.makeText(getApplicationContext(),,Toast.LENGTH_SHORT).show();
            //projectlist = null;
            projectListAdapter.clear();
            projectlist.clear();
            //Toast.makeText(getApplicationContext(),projectArray.,Toast.LENGTH_SHORT).show();
            for(JsonElement  jsonElement : projectArray) {
                JsonObject projectObject = jsonElement.getAsJsonObject();
                String pullsURL = projectObject.get("pulls_url").getAsString();
                String projectFullName = projectObject.get("full_name").getAsString();
                String projectTitle = projectObject.get("name").getAsString();
                //Toast.makeText(getApplicationContext(),pullsURL,Toast.LENGTH_SHORT).show();
                projectlist.add(new ProjectInfo(projectTitle,projectFullName,pullsURL));
            }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"No available repositories.",Toast.LENGTH_SHORT).show();
            }
            projectListAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            projectListView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        projectListView = findViewById(R.id.project_list_view);
        this.progressBar = findViewById(R.id.progress);
        queryEntry = findViewById(R.id.query);
        searchButton = findViewById(R.id.view_search_results);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startSearchResultActivity(new JSONArray());
                queryText = queryEntry.getText().toString();
                if(queryText != "" && haveNetworkConnection()) {
                    projectListView.setVisibility(View.INVISIBLE);
                    GitHubSearchTask task = new GitHubSearchTask();
                    queryText = queryText.replace(" ","%20");
                    try {
                        task.execute("https://api.github.com/search/repositories?q=" + queryText + "&sort=stars&order=desc");
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Please check your Internet connection.",Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.VISIBLE);
                }else{
                    if(!haveNetworkConnection()){
                        Toast.makeText(getApplicationContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Please enter a project name.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        this.projectlist = new ArrayList<ProjectInfo>();
//        for(int i = 0; i < 10;i++) {
//            projectlist.add(new ProjectInfo("tensorflow/tensorflow", "Tensorflow","https://api.github.com/repos/tensorflow/tensorflow/pulls"));
//        }
        this.projectListAdapter = new ProjectListAdapter(getApplicationContext(),R.layout.project_result_item,projectlist);
        projectListView.setAdapter(projectListAdapter);
//      android.support.v7.widget.SearchView searchView = findViewById(R.id.search_widget);
    }

    protected void startProjectPullsViewActivity(ProjectInfo projectInfo){
        Bundle projectDetails = new Bundle();
        projectDetails.putString("title",projectInfo.projectTitle);
        projectDetails.putString("full_name",projectInfo.projectFullName);
        projectDetails.putString("pull_url",projectInfo.projectPullURL);
        Intent intent = new Intent(this, ProjectPullsViewActivity.class);
        intent.putExtras(projectDetails);
        startActivity(intent);
    }
}
