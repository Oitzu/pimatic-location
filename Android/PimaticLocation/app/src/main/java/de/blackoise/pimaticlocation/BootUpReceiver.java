package de.blackoise.pimaticlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, PLService.class);
        context.startService(myIntent);
    }

}