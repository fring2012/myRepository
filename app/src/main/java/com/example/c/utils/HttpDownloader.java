package com.example.c.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpDownloader {
    String line = null;
    StringBuffer strBuffer = new StringBuffer();
    BufferedReader bufferedReader = null;

    //下载小型的文档文件，返回文档的String字符串
    public String downloadFiles(String urlStr){
        try {
            InputStream inputStream = getInputStreamFromUrl(urlStr);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null){
                strBuffer.append(line+'\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return strBuffer.toString();
    }
    //可以下载任意文件，例如MP3，并把文件存储在制定目录（
    // -1：下载失败，0：下载成功，1：文件已存在）
    public int downloadFiles(String urlStr,String path,String fileName)  {
        File resultFile = null;
        try {
            FileUtils fileUtils = new FileUtils();
            if(fileUtils.isFileExist(fileName,path))
                return  1;
            InputStream inputStream = getInputStreamFromUrl(urlStr);
            resultFile = fileUtils.write2SDFromInput(fileName,path,inputStream);
            if(resultFile == null)
                return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }



    public InputStream getInputStreamFromUrl(String urlStr) throws IOException {
        //创建一个URL对象
        URL url = new URL(urlStr);
        //创建一个HTTP链接
        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
        //使用IO流获取数据
        InputStream inputStream = urlConn.getInputStream();
        return inputStream;

    }
}
