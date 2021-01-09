package com.example.telegramhacker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.SQLOutput;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class watcherService extends Service {
    static final String CHANNEL_ID = "notification_channel_id";
    static final int imp = NotificationManager.IMPORTANCE_DEFAULT;

    static final String TAG = "watcherService";

    static NotificationChannel instanceOfNotificationChannel;
    static final String address = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separatorChar + "Telegram" +
            File.separatorChar + "Telegram Video";

    static private String logLocation = address + File.separatorChar + "telegram_hacker_logs.txt";

    static String TEMPORARY_CHANNEL_ID = "SOUND_NOTIFICATION_CHANNEL_ID";
    static final String nameIndex = "series_name";
    static final String countString = "count_number";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            File log = new File(address + File.separatorChar + "telegram_hacker_logs.txt");
            if (log.exists()){}
            else if(log.createNewFile()){
                try {
                    initiateJSONFile();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Log.e(TAG, "onStartCommand: Unable to create the log file . Might NOT have the storage permission." );
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onStartCommand: started...");

        Intent NotificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pd = PendingIntent.getActivity(this,0,NotificationIntent,0);

        NotificationChannel nc = new NotificationChannel(CHANNEL_ID,"Service Channel",imp);
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(nc);

        new Thread(new Runnable() {
            @Override
            public void run() {

//                updateLog("21");

                /*try {
                    Thread.currentThread().sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                notifyUser("Abc.txt");*/

                try {
                    realService();

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        Notification ntf = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Telegram Hacker")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(false)
                .setContentText("Currently watching over the files...")
                .build();

        startForeground(1,ntf);

        return START_NOT_STICKY;
    }

    static boolean checkNewAddition(){

        File logs = new File(logLocation);

//        System.out.println("The folder I am targetting is " + logs.getAbsolutePath());
        String listOfFiles[] = new File(address).list();

        System.out.println("List of files length = "  +listOfFiles.length);
        for (String str:listOfFiles){
            String end = str.substring(str.length()-3);
            System.out.println("File endings found to be "  +end);
            if (end.equals("mp4")){
                System.out.println("Found a video file");
                File ff = new File(str);
                long lastModifiedTime = ff.lastModified();

                long currentTime = System.currentTimeMillis();
                System.out.println("Last Modified of File is " + lastModifiedTime);
                System.out.println("Current time is " + currentTime);
                if (currentTime - lastModifiedTime < 10000){
                    System.out.println("File has been detected!!!");
                    return true;
                }
            }
        }
        return false;
    }

    boolean confirmMP4(String file){
        File subjectMP4File = new File(file);

        if (!file.substring(file.length()-3).equals("mp4")){
            System.out.println("Returned false as not an mp4 file");
            return false;
        }

        try {
            if (file.contains(getSeriesNameString())){return false;}
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, Uri.fromFile(subjectMP4File));
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMilliSec = Long.parseLong(duration);
//        System.out.println(subjectMP4File.getName() + " = " + timeInMilliSec + " milliseconds");
        if (timeInMilliSec > 62000){
            return true;
        }
        return false;
    }

    synchronized void realService() throws IOException,InterruptedException {
        Path pathToWatch = FileSystems.getDefault().getPath(watcherService.address);

        try (WatchService watchService = pathToWatch.getFileSystem().newWatchService()){
            Path dir = Paths.get(watcherService.address);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey key = watchService.take();

            do {
                for (final WatchEvent<?> event : key.pollEvents()){
                    Path name = (Path) event.context();
                    File filename = dir.resolve(name).toFile();
                    System.out.println(dir + " : " + event.kind() + " : " + event.context());

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                        try {
                            File ff = new File(dir.toString() + File.separatorChar + event.context());
                            System.out.println("Inside created entry ");

                            if (confirmMP4(ff.getAbsolutePath())){
                                System.out.println("Confirmed our targetted mp4 file");
                                {
                                    String num = getSeriesNumberString();

                                    File properlyNamedFile = new File(dir.toString() + File.separatorChar + getSeriesNameString() + "__" + num + ".mp4");
                                    ff.renameTo(properlyNamedFile);

                                    updateLog(num);

                                    FileSend.sendFile(properlyNamedFile.getAbsolutePath(),"");
                                    properlyNamedFile.delete();

                                    notifyUser(properlyNamedFile.getName());
                                }/*catch (IOException e){
                                    e.printStackTrace();
                                }*/
                            }
                            //Media Retriever Code
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }while (key.reset());

        }
    }

    void updateLog(String num){
        int numberOfFile = Integer.parseInt(num);

        try {
            numberOfFile++;
            changeSeriesNumber(numberOfFile);
            System.out.println("Updated the log");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void notifyUser(String fileName){

        System.out.println("Notifying users with notification");

//        createNotificationChannel();

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" +
                this.getPackageName() +
                File.separatorChar +
                "raw/custom_notification.mp3"
        );

        Uri test = Uri.parse("android.resource://com.example.telegramhacker/raw/custom_notification.mp3");

        instanceOfNotificationChannel  = new NotificationChannel(TEMPORARY_CHANNEL_ID,"sound_channel",NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
        instanceOfNotificationChannel.setSound(test,audioAttributes);
        instanceOfNotificationChannel.enableLights(true);
        instanceOfNotificationChannel.enableVibration(true);

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(instanceOfNotificationChannel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,TEMPORARY_CHANNEL_ID)
                .setContentText("Alert")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(fileName + " is ready to parcel");

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(808,builder.build());
    }

    static private String getStringFromLogs(){
        String ret = "";
        try {
            FileReader fr = new FileReader(logLocation);
            int i;

            while((i=fr.read()) != -1){
                ret = ret  + ((char)i);
    //                                        System.out.println("value = " + ((char)i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Read from log = " + ret);
        return ret;
    }

    static void updateProgress(Context context,String fileName,long sendPackets,long totalPackets){

        sendPackets = sendPackets * 100;

        int percentSent = (int)(sendPackets / totalPackets);

        instanceOfNotificationChannel.enableVibration(false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,TEMPORARY_CHANNEL_ID)
                .setContentTitle("Telegram Hacker")
                .setContentText(fileName +" : "+percentSent+" %")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(808,builder.build());

    }

    static void createNotificationChannel(){
        /*Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" +
                this.getPackageName() +
                File.separatorChar +
                "raw/custom_notification.mp3"
        );*/

        Uri test = Uri.parse("android.resource://com.example.telegramhacker/raw/custom_notification.mp3");

        instanceOfNotificationChannel  = new NotificationChannel(TEMPORARY_CHANNEL_ID,"sound_channel",NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
        instanceOfNotificationChannel.setSound(test,audioAttributes);
        instanceOfNotificationChannel.enableLights(true);
        instanceOfNotificationChannel.enableVibration(true);

    }

    static void writeStringtoLog(String jsonString){
        try {
            FileWriter fw = new FileWriter(logLocation);
            fw.write(jsonString);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private void initiateJSONFile() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("series_name","telehacker_series");
        jsonObject.put("count_number","1");

        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.get(nameIndex));
        System.out.println(jsonObject.get(countString));

        writeStringtoLog(jsonObject.toString());

    }

    static void changeSeriesName(String seriesName) throws JSONException {

        JSONObject jsonObject = new JSONObject(getStringFromLogs());

        jsonObject.put("series_name",seriesName);

        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.get(nameIndex));
        System.out.println(jsonObject.get(countString));

        writeStringtoLog(jsonObject.toString());
    }

    static void changeSeriesNumber(int number) throws JSONException {

        JSONObject jsonObject = new JSONObject(getStringFromLogs());

        jsonObject.put("count_number",number + "");

        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.get(nameIndex));
        System.out.println(jsonObject.get(countString));

        writeStringtoLog(jsonObject.toString());
    }

    static String getSeriesNameString() throws JSONException{
        JSONObject jsonObject = new JSONObject(getStringFromLogs());
        return (String)jsonObject.get(nameIndex);
    }

    static String getSeriesNumberString() throws JSONException{
        JSONObject jsonObject = new JSONObject(getStringFromLogs());
        return (String)jsonObject.get(countString);
    }

}
