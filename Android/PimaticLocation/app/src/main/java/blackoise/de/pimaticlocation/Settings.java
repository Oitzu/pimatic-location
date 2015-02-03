package blackoise.de.pimaticlocation;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class Settings extends Activity {

    SharedPreferences settings;
    EditText textHost;
    EditText textInterval;
    EditText textUser;
    EditText textPassword;
    EditText textVar;
    CheckBox autoRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

     //   settings = getPreferences(0);
        settings = getSharedPreferences("de.blackoise.pimaticlocation", MODE_PRIVATE);
        textHost = (EditText) findViewById(R.id.editTextHost);
        textInterval = (EditText) findViewById(R.id.editTextInterval);
        textUser = (EditText) findViewById(R.id.editTextUser);
        textPassword = (EditText) findViewById(R.id.editTextPassword);
        autoRefresh = (CheckBox) findViewById(R.id.checkBoxRefresh);
        textVar = (EditText) findViewById(R.id.editTextVar);

        textHost.setText(settings.getString("Host", "pimatic.example.org"));
        textInterval.setText(settings.getString("Interval", "5"));
        textUser.setText(settings.getString("User", "admin"));
        textPassword.setText(settings.getString("Password", "admin"));
        autoRefresh.setChecked(settings.getBoolean("autoRefresh", true));
        textVar.setText(settings.getString("Var", "distance"));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OnClickSave(View v) {
        RequestParams params = new RequestParams();

        doRequest(params);

    }



    public void doRequest(RequestParams params) {
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final String locationProvider = LocationManager.NETWORK_PROVIDER;
        final Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        final API api = new API(textHost.getText().toString(), textUser.getText().toString(), textPassword.getText().toString());
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
                                api.patch(textVar.getText().toString(), getApplicationContext(), jsonParams, new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        Toast.makeText(getApplicationContext(), "Distance set.\nSaving settings.", Toast.LENGTH_LONG).show();

                                        settings.edit().putString("Host", textHost.getText().toString()).apply();
                                        settings.edit().putString("Interval", textInterval.getText().toString()).apply();
                                        settings.edit().putString("User", textUser.getText().toString()).apply();
                                        settings.edit().putString("Password", textPassword.getText().toString()).apply();
                                        settings.edit().putBoolean("autoRefresh", autoRefresh.isChecked()).apply();
                                        settings.edit().putString("Var", textVar.getText().toString()).apply();

                                        Intent PLServiceIntent = new Intent(getApplicationContext(), PLService.class);
                                        PendingIntent PLServicePendingIntent = PendingIntent.getService(getApplicationContext(), 0, PLServiceIntent, 0);

                                        long interval = DateUtils.MINUTE_IN_MILLIS * Integer.parseInt(settings.getString("Interval", "5"));
                                        long firstStart = System.currentTimeMillis() + interval;
                                        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                        am.setRepeating(AlarmManager.RTC, firstStart, interval, PLServicePendingIntent);
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                        Toast.makeText(getApplicationContext(), "Couldn't set distance.\nPlease check '"+ textVar.getText().toString() +"'-variable.", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "Couldn't get pimatic location.\nPlease check 'Longitude'-variable.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Couldn't get pimatic location.\nPlease check config and 'Latitude'-variable.", Toast.LENGTH_LONG).show();
            }
        });
    }

}
