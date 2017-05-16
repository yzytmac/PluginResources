package com.yzy.plugindev;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by yzy on 17-5-15.
 */

public class PluginResources extends Resources {

    public PluginResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
    }

    /**
     * 这里就起到偷天换日的目的了，回去加载插件的资源
     * @param pluginApk
     * @return
     */
    private static AssetManager getPluginAssetManager(File pluginApk){
        //创建一个AssetManager，并执行addAssetPath方法，但是由于这个类到构造函数不可见，方法也不可见，所以就只能用反射到方式来
        try {
            Class<?> vForName = Class.forName("android.content.res.AssetManager");
            Method[] vMethods = vForName.getDeclaredMethods();
            for (Method vMethod : vMethods) {
                if (vMethod.getName().equals("addAssetPath")) {
                    AssetManager vAssetManager = AssetManager.class.newInstance();
                    vMethod.invoke(vAssetManager, pluginApk.getAbsolutePath());
                    return vAssetManager;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 此方法替代系统自带到getResources()方法，用来加载插件到资源
     * @param pResources 系统本身的res，因为要用到语言和屏幕分辨率等配置信息
     * @param pluginApk 插件apk
     * @return
     */
    public static PluginResources getPluginResources(Resources pResources,File pluginApk){
        AssetManager vAssetManager = getPluginAssetManager(pluginApk);
        PluginResources vPluginResources = new PluginResources(vAssetManager, pResources.getDisplayMetrics(), pResources.getConfiguration());
        return vPluginResources;
    }
}
