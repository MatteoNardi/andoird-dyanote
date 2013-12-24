package com.dyanote.android.utils;

import android.util.Log;

import com.dyanote.android.User;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class NetworkUtils {

    public static void dumpStream(InputStream s) {
        BufferedReader in = new BufferedReader(new InputStreamReader(s));
        String line;
        try {
            while((line = in.readLine()) != null) {
                Log.i("Network", line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String post(String address, String data) {
        return request(address, "POST", null, data);
    }

    public static String get(String address, User user) {
        return request(address, "GET", user, null);
    }

    private static String request(String address, String method, User user, String data) {
        Log.i("NetworkUtils", "Request to " + address);
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            Log.e("Network", "Malformed URL", e);
            return "";
        }
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e("Network", "Error opening connection", e);
            return "";
        }

        // Set connection timeouts
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(20000);

        // Set request method
        try {
            conn.setRequestMethod(method);
        } catch (ProtocolException e) {
            Log.e("Network", "Error setting request type", e);
            return "";
        }


        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        // Add data
        if(data != null) {
            OutputStream os;
            try {
                os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();
            } catch (IOException e) {
                Log.e("Network", "Error writing data to HttpURLConnection", e);
                return "";
            }
        }

        // Add authentication
        if (user != null)
            conn.setRequestProperty("Authorization", "Bearer " + user.getToken());

        // Connect and get response
        try {
            conn.connect();
            InputStream stream = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();
            BufferedInputStream in = new BufferedInputStream(stream);
            java.util.Scanner s = new java.util.Scanner(in, "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            Log.e("Network", "Error reading response", e);
            return "";
        } finally {
            conn.disconnect();
        }
    }
}