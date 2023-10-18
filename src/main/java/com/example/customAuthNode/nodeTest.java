package com.example.customAuthNode;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.PASSWORD;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.inject.Inject;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.realms.Realm;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.security.auth.callback.TextInputCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.google.inject.assistedinject.Assisted;


public class nodeTest {
    public static double getDistanceFromNewYork(double ny_lat, double ny_lon, double user_lat, double user_lon){
        int radius = 6371;
        double dlat = deg2rad(ny_lat - user_lat);
        double dlon = deg2rad(ny_lon - user_lon);

        var a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                Math.cos(deg2rad(ny_lat)) * Math.cos(deg2rad(user_lat)) *
                        Math.sin(dlon/2) * Math.sin(dlon/2) ;
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = radius * c;
        return d;
    }

    public static double deg2rad(double deg){
        return deg * (Math.PI /180);
    }

  public static void main(String [] args)  {
        try {



            nodeTest obj = new nodeTest();

            //Change when figure out how to get input
            String address = "14784 N 200 E, Covington, Indiana";


            String api_key = "21491b1a65764b0a92e59f30cd6f4303";
            String type = "miles";

            String url_city = "https://api.geoapify.com/v1/geocode/search?text=Indianapolis%2C%20Indiana&format=json&apiKey=" + api_key;
            //String url_before = "https://api.geoapify.com/v1/geocode/search?text="+ address +"format=json&apiKey=" + api_key;

            String url = "https://api.geoapify.com/v1/geocode/search?text=" + address + "&format=json&apiKey=" + api_key;
            //String encoded_url = URLEncoder.encode(url, StandardCharsets.UTF_8);



            //For User address


            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .build();
            Response response = client.newCall(request).execute();
            System.out.println("This is the response " + response);

            //For City Name
            OkHttpClient client_city = new OkHttpClient().newBuilder()
                    .build();
            Request request_city = new Request.Builder()
                    .url(url_city)
                    .method("GET", null)
                    .build();

            Response response_city = client_city.newCall(request_city).execute();

            //Get response from api call - User
            String jsonData = response.body().string();

            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jArray = jsonObject.getJSONArray("results");
            JSONObject index = jArray.getJSONObject(0);
            double user_lon = index.getDouble("lon");
            double user_lat = index.getDouble("lat");
            String user_city = index.getString("city");
            String user_state = index.getString("state");
            System.out.println("\n\n\n");
            System.out.println("User Address Information");
            System.out.println("================================");
            System.out.println("User jArray: "  + jArray);
            System.out.println("User lon: " + user_lon);
            System.out.println("User lat: " + user_lat);
            System.out.println("User city: " + user_city);
            System.out.println("User state: " + user_state);
            System.out.println("================================");
            System.out.println("\n\n\n");

            //Get response from api call - City

            String jsonData_city = response_city.body().string();
            System.out.println("This is the jsonData: " + jsonData_city);
            JSONObject jsonObject_city = new JSONObject(jsonData_city);
            JSONArray jArray_city = jsonObject_city.getJSONArray("results");
            JSONObject index_city = jArray_city.getJSONObject(0);
            double city_lon = index_city.getDouble("lon");
            double city_lat = index_city.getDouble("lat");
            String city = index_city.getString("city");
            String state = index_city.getString("state");

            System.out.println("\n\n\n");
            System.out.println("City Information");
            System.out.println("================================");
            System.out.println("City jsonArray: "  + jArray_city);
            System.out.println("City: " + city);
            System.out.println("State: " + state);
            System.out.println("City lon: " + city_lon);
            System.out.println("City lat: " + city_lat);
            System.out.println("================================");
            System.out.println("\n\n\n");







            if(type == "miles"){
                double val = obj.getDistanceFromNewYork(city_lat, city_lon, user_lat, user_lon);
                double convert = val * .62137119223714;
                System.out.println("Distance from "+ city + ", " +state+ ": "+ convert + " miles");
                System.out.println("\n\n\n");

            }else{
                System.out.println("Distance from " + city + ", " +state+ ": "+ obj.getDistanceFromNewYork(city_lat, city_lon, user_lat, user_lon) + " kilometers");
            }

            //Put the result into the sharedState
//      ns.putShared("Test", "This works");
//      ns.putShared("Distance from New York", getDistanceFromNewYork(ny_lat, ny_lon, user_lat, user_lon));
        }catch(Exception e){
            System.err.println(e.toString());
        }
  }

}
