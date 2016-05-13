package com.example.lanco.hw3;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    WebView message;
    JSinterface js = new JSinterface();
    String txtNum, txtCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        message = (WebView) findViewById(R.id.messageInput);
        // admit & adapt javascript. and use HTML page & Javascript
        message.getSettings().setJavaScriptEnabled(true);
        message.addJavascriptInterface(js, "control");
        message.loadUrl("file:///android_asset/message.html");

        //Processing permission for SDK 23 Version(Android 6.0)
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS 발신 권한 있음.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "SMS 발신 권한 없음.", Toast.LENGTH_SHORT).show();
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "SMS 발신 권한 설명필요함.", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }
    }

    //Processing permission for SDK 23 Version(Android 6.0)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS 권한을 사용자가 승인함.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SMS 권한 거부됨.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    // sendSMS function : using broadcast to check sended message. and it send SMS to use SMSmanager
    public void sendSMS(String smsNumber, String smsText) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        registerReceiver(new BroadcastReceiver() {

            // onReceive function : check message to send, and announce contents using toast
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(mContext, "Send Complete!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(mContext, "Send failed", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(mContext, "No Service area", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(mContext, "Wireless(Radio) is off", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));
        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, null);
    }


    // JavaScript interface function
    public class JSinterface {

        // showText function : get the button value from HTML&JavaScript, and return number to set phone text filed in HTML
        @JavascriptInterface
        public String showText(String ini, String num) {
            // if "c" button
            if (num.equals("c")) {
                ini = ini.substring(0, ini.length() - 1);
                return ini;
            } else {
                ini = ini + num;
                return ini;
            }
        }

        // showText function : get number and contents from HTML&JavaScript, and send SMS
        @JavascriptInterface
        public void sendData(String num, String con) {
            // if text field in HTML is not empty, send SMS using sendSMS() function
            if (num.length() > 0 && con.length() > 0) {
                txtNum = num;
                txtCon = con;
                sendSMS(txtNum, txtCon);

            }
            // if text field in HTML is empty, send toast to announce alert
            else {
                Toast.makeText(getApplicationContext(), "Fill in the Phone number or contents", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
