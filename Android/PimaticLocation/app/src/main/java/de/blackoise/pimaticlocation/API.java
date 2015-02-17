package de.blackoise.pimaticlocation;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

/**
 * Created by Oitzu on 25.01.2015.
 */
public class API {
    private AsyncHttpClient client = new AsyncHttpClient();
    String hostname;
    String protocol;
    String port;

    public API(String host, String proto, String Port, String user, String pw)
    {
        client.addHeader("Content-Type", "application/json");
        client.setBasicAuth(user, pw);
        hostname = host;
        protocol = proto;
        port = Port;
        if(protocol.equals("https"))
        {
            try {
                /// We initialize a default Keystore
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                // We load the KeyStore
                trustStore.load(null, null);
                // We initialize a new SSLSocketFacrory
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                // We set that all host names are allowed in the socket factory
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e)
            {

            }
        }
    }

    public void get_variable(String variable, Context context, JSONObject params, AsyncHttpResponseHandler responseHandler) {
        try {
            StringEntity entity = new StringEntity(params.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            client.get(context, protocol + "://" + hostname + ":" + port + "/api/variables/" + variable, entity, "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void set_LinearDistance(String deviceID, Context context, JSONObject params, AsyncHttpResponseHandler responseHandler) {
        try {
            StringEntity entity = new StringEntity(params.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            client.get(context, protocol + "://" + hostname + ":" + port + "/api/device/" + deviceID + "/updateLinearDistance", entity, "application/json", responseHandler);
            Log.d("API", protocol + "://" + hostname + ":" + port + "/api/device/" + deviceID + "/setLinearDistance");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void patch(String variable, Context context, JSONObject params, AsyncHttpResponseHandler responseHandler)
    {
        try
        {
            StringEntity entity = new StringEntity(params.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            client.patch(context, protocol + "://"+ hostname + ":" + port +  "/api/variables/"+variable, entity, "application/json", responseHandler);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
/*
    public static void create(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }
    */
}
