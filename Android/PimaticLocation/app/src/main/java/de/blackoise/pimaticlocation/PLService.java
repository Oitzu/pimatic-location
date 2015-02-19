package de.blackoise.pimaticlocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.google.android.gms.location.LocationListener;

import org.apache.http.Header;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileOutputStream;

/**
 * Created by Oitzu on 03.02.2015.
 */
public class PLService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private LocationManager locManager;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("PLService", "Connection to GoogleApiClient suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onCreate() {

        Log.v("PLService", "Service created.");
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        Log.v("PLService", "Connected to GoogleApiClient.");

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        Log.i("PLService", "Connection to GoogleApiClient failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {

        if(location!=null){
            writeLog("Location update received. Provider: " + location.getProvider());
            updateLocation(location);
            Log.v("Debug", "Location changed.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("PLService", "Service started.");
        final SharedPreferences settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            writeLog("Intent with type " + intent.getType()+" received.");
            if ("text/plain".equals(intent.getType())) {
                Bundle extras = intent.getExtras();
                if(extras.containsKey("android.intent.extra.INTERVAL"))
                {
                    settings.edit().putString("Interval",String.valueOf(extras.getInt("android.intent.extra.INTERVAL"))).apply();
                    writeLog("Setting new Interval "+ String.valueOf(extras.getInt("android.intent.extra.INTERVAL")) + " by intent.");
                }
                if(extras.containsKey("android.intent.extra.PRIORITY"))
                {
                    settings.edit().putInt("Priority", extras.getInt("android.intent.extra.PRIORITY")).apply();
                    writeLog("Setting new Priority "+ String.valueOf(extras.getInt("android.intent.extra.PRIORITY")) + " by intent.");
                }
            }
        }



        //mGoogleApiClient.connect();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        int Interval = Integer.parseInt(settings.getString("Interval", "600000"));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Interval);
        mLocationRequest.setFastestInterval(Interval/2);

        String accuracy;

        switch(settings.getInt("Priority", 0))
        {
            case 0:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                accuracy = "Balanced Power";
                break;
            case 1:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                accuracy = "High";
                break;
            case 2:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                accuracy = "Low";
                break;
            case 3:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                accuracy = "No";
                break;
            default:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                accuracy = "Balanced Power";
        }

        if(mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.reconnect();
        }
        else
        {
            mGoogleApiClient.connect();
        }

        writeLog("Starting service with interval of " + Integer.parseInt(settings.getString("Interval", "600000")) +"ms and " + accuracy + " Accuracy.");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    private void updateLocation(final Location lastKnownLocation)
    {
        final SharedPreferences settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);
        final API api = new API(settings.getString("Host", "pimatic.example.org"), settings.getString("Protocol", "http"), settings.getString("Port", "80"), settings.getString("User", "admin"), settings.getString("Password", "admin"));
        //first get latitude of pimatic
        JSONObject jsonParams = new JSONObject();
        api.get_variable("latitude", getApplicationContext(), jsonParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try
                {
                    //save latitude for later use
                    final JSONObject latitudeVariable = response.getJSONObject("variable");

                    //second get longitude of pimatic
                    JSONObject jsonParams = new JSONObject();
                    api.get_variable("longitude", getApplicationContext(), jsonParams, new JsonHttpResponseHandler() {
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
                                jsonParams.put("distance", distance);
                                api.update_LinearDistance(settings.getString("DeviceID", android.os.Build.MODEL), getApplicationContext(), jsonParams, new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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

                FileOutputStream fos = openFileOutput("logfile", Context.MODE_APPEND);

                fos.write(logLine.getBytes());

                fos.close();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }
}
