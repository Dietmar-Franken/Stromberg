package de.repictures.stromberg.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import de.repictures.stromberg.Features.AddProductActivity;
import de.repictures.stromberg.Helper.Internet;
import de.repictures.stromberg.LoginActivity;

public class PostProductAsyncTask extends AsyncTask<String, Void, String>{

    private AddProductActivity addProductActivity;
    private String TAG = "PostProductAsyncTask";
    private Internet internetHelper = new Internet();

    public PostProductAsyncTask(AddProductActivity addProductActivity){
        this.addProductActivity = addProductActivity;
    }

    @Override
    protected void onPreExecute() {
        if (!internetHelper.isNetworkAvailable(addProductActivity)){
            cancel(true);
            onPostExecute("-1");
        }
    }

    @Override
    protected String doInBackground(String... productInfos) {
        try {
            for (int i = 0; i < productInfos.length; i++) {
                productInfos[i] = URLEncoder.encode(productInfos[i], "UTF-8");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        String baseUrl = LoginActivity.SERVERURL + "/getproduct?code=" + productInfos[0]
                + "&name=" + productInfos[1]
                + "&price=" + productInfos[2]
                + "&accountnumber=" + productInfos[3]
                + "&selfbuy=" + productInfos[4];
        return new Internet().doGetString(baseUrl);
    }

    @Override
    protected void onPostExecute(String response) {
        addProductActivity.processResult(Integer.parseInt(response));
    }
}
