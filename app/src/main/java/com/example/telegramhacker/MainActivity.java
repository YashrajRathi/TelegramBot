package com.example.telegramhacker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    Button stopButton;

    Button nameChangeButton;
    Button numberChangeButton;

    static String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE/*,Manifest.permission.READ_EXTERNAL_STORAGE*/,Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        confirmPermissions();

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startService(new Intent(MainActivity.this,watcherService.class));

            }
        });

        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopService(new Intent(MainActivity.this,watcherService.class));

            }
        });

        nameChangeButton = findViewById(R.id.change_name);
        nameChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAlertBoxForNameChange();

            }
        });

        numberChangeButton = findViewById(R.id.change_number);
        numberChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAlertBoxForNumberChange();

            }
        });

    }

    void test() throws IOException, InterruptedException {
        Path pathToWatch = FileSystems.getDefault().getPath(watcherService.address);

        try (WatchService watchService = pathToWatch.getFileSystem().newWatchService()) {
            Path dir = Paths.get(watcherService.address);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey key = watchService.take();

            do {
                for (final WatchEvent<?> event : key.pollEvents()) {
                    Path name = (Path) event.context();
                    File filename = dir.resolve(name).toFile();
                    System.out.println(dir + " : " + event.kind() + " : " + event.context());

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            File ff = new File(dir.toString() + File.separatorChar + event.context());

                            //Media Retriever Code
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } while (key.reset());

        }
    }

    void showAlertBoxForNumberChange() {
        System.out.println("Video number needs to be changed.");

        try {
            String str = watcherService.getSeriesNumberString();

            TextInputEditText inputEditText = new TextInputEditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputEditText.setHint(str);
            inputEditText.setLayoutParams(lp);

            AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(true)
                    .setMessage("Enter the playlist episode number")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            try {
                                int value = Integer.parseInt(inputEditText.getText().toString());
                                watcherService.changeSeriesNumber(value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    })
                    .setCancelable(true)
                    .setView(inputEditText)
                    .create();
            ad.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        ad.setView(inputEditText);
//        ad.setView(inputEditTextSeriesPrefix);



        /*DialogInterface.OnClickListener dialogInterface = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };*/

        /*Button okButton = ad.findViewById(R.id.alert_box_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                System.out.println("Input text is " + series.getEditText().getText().toString());
            }

        });*/

    }

    void showAlertBoxForNameChange(){
        System.out.println("Video name needs to be changed.");

        try {
            String str = watcherService.getSeriesNameString();

            TextInputEditText inputEditText = new TextInputEditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputEditText.setHint(str);
            inputEditText.setLayoutParams(lp);

            AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(true)
                    .setMessage("Enter the playlist name")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            try {
                                watcherService.changeSeriesName(inputEditText.getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    })
                    .setCancelable(true)
                    .setView(inputEditText)
                    .create();
            ad.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void confirmPermissions(){

        ArrayList<String> expiredPermissions = new ArrayList<>();

        for (String str:permissions){
            if (ContextCompat.checkSelfPermission(MainActivity.this, str) !=
                    PackageManager.PERMISSION_GRANTED) {
                expiredPermissions.add(str);
            }
        }

        if (expiredPermissions.size() > 0){
            askPermissions(expiredPermissions.toArray(new String[0]));
        }

    }

    void askPermissions(String permissionsToAsk[]){

        requestPermissions(permissionsToAsk,1001);

    }

}