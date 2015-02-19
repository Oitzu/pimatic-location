package de.blackoise.pimaticlocation;

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
import android.widget.Spinner;
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
    EditText textDeviceID;
    CheckBox autoRefresh;
    EditText textPort;
    Spinner spinnerProtocol;
    Spinner spinnerPriority;
    CheckBox writeLogfile;

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
        textDeviceID = (EditText) findViewById(R.id.editTextDeviceID);
        textPort = (EditText) findViewById(R.id.editTextPort);
        spinnerProtocol = (Spinner) findViewById(R.id.spinnerProtocol);
        writeLogfile = (CheckBox) findViewById(R.id.checkBoxLogfile);
        spinnerPriority = (Spinner) findViewById(R.id.spinnerPriority);

        textHost.setText(settings.getString("Host", "pimatic.example.org"));
        textInterval.setText(settings.getString("Interval", "600000"));
        textUser.setText(settings.getString("User", "admin"));
        textPassword.setText(settings.getString("Password", "admin"));
        autoRefresh.setChecked(settings.getBoolean("autoRefresh", true));
        textDeviceID.setText(settings.getString("DeviceID", android.os.Build.MODEL));
        textPort.setText(settings.getString("Port", "80"));

        if(settings.getString("Protocol", "http").equals("http"))
        {
            spinnerProtocol.setSelection(0);
        }
        else
        {
            spinnerProtocol.setSelection(1);
        }

        spinnerPriority.setSelection(settings.getInt("Priority", 0));

        writeLogfile.setChecked(settings.getBoolean("writeLogfile", true));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        textInterval.setText(settings.getString("Interval", "600000"));
        spinnerPriority.setSelection(settings.getInt("Priority", 0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_logfile:
                startActivity(new Intent(Settings.this, ShowLogfile.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void OnClickSave(View v) {
        RequestParams params = new RequestParams();

        doRequest(params);

    }



    public void doRequest(RequestParams params) {
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final String locationProvider = LocationManager.NETWORK_PROVIDER;
        final Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        Log.d("Port", textPort.getText().toString());
        Log.d("Protocol", spinnerProtocol.getSelectedItem().toString());
        final API api = new API(textHost.getText().toString().trim(), spinnerProtocol.getSelectedItem().toString(), textPort.getText().toString().trim(), textUser.getText().toString(), textPassword.getText().toString());
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
                                api.update_LinearDistance(textDeviceID.getText().toString(), getApplicationContext(), jsonParams, new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        Toast.makeText(getApplicationContext(), "Distance set.\nSaving settings.", Toast.LENGTH_LONG).show();

                                        settings.edit().putString("Host", textHost.getText().toString().trim()).apply();
                                        settings.edit().putString("Interval", textInterval.getText().toString()).apply();
                                        settings.edit().putString("User", textUser.getText().toString()).apply();
                                        settings.edit().putString("Password", textPassword.getText().toString()).apply();
                                        settings.edit().putBoolean("autoRefresh", autoRefresh.isChecked()).apply();
                                        settings.edit().putString("DeviceID", textDeviceID.getText().toString()).apply();
                                        settings.edit().putString("Protocol", spinnerProtocol.getSelectedItem().toString()).apply();
                                        settings.edit().putString("Port", textPort.getText().toString().trim()).apply();
                                        settings.edit().putBoolean("writeLogfile", writeLogfile.isChecked()).apply();
                                        if(autoRefresh.isChecked()) {
                                            Intent PLServiceIntent = new Intent(getApplicationContext(), PLService.class);
                                            getApplicationContext().startService(PLServiceIntent);
                                        }
                                        settings.edit().putInt("Priority", spinnerPriority.getSelectedItemPosition()).apply();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                        Toast.makeText(getApplicationContext(), "Couldn't set distance.\nPlease check the Device ID.", Toast.LENGTH_LONG).show();
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
