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

import de.repictures.stromberg.Helper.Internet;
import de.repictures.stromberg.LoginActivity;
import de.repictures.stromberg.ScanProductActivity;

public class GetProductAsyncTask extends AsyncTask<String, Void, String>{

    private ScanProductActivity scanProductActivity;
    private Internet internetHelper = new Internet();
    private String TAG = "GetProductAsyncTask";

    public GetProductAsyncTask(ScanProductActivity scanProductActivity){
        this.scanProductActivity = scanProductActivity;
    }

    @Override
    protected void onPreExecute() {
        if (!internetHelper.isNetworkAvailable(scanProductActivity)){
            cancel(true);
            onPostExecute("0");
        }
    }

    @Override
    protected String doInBackground(String... codes) {
        String baseUrl = LoginActivity.SERVERURL + "/postproducts?code=" + codes[0];
        return internetHelper.doGetString(baseUrl);
    }

    @Override
    protected void onPostExecute(String s) {
        if (s.length() == 1 && s.charAt(0) == '0'){
            //Produkt gibts nicht
            scanProductActivity.receiveResult();
        } else {
            String[] responsesRaw = s.split("ň");
            String[][] products = new String[responsesRaw.length][4];
            for (int i = 0; i < products.length; i++) {
                products[i] = responsesRaw[i].split("ò");
            }
            scanProductActivity.receiveResult(products);
        }
    }
}
