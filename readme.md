**插件化开发——加载插件中的资源文件**  
通常会有很多资源文件是动态变化的，做成一个插件的形式供应用加载，而插件的形式由很多种如dex、zip、jar、apk。其中最有含量的就是apk形式的插件，并且是非安装形式的。  
项目中有MainApp和PluginApp两个项目，PluginApp就是插件，里面有一个string资源叫plugin_name。我们要在MainApp中下载PluginApp并拿到其中的string文件并显示在textview中。  
  
----------------原理--------------------  
正常情况下我们在java代码中获取string文件的方式是context.getResources().getString(R.string.xxx);这样得到的就是笨应用中的资源。我们只需要把context.getResources()得到的resources变成插件中的resources即可拿到插件中的资源。包括xml、图片等res目录下的所有资源。详情见代码。  
如有错误欢迎指正。邮箱yzytmac@163.com
