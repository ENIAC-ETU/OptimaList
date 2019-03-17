package com.eniac.optimalist.activities;


import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;


public class RecommendationActivity extends AsyncTask<HashMap<String,List<Integer>>, Void, HashMap<String,Integer>> {
    private final String serverAddress="https://optimalist-server.herokuapp.com/get-prediction/";
    HashMap<String,List<Integer>> obj;
    public HashMap<String,Integer> d;

    @Override
    public HashMap<String, Integer> doInBackground(HashMap<String,List<Integer>>... strings) {
        Gson gson=new Gson();
        String json=gson.toJson(strings[0]);
        InputStream inputStream;
        HttpURLConnection urlConnection;
        byte[] outputBytes;
        String responseData;
        HashMap<String, Integer> myMap=new HashMap<>();
        try{
            URL url = new URL(serverAddress);
            urlConnection = (HttpURLConnection) url.openConnection();
            outputBytes = json.getBytes("UTF-8");
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();

            OutputStream os = urlConnection.getOutputStream();
            os.write(outputBytes);
            os.flush();
            os.close();
            BufferedReader in = new BufferedReader( new InputStreamReader(urlConnection.getInputStream()) );
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null)
            { response.append(inputLine); }
            in.close();
            Log.d("MyLocation::",""+response);
            Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
            myMap = gson.fromJson(response.toString(), type);
            Log.d("MyLocation::",""+myMap.toString());
            d=myMap;
            urlConnection.disconnect();
        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return myMap;
    }
}