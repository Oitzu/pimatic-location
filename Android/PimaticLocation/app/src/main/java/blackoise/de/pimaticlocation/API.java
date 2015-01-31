package blackoise.de.pimaticlocation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Oitzu on 25.01.2015.
 */
public class API {
    private AsyncHttpClient client = new AsyncHttpClient();
    String hostname;

    public API(String host, String user, String pw)
    {
        client.addHeader("Content-Type", "application/json");
        client.setBasicAuth(user, pw);
        hostname = host;
    }

    public void get(String variable, Context context, JSONObject params, AsyncHttpResponseHandler responseHandler) {
        try {
            StringEntity entity = new StringEntity(params.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            client.get(context, "http://" + hostname + "/api/variables/" + variable, entity, "application/json", responseHandler);
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

            client.patch(context, "http://"+ hostname + "/api/variables/"+variable, entity, "application/json", responseHandler);
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
