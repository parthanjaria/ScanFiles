package com.app.scanfiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class ReportActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportAdapter reportAdapter;
    TextView total_files;
    TextView total_files_size;
    TextView file_exe;
    TextView average;
    LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        layout = findViewById(R.id.layout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setFocusable(false);
        total_files = findViewById(R.id.total_files);
        file_exe = findViewById(R.id.file_exe);
        total_files_size = findViewById(R.id.total_files_size);
        average = findViewById(R.id.average);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        ArrayList<File> files = new ArrayList<>();
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        try {
            JSONArray jsonArray = new JSONArray(sharedPref.getString(getResources().getString(R.string.preference_files_names),"[]"));
            for(int i=0;i<jsonArray.length();i++)
            {
                File file = new File(jsonArray.getString(i));
                files.add(file);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reportAdapter = new ReportAdapter(files);
        recyclerView.setAdapter(reportAdapter);

        long totalFiles = sharedPref.getLong(getResources().getString(R.string.preference_total_files),0);
        double totalFilesSize = sharedPref.getFloat(getResources().getString(R.string.preference_total_files_size),0);
        try {
            JSONObject fileExe = new JSONObject(sharedPref.getString(getResources().getString(R.string.preference_file_exe),"{}"));
            Iterator<String> iterator = fileExe.keys();
            String msg = "";
            while (iterator.hasNext())
            {
                String exe = iterator.next();
                msg+=exe+" : "+fileExe.getInt(exe)+"\n";
            }
            file_exe.setText(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DecimalFormat form = new DecimalFormat("0.00");
        average.setText(form.format(totalFilesSize/totalFiles)+" MB");
        total_files.setText(totalFiles+"");
        total_files_size.setText(totalFilesSize+"");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.share)
        {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            Bitmap returnedBitmap = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(returnedBitmap);
            Drawable bgDrawable =layout.getBackground();
            if (bgDrawable!=null)
                bgDrawable.draw(canvas);
            else
                canvas.drawColor(Color.WHITE);
            layout.draw(canvas);

            Uri bmpUri = null;
            try {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + new Date().getTime() + ".png");
                FileOutputStream out = new FileOutputStream(file);
                returnedBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                bmpUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".core.my.provider", file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, "Checkout my scanning details");
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "send"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
