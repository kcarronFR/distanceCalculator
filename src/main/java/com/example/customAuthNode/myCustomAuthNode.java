/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2022 ForgeRock AS.
 */
/*
 * This code is to be used exclusively in connection with ForgeRock’s software or services.
 * ForgeRock only offers ForgeRock software or services to legal entities who have entered
 * into a binding license agreement with ForgeRock.
 */


package com.example.customAuthNode;

import java.util.*;
import java.util.jar.Attributes;
import java.util.regex.Pattern;


import javax.inject.Inject;

import okhttp3.*;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.util.i18n.PreferredLocales;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.Callback;

import com.google.inject.assistedinject.Assisted;
import org.w3c.dom.Attr;

import static org.forgerock.openam.auth.node.api.Action.send;


/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = AbstractDecisionNode.OutcomeProvider.class,
               configClass      = myCustomAuthNode.Config.class)
public class myCustomAuthNode extends AbstractDecisionNode {

    private final Pattern DN_PATTERN = Pattern.compile("^[a-zA-Z0-9]=([^,]+),");
    private final Logger logger = LoggerFactory.getLogger(myCustomAuthNode.class);

    private final String loggerPrefix = "myCustomAuthNode" + myCustomAuthNodePlugin.logAppender;

    private final Config config;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The header name for Miles or Kilometers that will contain the identity's username.
         */
        @Attribute(order = 100)
        default String milesorkilometers() {
            return "Miles or Kilometers";
        }

        @Attribute(order = 200)
        default String api_key(){
            return "Enter Api Key";
        }

    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm The realm the node is in.
     */
    @Inject
    public myCustomAuthNode(@Assisted Config config, @Assisted Realm realm) {
        this.config = config;
        this.realm = realm;
        
    }
    public static double deg2rad(double deg){
        return deg * (Math.PI /180);
    }

    public static double getDistanceFromNewYork(double city_lat, double city_lon, double user_lat, double user_lon){

        //This is the math to get the kilometers from the city
        int radius = 6371;
        double dlat = deg2rad(city_lat - user_lat);
        double dlon = deg2rad(city_lon - user_lon);

        var a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                Math.cos(deg2rad(city_lat)) * Math.cos(deg2rad(user_lat)) *
                Math.sin(dlon/2) * Math.sin(dlon/2) ;
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = radius * c;
        return d;
}

    @Override
    public Action process(TreeContext context) {


        try {
            /*
            Ask for address via input
            Ask for city name via input
            Get lat and lon for address
            Get lat and lon for city name
            Admin - Miles or Kilometers
             */

            //Initializes the NodeState
            NodeState ns = context.getStateFor(this);
            ArrayList<Callback> callbacks = new ArrayList<Callback>();
            Callback TextInputCallback;
            String address;
            String city;
            String api_key;

            if(context.hasCallbacks()){
                address = context.getCallbacks(NameCallback.class).get(0).getName();
                System.out.println("Address: " + address);

                city = context.getCallbacks(NameCallback.class).get(1).getName();
                System.out.println("City: " + city);

                ns.putShared("address", address);
                ns.putShared("city", city);

                //input parameter for admin

                String type = config.milesorkilometers();
                api_key = config.api_key();

                String url_city = "https://api.geoapify.com/v1/geocode/search?text=" + city +"&format=json&apiKey=" + api_key;
                String url = "https://api.geoapify.com/v1/geocode/search?text=" + address + "&format=json&apiKey=" + api_key;



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
                String city_city = index_city.getString("city");
                String state = index_city.getString("state");

                System.out.println("\n\n\n");
                System.out.println("City Information");
                System.out.println("================================");
                System.out.println("City jArray: "  + jArray_city);
                System.out.println("City: " + city_city);
                System.out.println("State: " + state);
                System.out.println("City lon: " + city_lon);
                System.out.println("City lat: " + city_lat);
                System.out.println("================================");
                System.out.println("\n\n\n");


                if(Objects.equals(config.milesorkilometers(), "miles")){
                    double val = getDistanceFromNewYork(city_lat, city_lon, user_lat, user_lon);
                    double convert = val * .62137119223714;
                    System.out.println("Distance from "+ city_city + ", " +state+ ": "+ convert + " miles");
                    System.out.println("\n\n\n");
                    ns.putShared("Distance from "+ city, convert +" miles" );
                } else{
                    System.out.println("Distance from " + city_city + ", " +state+ ": "+ getDistanceFromNewYork(city_lat, city_lon, user_lat, user_lon) + " kilometers");
                    ns.putShared("Distance from " + city_city +", " +state+ ": ", getDistanceFromNewYork(city_lat, city_lon, user_lat, user_lon) +" kilometers");
                }

                return goTo(true).build();
            }
            else{
                System.out.println("Initializing callbacks...");

                callbacks.add((Callback) new NameCallback("Enter Address: ", "address"));
                callbacks.add((Callback) new NameCallback("Enter City: ", "city"));
                return send(callbacks).build();
            }


        } catch (Exception ex) {
            String stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
            logger.error(loggerPrefix + "Exception occurred: " + stackTrace);
            context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + ex.getMessage());
            context.getStateFor(this).putShared(loggerPrefix + "StackTrace", new Date() + ": " + stackTrace);

            return goTo(false).build();
        }
        //return goTo(false).build();
    }
    public static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider{

        static final String SUCCESS_OUTCOME = "True";
        static final String FAILURE_OUTCOME = "False";

        private static final String BUNDLE = myCustomAuthNode.class.getName();
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) throws NodeProcessException {

            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());

            List<Outcome> results = new ArrayList<>(
                    Arrays.asList(
                            new Outcome(SUCCESS_OUTCOME, SUCCESS_OUTCOME)
                    )
            );
            results.add(new Outcome(FAILURE_OUTCOME, FAILURE_OUTCOME));

            return Collections.unmodifiableList(results);
        }
    }

}
