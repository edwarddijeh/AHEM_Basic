package com.example.ahem_basic;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MyAsyncTask extends AsyncTask<Void, Void, String> {

    private String url;
    private boolean routing;
    private boolean polygon;
    private boolean GMapRoute;
    private boolean user_polygon;
    private String data_route;
    private String data_polygon;
    private String data_user_polygon;
    private String data_GMap_route;
    private List<LatLng> latLngList_GMAP;

    public void setMode(String str){
        GMapRoute = false;
        polygon = false;
        user_polygon = false;
        routing = false;
        if (str.equalsIgnoreCase("routing")){
            routing  = true;
        } else if (str.equalsIgnoreCase("gMaproute")){
            GMapRoute = true;
        } else if (str.equalsIgnoreCase("polygon")){
            polygon  = true;
        } else if (str.equalsIgnoreCase("user_polygon")){
            user_polygon = true;
        }
    }


    public MyAsyncTask( String url) {
        this.url = url;
        data_route = "";
        data_polygon = "";
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (!anotherMethod(url)) {
            try {
                URL url = new URL(this.url);
//                URL url = new URL("https://example.com/api/data");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("******************************Test1******************************");
                con.setRequestMethod("GET");
                con.connect();
                System.out.println("******************************Test2******************************");
                int responseCode = con.getResponseCode();
                System.out.println("******************************Test3******************************");
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                        stringBuilder.append(inputLine);
                    }
                    in.close();
                    String responseBody = response.toString();
                    if (GMapRoute) {
                        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                        List<LatLng> latLngs = new ArrayList<>();

                        JSONArray routes = jsonObject.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                            if (legs.length() > 0) {
                                JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");
                                for (int i = 0; i < steps.length(); i++) {
                                    String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                                    latLngs.addAll(PolyUtil.decode(polyline));
                                    System.out.println(latLngs);
                                }
                            }
                        }
                        latLngList_GMAP = latLngs;

                    } else {

                        if (routing) {
                            data_route = responseBody;
                            System.out.println("responseBody = "+responseBody);
                        } else if (polygon) {
                            data_polygon = responseBody;
                            System.out.println(responseBody);
                        } else if (user_polygon){
                            data_user_polygon = responseBody;
                        }
                    }
                    return responseBody;
                } else {
                    // Handle HTTP error response
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // Handle the response body here
        } else {
            // Handle the error case
        }
    }
    public String giveRoute(){
        return data_route;
    }
    public String givePolygon(){
        return data_polygon;
    }

    public List<LatLng> giveGMapRoute(){return latLngList_GMAP;}

    public String giveUserPolygon() {
        return data_user_polygon;
    }

    private boolean anotherMethod(String url){
        return false;
    }
}
