package ru.liner.shizukusamples;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.concurrent.TimeUnit;

import ru.liner.shizukusamples.shizuku.ShizukuService;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Usage variant #1
        ShizukuService
                .of(IPowerManager.class)
                .checkShizuku(new ShizukuService.Callback<IPowerManager>() {
                    @Override
                    public void whenReady(@NonNull ShizukuService<IPowerManager> service) {
                        IPowerManager powerManager = service.load(Context.POWER_SERVICE);
                        MaterialSwitch materialSwitch = findViewById(R.id.powerSaveSwitch);
                        materialSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                            try {
                                powerManager.setPowerSaveModeEnabled(b);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    }
                });

        //Usage variant #2
        findViewById(R.id.killBackgroundApps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ShizukuService
                            .of(IActivityManager.class)
                            .load(Context.ACTIVITY_SERVICE)
                            .killAllBackgroundProcesses();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }
}