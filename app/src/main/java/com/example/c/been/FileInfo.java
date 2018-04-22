package com.example.c.been;


import com.example.c.been.code.Column;
import com.example.c.been.code.Table;

@Table(table = "file_info")
public class FileInfo  {
    @Column(cname = "_id")
    private int id;

    @Column(cname = "file_name")
    private String fileName;//文件名

    @Column(cname = "url")
    private String url;//下载地址

    @Column(cname = "length")
    private int length = 0;//文件大小

    @Column(cname = "finished")
    private int finished = 0;//下载以已完成进度

    @Column(cname = "is_stop")
    private boolean isStop = false;//是否暂停下载

    @Column(cname = "is_downloading")
    private boolean isDownLoading = false;//是否下载到一半 false代表下载完成 或者 代表要下的信息和数据库不一致需要重新下载

    @Column(cname = "md5sum")
    private String md5sum;//文件的md5值

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public void addFinished(int length){
        finished += length;
    }
    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public boolean isDownLoading() {
        return isDownLoading;
    }

    public void setDownLoading(boolean downLoading) {
        isDownLoading = downLoading;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }
    public void setFilenameAndUrl(String url){
        this.url = url;
        this.fileName = url.substring(url.lastIndexOf('/')+1);
    }
    public String toString(){
        return "fileName:" + fileName
               + "; url:" + url
               + "; length:" + length
               + "; finished:" + finished
               + "; isStop:" + isStop
               + "; isDownLoading:" +isDownLoading
                +"; md5:" + md5sum;
    }


    public boolean equals(FileInfo fileInfo) {
        return this.url.equals(fileInfo.getUrl()) &&
                this.md5sum.equals(fileInfo.getMd5sum()) &&
                this.length == fileInfo.getLength() &&
                this.fileName.equals(fileInfo.getFileName());
    }
}
