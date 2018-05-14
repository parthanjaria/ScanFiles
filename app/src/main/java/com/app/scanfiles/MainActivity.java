package com.app.scanfiles;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.glomadrian.dashedcircularprogress.DashedCircularProgress;

import org.json.JSONArray;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DashedCircularProgress progress;
    TextView fileName;
    TextView totalFiles;
    Button scanBtn;
    Button reportBtn;
    BroadcastReceiver broadcastReceiver;
    ArrayList<String> stringArrayList;
    boolean isScanFinished = false;
    double totalFileSize;
    long totalFiles_;
    String fileName_text;
    long megaUsed;

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter(ReadFileService.RESULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("TESTING : onCreate");
        setContentView(R.layout.activity_main);
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalSize = (long)stat.getBlockCount()
                * (long)stat.getBlockSize();
        long availableSize = (long)stat.getAvailableBlocks()
                * (long)stat.getBlockSize();
        long usedSize = totalSize - availableSize;
        megaUsed = usedSize / 1048576;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                totalFiles_ = intent.getLongExtra(ReadFileService.TOTAL_FILES,0);
                totalFileSize = intent.getDoubleExtra(ReadFileService.TOTAL_FILE_SIZE,0);

                totalFiles.setText(totalFiles_+"\nFiles Scanned");
                if(intent.hasExtra(ReadFileService.FILENAME)) {
                    fileName_text = intent.getStringExtra(ReadFileService.FILENAME);
                    fileName.setText("File : "+fileName_text);
                    progress.setValue((float) ((totalFileSize/megaUsed)*100.0f));
                }else if(intent.hasExtra(ReadFileService.FINISHED))
                {
                    isScanFinished = true;
                    stringArrayList = intent.getStringArrayListExtra(ReadFileService.FINISHED);
                    JSONArray  jsonArray = new JSONArray(stringArrayList);
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getResources().getString(R.string.preference_files_names), jsonArray.toString());
                    editor.putLong(getResources().getString(R.string.preference_total_files), totalFiles_);
                    editor.putFloat(getResources().getString(R.string.preference_total_files_size), (float) totalFileSize);
                    editor.putString(getResources().getString(R.string.preference_file_exe), intent.getStringExtra(ReadFileService.FILEEXE));
                    editor.apply();
                    scanBtn.setText(MainActivity.this.getResources().getString(R.string.start));
                    progress.setValue(100);
                    if(intent.getBooleanExtra(ReadFileService.STOPPED,true))
                    Toast.makeText(MainActivity.this,"Scanning complete, Please check Report",Toast.LENGTH_SHORT).show();
                }
            }
        };
        progress = findViewById(R.id.progress);
        fileName = findViewById(R.id.file_name);
        totalFiles = findViewById(R.id.total_files);
        scanBtn = findViewById(R.id.scan_btn);
        reportBtn = findViewById(R.id.report_btn);
        scanBtn.setOnClickListener(this);
        reportBtn.setOnClickListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ReadFileService.TOTAL_FILES,totalFiles_);
        outState.putDouble(ReadFileService.TOTAL_FILE_SIZE,totalFileSize);
        outState.putString(ReadFileService.FILENAME,fileName_text);
        outState.putString(ReadFileService.FILENAME,fileName_text);
        outState.putStringArrayList(ReadFileService.FINISHED,stringArrayList);
        outState.putBoolean("isScanFinished",isScanFinished);
        System.out.println("TESTING : onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        totalFiles_ = savedInstanceState.getLong(ReadFileService.TOTAL_FILES,0);
        totalFileSize = savedInstanceState.getDouble(ReadFileService.TOTAL_FILE_SIZE,0);
        fileName_text = savedInstanceState.getString(ReadFileService.FILENAME);
        stringArrayList = savedInstanceState.getStringArrayList(ReadFileService.FINISHED);
        isScanFinished = savedInstanceState.getBoolean("isScanFinished");
        if(isScanFinished)
        progress.setValue(100);
        else
        progress.setValue((float) ((totalFileSize/megaUsed)*100.0f));
        totalFiles.setText(totalFiles_+"\nFiles Scanned");
        System.out.println("TESTING : onRestoreInstanceState");
    }

    private void scan() {
        Intent intentService = new Intent(MainActivity.this, ReadFileService.class);
        startService(intentService);
        scanBtn.setText(getResources().getString(R.string.stop));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100)
        {
            boolean granted = true;
            for (int i=0;i<grantResults.length;i++)
            {
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    granted = false;
                    break;
                }
            }
            if(granted)
            {
                scan();
            }else
            {
                Toast.makeText(this,"Permission Not Granted!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.scan_btn :
                isScanFinished = false;
                if (scanBtn.getText().toString().equalsIgnoreCase("START")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    100);
                        } else {
                            scan();
                        }
                    }else
                    {
                        scan();
                    }
                } else {
                    Intent intentService = new Intent(MainActivity.this, ReadFileService.class);
                    stopService(intentService);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(100);
                    scanBtn.setText(getResources().getString(R.string.start));
                }
                break;
            case R.id.report_btn :
                if(isScanFinished) {
                    Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                    startActivity(intent);
                }else
                {
                    Toast.makeText(this,"No Report to show, let's scan",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intentService = new Intent(MainActivity.this, ReadFileService.class);
        stopService(intentService);
        finish();
    }
}
