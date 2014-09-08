package ru.uslugivkz.test_login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import  android.telephony.TelephonyManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Test_login extends ActionBarActivity {

    //WebView mWebView;
    TextView dispID;
    TextView saveID;
    TextView t_info;
    TextView t_phone;
    String sId;
    String sPhone;
    String sPass;

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_ID = "sim_id";
    public static final String APP_PREFERENCES_PHONE = "phone";
    public static final String APP_PREFERENCES_PASS = "pass";
    SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_login);
        //mWebView=(WebView)findViewById(R.id.webView);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        dispID=(TextView)findViewById(R.id.textView3);
        saveID=(TextView)findViewById(R.id.textView4);
        t_info=(TextView)findViewById(R.id.textView5);
        t_phone=(TextView)findViewById(R.id.editText);
        /*mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(mWebView, url);
             //   mWebView.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");  //              t_info.setText("Загружено "+url);
                // прячем диалог please wait здесь
            }
        });
        */
        final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
        IntentFilter filter = new IntentFilter(SMS_RECEIVED);
        BroadcastReceiver receiver = new IncomingSMSReceiver();
        registerReceiver(receiver, filter);
    }
    public class IncomingSMSReceiver extends BroadcastReceiver {
        private static final String queryString = "uslugiVKZ.ru";
        private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

        public void onReceive(Context _context, Intent _intent) {
            if (_intent.getAction().equals(SMS_RECEIVED)) {
                //SmsManager sms = SmsManager.getDefault();
                Bundle bundle = _intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++)
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    for (SmsMessage message : messages) {
                        String msg = message.getMessageBody();
                        String to = message.getOriginatingAddress();
                        int pos=msg.indexOf(queryString);
                        if (pos>0) {
                            String out = msg.substring(pos+14);
                            t_info.setText(out);
                            StorePass(out);
                            //sms.sendTextMessage(to, null, out, null, null);
                        }
                    }
                }
            }
        }
    }
    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            t_info.setText("Загружаем...");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadOneUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String res=result.replaceAll("<br>", "\n");
            res=res.replaceAll("<[a-zA-Z\\\\s/]+>","");
            t_info.setText(res);
            super.onPostExecute(result);
        }
    }

    private String downloadOneUrl(String myurl) throws IOException {
        InputStream inputstream = null;
        String data = "";
        try {
            URL url = new URL(myurl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                inputstream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int read = 0;
                while ((read = inputstream.read()) != -1) {
                    bos.write(read);
                }
                byte[] result = bos.toByteArray();
                bos.close();

                data = new String(result);

            } else {
                data = connection.getResponseMessage() + " . Error Code : " + responseCode;
            }
            connection.disconnect();
            //return data;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (inputstream != null) {
                inputstream.close();
            }
        }
        return data;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // если ли нужный нам ключ
        //if (mSettings.contains(APP_PREFERENCES_ID)) {
            // Получаем число из настроек
            sId=mSettings.getString(APP_PREFERENCES_ID, "NONE");
            String srvcName = Context.TELEPHONY_SERVICE;
            TelephonyManager telephonyManager = (TelephonyManager)  getSystemService(srvcName);
            dispID.setText(telephonyManager.getSimSerialNumber());
        if (dispID.getText().toString().equals(sId)) {
            saveID.setText(sId);
            sPhone = mSettings.getString(APP_PREFERENCES_PHONE, "");
            t_phone.setText(sPhone);
            sPass = mSettings.getString(APP_PREFERENCES_PASS, "@string/not_registered");
            t_info.setText(sPass);
        }
        else
            t_info.setText("@string/not_registered");
        // Выводим на экран

        //}
    }
    public void button_click_sms(View view) {
        String srvcName = Context.TELEPHONY_SERVICE;
        TelephonyManager telephonyManager = (TelephonyManager)  getSystemService(srvcName);
        dispID.setText(telephonyManager.getSimSerialNumber());
        if (dispID.getText().toString().equals(saveID.getText().toString()))
            saveID.setText("SMS id совпадают");
        else
            saveID.setText("SMS id НЕ совпадают");
       // mWebView.loadUrl("http://uslugivkz.ru");

        ConnectivityManager myConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = myConnMgr.getActiveNetworkInfo();

        if (networkinfo != null && networkinfo.isConnected()) {
            new DownloadPageTask().execute("http://uslugivkz.ru/daimon/add_user.php?phone="+t_phone.getText().toString());
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show();
        }



    }

    public void StorePass(String s_pass){
        if (s_pass.matches("[0-9]+")) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_ID, dispID.getText().toString());
            editor.putString(APP_PREFERENCES_PHONE, t_phone.getText().toString());
            editor.putString(APP_PREFERENCES_PASS, s_pass);
            editor.apply();
        }
    }

}
