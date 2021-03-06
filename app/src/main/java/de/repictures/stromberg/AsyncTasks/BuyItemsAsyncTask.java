package de.repictures.stromberg.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.repictures.stromberg.Helper.Internet;
import de.repictures.stromberg.LoginActivity;
import de.repictures.stromberg.ScanProductActivity;

public class BuyItemsAsyncTask  extends AsyncTask<String, Void, String> {

    private static final String TAG = "BuyItemsAsyncTask";
    private ScanProductActivity scanProductActivity;
    private Internet internet = new Internet();

    public BuyItemsAsyncTask(ScanProductActivity scanProductActivity){

        this.scanProductActivity = scanProductActivity;
    }

    @Override
    protected void onPreExecute() {
        if (!internet.isNetworkAvailable(scanProductActivity)){
            cancel(true);
            onPostExecute("0");
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String getUrl = LoginActivity.SERVERURL + "/getshoppingrequest?code=" + URLEncoder.encode(params[0], "UTF-8")
                    + "&accountnumber=" + params[1]
                    + "&companynumber=" + params[2]
                    + "&shoppinglist=" + URLEncoder.encode(params[3], "UTF-8");
            return internet.doGetString(getUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "-4";
        }
    }

    @Override
    protected void onPostExecute(String resp) {
        scanProductActivity.buyItemResult(Integer.parseInt(resp));
    }
}
