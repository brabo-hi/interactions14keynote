package com.inin.phoneapplication.app;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import android.util.Log;
/**
 * Created by kevin.glinski on 3/25/14.
 */
public class ConnectionService {

    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        InputStream in = entity.getContent();


        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n>0) {
            byte[] b = new byte[4096];
            n =  in.read(b);


            if (n>0) out.append(new String(b, 0, n));
        }


        return out.toString();
    }


    public IcwsClient connect(String user, String password, String server){
        HttpClient httpClient = new DefaultHttpClient();

        HttpContext localContext = new BasicHttpContext();
        AppLog.d("","Connecting to " + server);
        HttpPost post = new HttpPost(String.format("%s/icws/connection", server));
        String text = null;

        try {
            JSONObject data = new JSONObject();
            data.put("__type", "urn:inin.com:connection:icAuthConnectionRequestSettings");
            data.put("applicationName", "Interactions Demo");
            data.put("userID", user);
            data.put("password", password);

            String dataString = data.toString();
            post.setEntity(new ByteArrayEntity(dataString.getBytes("UTF8")));

            post.setHeader("Accept-Language", "en-us");
            HttpResponse response = httpClient.execute(post, localContext);
            HttpEntity entity = response.getEntity();

            text = getASCIIContentFromEntity(entity);
            JSONObject obj = new JSONObject(text);

            String csrfToken = obj.getString("csrfToken");
            String cookie = response.getFirstHeader("set-cookie").getValue();
            String sessionId = obj.getString("sessionId");
            AppLog.d("","Connected Session: " + sessionId);

            IcwsClient client = new IcwsClient(server,sessionId,cookie,csrfToken);
            return client;

        } catch (Exception e) {
            AppLog.d("","unable to connect to the server " + e);
            return null;// e.getLocalizedMessage();
        }
    }
}