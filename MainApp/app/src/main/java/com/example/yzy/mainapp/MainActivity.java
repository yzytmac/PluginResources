package com.example.yzy.mainapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yzy.plugindev.PluginResources;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    private static final String PLUGIN_NAME = "app_plugin.apk";
    private static final String PLUGIN_PACKAGE_NAME = "com.example.yzy.pluginapp";
    private static final String FIELD_NAME = "plgin_name";
    private static final String DOWNLOAD_URL = "https://git.oschina.net/yzytmac/resource/raw/master/pluginDev/app_plugin.apk";
    private static final String INFO_URL = "https://git.oschina.net/yzytmac/resource/raw/master/pluginDev/VersionInfo.json";

    private TextView tv;
    private File mPluginApk;
    private String mPluginPath;
    private SharedPreferences mPreferences;
    private int mVersioncode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        mPreferences = getSharedPreferences("pluginSp", MODE_PRIVATE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(INFO_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    String jsonstring = streamToString(is);
                    JSONObject json = new JSONObject(jsonstring);
                    mVersioncode = json.getInt("versioncode");

                    Log.e("yzy", "mVersioncode:"+mVersioncode );
                    handler.sendEmptyMessage(0);
                } catch (Exception pE) {
                    pE.printStackTrace();
                }

            }
        }).start();
    }

    private String streamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            BufferedReader vBufferedReader = new BufferedReader(new InputStreamReader
                    (inputStream));
            StringBuilder vStringBuilder = new StringBuilder();
            String content = null;
            while ((content = vBufferedReader.readLine()) != null) {
                vStringBuilder.append(content);
            }
            return vStringBuilder.toString();
        }
        return null;
    }

    public void onClick(View pView) {
        mPluginPath = getCacheDir() + File.separator + PLUGIN_NAME;
        mPluginApk = new File(mPluginPath);
        if (!mPluginApk.exists()) {
            //下载,子线程中进行
            downloadPlugin();
        }
        if(mPreferences.getInt("versioncode",0)<mVersioncode) {
            //下载,子线程中进行
            downloadPlugin();
        }
        handler.sendEmptyMessage(2);

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(MainActivity.this, "比较完成"+mVersioncode, Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    try {
                        PluginResources vPluginResources = PluginResources.getPluginResources(MainActivity.this.getResources(), mPluginApk);
                        DexClassLoader vDexClassLoader = new DexClassLoader(mPluginApk.getAbsolutePath(), getDir(PLUGIN_NAME, Context.MODE_PRIVATE).getAbsolutePath(), null, getClassLoader());
                        Class<?> vClass = vDexClassLoader.loadClass(PLUGIN_PACKAGE_NAME + ".R$string");//这里是美元符$没错
                        Field[] vFields = vClass.getDeclaredFields();
                        for (Field vField : vFields) {
                            if (vField.getName().equals(FIELD_NAME)) {
                                int stringId = vField.getInt(R.string.class);//这里是拿string文件所以是R.string
                                String vString = vPluginResources.getString(stringId);
                                tv.setText(vString);
                            }
                        }
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                default:
            }

        }
    };

    private void downloadPlugin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    InputStream is = getResources().getAssets().open(PLUGIN_NAME);
                    URL url = new URL(DOWNLOAD_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    OutputStream os = new FileOutputStream(mPluginPath);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.close();
                    is.close();
                    SharedPreferences.Editor vEdit = mPreferences.edit();
                    vEdit.putInt("versioncode",mVersioncode).commit();
                    handler.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



}
