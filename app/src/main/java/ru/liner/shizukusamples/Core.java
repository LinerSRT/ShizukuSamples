package ru.liner.shizukusamples;

import android.app.Application;
import android.content.Context;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

/**
 * Author: Line'R
 * E-mail: serinity320@mail.com
 * Github: https://github.com/LinerSRT
 * Date: 17.09.2023, 17:18
 */
public class Core extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HiddenApiBypass.addHiddenApiExemptions("L");
    }
}
