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
     //�ոյ������ļ�λ��
    public String conf_filename = "C:\\java\\software_eclipse\\fastdfs-demo\\src\\main\\resources\\fdfs_client.conf"; 

     //Ҫ�ϴ����ļ���ַ
    public String local_filename = "C:\\file\\image\\red.jpg";
    
    //�����ļ���
    String fileInfo = "";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    /**
     * �ϴ��ļ�
     */
    @Test
    public void testUpload() {

        try {
        	//��ʼ���ͻ���
            ClientGlobal.init(conf_filename);
            
            TrackerClient tracker = new TrackerClient(); 
            TrackerServer trackerServer = tracker.getConnection(); 
            StorageServer storageServer = null;
            StorageClient storageClient = new StorageClient(trackerServer, storageServer); 
            //NameValuePair nvp = new NameValuePair("age", "18"); 
            //����һ��NameValuePair���飬���ڴ洢���͵�����
            NameValuePair nvp [] = new NameValuePair[]{ 
                    new NameValuePair("age", "18"), 
                    new NameValuePair("sex", "male") 
            }; 
            String fileIds[] = storageClient.upload_file(local_filename, "jpg", nvp);

            System.out.println(fileIds.length); 
            fileInfo = fileIds[1];
            System.out.println("������" + fileIds[0]); 
            System.out.println("·��: " + fileIds[1]);

        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } catch (MyException e) { 
            e.printStackTrace(); 
        } 
    }
    
    /**
     * �����ļ�
     */
    @Test
    public void testDownload() {
        try {
        	 // ��ʼ��ȫ�����á�����һ�������ļ�
            ClientGlobal.init(conf_filename);
            // ����һ��TrackerClient����
            TrackerClient tracker = new TrackerClient(); 
            // ����һ��TrackerServer����
            TrackerServer trackerServer = tracker.getConnection(); 
            // ����һ��StorageServer����
            StorageServer storageServer = null;
            // ���StorageClient����
            StorageClient storageClient = new StorageClient(trackerServer, storageServer); 
            byte[] b = storageClient.download_file("group1", "M00/00/00/wKh3gFyAwrOAbK8VAAE4pVkrll4022.jpg"); 
            System.out.println(b); 
            // �����ص��ļ�������
            IOUtils.write(b, new FileOutputStream("D:\\FastDFSDemo/"+UUID.randomUUID().toString()+".jpg"));
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
    
    /**
     * ��ȡ�ϴ��ļ�ʱ������
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
            System.out.println("ip��ַ:"+fi.getSourceIpAddr()); 
            System.out.println("�ļ���С:"+fi.getFileSize()); 
            System.out.println("�������ڣ�"+fi.getCreateTimestamp()); 
            System.out.println("CRC32ֵ:"+fi.getCrc32()); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 

    /**
     * ��ȡ�ļ�����
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
     * ɾ���ļ�
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
            System.out.println( i==0 ? "ɾ���ɹ�" : "ɾ��ʧ��:"+i); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
}