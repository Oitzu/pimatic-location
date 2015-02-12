package blackoise.de.pimaticlocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Oitzu on 03.02.2015.
 */
public class PLService extends Service {
    private LocationManager locManager;
    private LocationListener locListener = new myLocationListener();

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.v("PLService", "Service created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("PLService", "Service started.");
        final SharedPreferences settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                settings.edit().putString("Interval", intent.getStringExtra(Intent.EXTRA_TEXT)).apply();
            }
        }

        writeLog("Starting service with interval of " + Integer.parseInt(settings.getString("Interval", "60000")) +"ms.");

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, Integer.parseInt(settings.getString("Interval", "60000")), locListener);
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        return START_STICKY;
    }

    private void updateLocation(final Location lastKnownLocation)
    {
        final SharedPreferences settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);
        final API api = new API(settings.getString("Host", "pimatic.example.org"), settings.getString("Protocol", "http"), settings.getString("Port", "80"), settings.getString("User", "admin"), settings.getString("Password", "admin"));
        //first get latitude of pimatic
        JSONObject jsonParams = new JSONObject();
        api.get("latitude", getApplicationContext(), jsonParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try
                {
                    //save latitude for later use
                    final JSONObject latitudeVariable = response.getJSONObject("variable");

                    //second get longitude of pimatic
                    JSONObject jsonParams = new JSONObject();
                    api.get("longitude", getApplicationContext(), jsonParams, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                //save longitude for later use
                                final JSONObject longitudeVariable = response.getJSONObject("variable");

                                //set Location to Location-Object
                                Location pimaticLocation = new Location("JSON");
                                pimaticLocation.setLatitude(latitudeVariable.getDouble("value"));
                                pimaticLocation.setLongitude(longitudeVariable.getDouble("value"));

                                Log.i("Location:", lastKnownLocation.toString());
                                Log.i("Location:", pimaticLocation.toString());

                                //calculate distance
                                final float distance = lastKnownLocation.distanceTo(pimaticLocation);

                                //update distance variable
                                JSONObject jsonParams = new JSONObject();
                                jsonParams.put("type", "value");
                                jsonParams.put("valueOrExpression", distance);
                                api.patch(settings.getString("Var", "distance"), getApplicationContext(), jsonParams, new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                      //  Toast.makeText(getApplicationContext(), "Distance set.\nSaving settings.", Toast.LENGTH_LONG).show();
/*
                                        settings.edit().putString("Host", textHost.getText().toString()).apply();
                                        settings.edit().putString("Interval", textInterval.getText().toString()).apply();
                                        settings.edit().putString("User", textUser.getText().toString()).apply();
                                        settings.edit().putString("Password", textPassword.getText().toString()).apply();
                                        settings.edit().putBoolean("autoRefresh", autoRefresh.isChecked()).apply();
                                        settings.edit().putString("Var", textVar.getText().toString()).apply(); */
                                        writeLog("Updated distance to " + distance +"m");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                     //   Toast.makeText(getApplicationContext(), "Couldn't set distance.\nPlease check '"+ textVar.getText().toString() +"'-variable.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                          //  Toast.makeText(getApplicationContext(), "Couldn't get pimatic location.\nPlease check 'Longitude'-variable.", Toast.LENGTH_LONG).show();
                        }

                    });

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
              //  Toast.makeText(getApplicationContext(), "Couldn't get pimatic location.\nPlease check config and 'Latitude'-variable.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void writeLog(String text)
    {
        final SharedPreferences settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);
        if(settings.getBoolean("writeLogfile", true))
        {
            try {
                String date = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()).toString();

                String logLine = date + ": " + text + "\n";

                FileOutputStream fos = openFileOutput("logfile", Context.MODE_WORLD_READABLE | Context.MODE_APPEND);

                fos.write(logLine.getBytes());

                fos.close();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }

    private class myLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

            if(location!=null){
                writeLog("Location update received. Provider: " + location.getProvider());
                updateLocation(location);
                Log.v("Debug", "Location changed.");
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

}
