package blackoise.de.pimaticlocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Oitzu on 03.02.2015.
 */
public class PLService extends Service {
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

        updateLocation();

        stopSelf();

        return START_REDELIVER_INTENT;
    }

    private void updateLocation()
    {
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final String locationProvider = LocationManager.NETWORK_PROVIDER;
        final Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        final SharedPreferences settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);

        final API api = new API(settings.getString("Host", "pimatic.example.org"), settings.getString("User", "admin"), settings.getString("Password", "admin"));
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
}
