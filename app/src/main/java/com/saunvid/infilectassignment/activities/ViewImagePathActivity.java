package com.saunvid.infilectassignment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.saunvid.infilectassignment.adapter.ImgPathAdapter;
import com.saunvid.infilectassignment.R;
import com.saunvid.infilectassignment.remote.Api;
import com.saunvid.infilectassignment.remote.ApiListener;
import com.saunvid.infilectassignment.remote.ApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewImagePathActivity extends AppCompatActivity {

    List<String> list;
    private RecyclerView recyclerView;
    private ImgPathAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_path);

        list = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new ImgPathAdapter(list);
        recyclerView.setAdapter(mAdapter);

        getData();
    }

    private void getData() {
        String url = Api.BASE_URL + "Service1.svc/getImages";
        new ApiRequest(this).get(url, new ApiListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("CODE");
                    if (code.equalsIgnoreCase("200")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("DATA");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            list.add(obj.getString("name"));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (
                        JSONException e) {
                    e.getMessage();
                }
            }

            @Override
            public void onError(String ERROR, String errorMessage) {

            }
        });
    }
}
