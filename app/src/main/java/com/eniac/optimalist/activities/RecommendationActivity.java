package com.eniac.optimalist.activities;


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


public class RecommendationActivity  {
    private final String serverAddress="https://www.android.com";
        public HashMap<String,Integer> sendPost(HashMap<String,List<Integer>> obj) throws IOException {
            Gson gson=new Gson();
            String json=gson.toJson(obj);
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

                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                responseData = convertStreamToString(inputStream);
                Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
                myMap = gson.fromJson(responseData, type);
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
            return myMap;
            }

    public String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}