package com.xj.fastdfs_demo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFastDfs {
     //刚刚的配置文件位置
    public String conf_filename = "C:\\java\\software_eclipse\\fastdfs-demo\\src\\main\\resources\\fdfs_client.conf"; 

     //要上传的文件地址
    public String local_filename = "C:\\file\\image\\red.jpg";
    
    //虚拟文件名
    String fileInfo = "";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    /**
     * 上传文件
     */
    @Test
    public void testUpload() {

        try {
        	//初始化客户端
            ClientGlobal.init(conf_filename);
            
            TrackerClient tracker = new TrackerClient(); 
            TrackerServer trackerServer = tracker.getConnection(); 
            StorageServer storageServer = null;
            StorageClient storageClient = new StorageClient(trackerServer, storageServer); 
            //NameValuePair nvp = new NameValuePair("age", "18"); 
            //建立一个NameValuePair数组，用于存储传送的数据
            NameValuePair nvp [] = new NameValuePair[]{ 
                    new NameValuePair("age", "18"), 
                    new NameValuePair("sex", "male") 
            }; 
            String fileIds[] = storageClient.upload_file(local_filename, "jpg", nvp);

            System.out.println(fileIds.length); 
            fileInfo = fileIds[1];
            System.out.println("组名：" + fileIds[0]); 
            System.out.println("路径: " + fileIds[1]);

        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } catch (MyException e) { 
            e.printStackTrace(); 
        } 
    }
    
    /**
     * 下载文件
     */
    @Test
    public void testDownload() {
        try {
        	 // 初始化全局配置。加载一个配置文件
            ClientGlobal.init(conf_filename);
            // 创建一个TrackerClient对象
            TrackerClient tracker = new TrackerClient(); 
            // 创建一个TrackerServer对象。
            TrackerServer trackerServer = tracker.getConnection(); 
            // 声明一个StorageServer对象，
            StorageServer storageServer = null;
            // 获得StorageClient对象
            StorageClient storageClient = new StorageClient(trackerServer, storageServer); 
            byte[] b = storageClient.download_file("group1", "M00/00/00/wKh3gFyAwrOAbK8VAAE4pVkrll4022.jpg"); 
            System.out.println(b); 
            // 将下载的文件流保存
            IOUtils.write(b, new FileOutputStream("D:\\FastDFSDemo/"+UUID.randomUUID().toString()+".jpg"));
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
    
    /**
     * 获取上传文件时的名称
     */
    @Test
    public void testGetFileInfo(){ 
        try { 
            ClientGlobal.init(conf_filename);

            TrackerClient tracker = new TrackerClient(); 
            TrackerServer trackerServer = tracker.getConnection(); 
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer, storageServer); 
            FileInfo fi = storageClient.get_file_info("group1", "fileInfo"); 
            System.out.println("ip地址:"+fi.getSourceIpAddr()); 
            System.out.println("文件大小:"+fi.getFileSize()); 
            System.out.println("创建日期："+fi.getCreateTimestamp()); 
            System.out.println("CRC32值:"+fi.getCrc32()); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 

    /**
     * 获取文件名称
     */
    @Test
    public void testGetFileMate(){ 
        try { 
            ClientGlobal.init(conf_filename);

            TrackerClient tracker = new TrackerClient(); 
            TrackerServer trackerServer = tracker.getConnection(); 
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer, 
                    storageServer); 
            NameValuePair nvps [] = storageClient.get_metadata("group1", "fileInfo"); 
            for(NameValuePair nvp : nvps){ 
                System.out.println(nvp.getName() + ":" + nvp.getValue()); 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 

    /**
     * 删除文件
     */
    @Test
    public void testDelete(){ 
        try { 
            ClientGlobal.init(conf_filename);

            TrackerClient tracker = new TrackerClient(); 
            TrackerServer trackerServer = tracker.getConnection(); 
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer, 
                    storageServer); 
            int i = storageClient.delete_file("group1", "fileInfo"); 
            System.out.println( i==0 ? "删除成功" : "删除失败:"+i); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
}