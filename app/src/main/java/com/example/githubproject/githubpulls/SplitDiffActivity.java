package com.example.githubproject.githubpulls;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplitDiffActivity extends AppCompatActivity {
    private String title,url;
    private OkHttpClient client;
    ProgressBar progressBar;
    TextView bodyText;

    private ArrayList<TextView> generateColorHighlightsDisplay(ArrayList<String> lines){
        ArrayList<TextView> rendered = new ArrayList<TextView>();
        int count = 1;
        for(String line : lines){
            TextView textView = new TextView(this);
            textView.setText(line.trim());
            textView.setPadding(5,0,5,0);
            textView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setBackgroundColor(Color.WHITE);
            if(line.charAt(0) =='+'){
                textView.setBackgroundColor(Color.GREEN);
            }
            if(line.charAt(0) =='-'){
                textView.setBackgroundColor(Color.RED);
            }
            textView.setText(Integer.toString(count-1)+" "+line);
            rendered.add(textView);
            ++count;
        }
        rendered.get(0).setBackgroundColor(Color.BLACK);
        rendered.get(0).setTextColor(Color.WHITE);
        String[] items = rendered.get(0).getText().toString().replace("0 ","").split("/");
        rendered.get(0).setText(rendered.get(0).getText().toString().replace("0 ","").split("/")[items.length-1]);
        rendered.get(0).setPadding(5,5,5,5);
        return rendered;
    }

    private class GitHubSplitDifProcessingTask extends AsyncTask {
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
            ArrayList<Integer> points = new ArrayList<Integer>();
            LinearLayout parent = findViewById(R.id.parent);
            String response = (String)o;
            String lines[] = response.split("\n");
            ArrayList<ArrayList<String>> diffs = new ArrayList<ArrayList<String>>();
            Toast.makeText(getApplicationContext(),Integer.toString(lines.length),Toast.LENGTH_SHORT).show();
            for(int count = 0; count < lines.length;count++){
                //Toast.makeText(getApplicationContext(),"Run",Toast.LENGTH_SHORT).show();
                if(lines[count].trim().length() >= 4) {
                    if (lines[count].trim().substring(0, 4).equals("diff")) {
                        points.add(count);
                    }
                }
            }
            for(int iteration=0; iteration < points.size(); iteration++){
                ArrayList<String> sub_diff = new ArrayList<>();
                int begin = points.get(iteration);
                int end;
                String substring = "";
                if((iteration+1) >= points.size()) {
                    end = lines.length - 1;
                }else{
                    end = points.get(iteration+1);
                }
                for(int index = begin; index < end; index++){
                   sub_diff.add(lines[index]);
                }
                diffs.add(sub_diff);
            }
            for(ArrayList<String> subDiff : diffs){
                ArrayList<TextView> collection = generateColorHighlightsDisplay(subDiff);
                LinearLayout verticalLayout = new LinearLayout(getApplicationContext());
                CardView cardView = new CardView(getApplicationContext());
                verticalLayout.setOrientation(LinearLayout.VERTICAL);
                cardView.addView(verticalLayout);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(10,0,10,20);
                cardView.setLayoutParams(params);
                for(TextView line : collection){
                    verticalLayout.addView(line);
                }
                progressBar.setVisibility(View.INVISIBLE);
                parent.addView(cardView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_diff);
        this.bodyText = findViewById(R.id.body_text);
        this.bodyText.setText(this.getIntent().getStringExtra("body"));
        this.client = new OkHttpClient();
        this.title = this.getIntent().getStringExtra("title");
        this.url = this.getIntent().getStringExtra("diffUrl");
        GitHubSplitDifProcessingTask task = new GitHubSplitDifProcessingTask();
        task.execute(this.url);
        getSupportActionBar().setTitle("Diffs - "+title);
        progressBar = findViewById(R.id.diff_progress);
        progressBar.setVisibility(View.VISIBLE);
    }
}
