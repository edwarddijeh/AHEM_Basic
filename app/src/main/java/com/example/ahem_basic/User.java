package com.example.ahem_basic;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class User implements Serializable {
    private boolean[] sensitivities = new boolean[6];//pm2.5,pm10,O3,SO2,NO2,CO
    private void getSensitivity(){}
    private String searchStringLL;
    private String searchStringAdd;
    private String longitude;
    private String latitude;
    private double latitude_double;
    private double longitude_double;
    private String address;
    private boolean LL;
    private boolean Add;
    private LatLng locationLatLng;// = new LatLng(Statics.centerLat, Statics.centerLon);
    private String test;
    public void print(){
        System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        System.out.println(test);
    }
    public void setTest(){
        test = "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+
                "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU";
    }

    public void setSearchStringLL(String searchStringLL) {
        this.searchStringLL = searchStringLL;
        String a[] = this.searchStringLL.split(",");
        latitude = a[0];
        longitude = a[1];
    }

    public void setSearchStringAdd(String searchStringAdd) {
        this.searchStringAdd = searchStringAdd;
        address = searchStringAdd;
    }

    public void setSensitivities(String sense, boolean checked){
        if (checked) {
            if (sense.equalsIgnoreCase("pm25")) {
                sensitivities[0] = true;
                System.out.println(sensitivities.length);
            } else if (sense.equalsIgnoreCase("pm10")) {
                sensitivities[1] = true;
            } else if (sense.equalsIgnoreCase("O3")) {
                sensitivities[2] = true;
            } else if (sense.equalsIgnoreCase("SO2")) {
                sensitivities[3] = true;
            } else if (sense.equalsIgnoreCase("CO")) {
                sensitivities[4] = true;
            } else if (sense.equalsIgnoreCase("NO2")) {
                sensitivities[5] = true;
            }
        } else {
            if (sense.equalsIgnoreCase("pm25")) {
                sensitivities[0] = false;
            } else if (sense.equalsIgnoreCase("pm10")) {
                sensitivities[1] = false;
            } else if (sense.equalsIgnoreCase("O3")) {
                sensitivities[2] = false;
            } else if (sense.equalsIgnoreCase("SO2")) {
                sensitivities[3] = false;
            } else if (sense.equalsIgnoreCase("CO")) {
                sensitivities[4] = false;
            } else if (sense.equalsIgnoreCase("NO2")) {
                sensitivities[5] = false;
            }
        }
    }

    public String getSensitivities() {
        String sense = "";
        if (noSensitivities()){
            // give default sensitivities
            return "";
        } else {
            if (sensitivities[0] = true){
                sense += "1,";
            }
            if (sensitivities[1] = true){
                sense += "2,";
            }
            if (sensitivities[2] = true){
                sense += "3,";
            }
            if (sensitivities[3] = true){
                sense += "4,";
            }
            if (sensitivities[4] = true){
                sense += "5,";
            }
            if (sensitivities[5] = true){
                sense += "6,";
            }
            sense = sense.substring(0, sense.length() - 1);
        }
        return sense;
    }
    private boolean noSensitivities(){
        for(int i = 0; i<sensitivities.length; i++){
            if (sensitivities[i] = true){return false;}
        }
        return true;
    }

    public void setLL(boolean LL) {
        this.LL = LL;
    }

    public void setAdd(boolean add) {
        Add = add;
    }
    public boolean isLLString(){
        if (Add == true && LL == false){
            return false;
        }
        return true;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
        longitude_double = Double.parseDouble(longitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
        latitude_double = Double.parseDouble(latitude);
    }

    public double getLatitude_double() {
        return latitude_double;
    }

    public double getLongitude_double() {
        return longitude_double;
    }
    public boolean isSensitiveTo(String str){
        String[] pollutant= {"PM25", "PM10", "O3", "SO2", "CO", "NO2"};
        for (int i = 0; i<6; i++){
//            if (i==0){
//                pollutant = "PM25";
//            } else if (i==1){
//                pollutant = "PM10";
//            } else if (i==2){
//                pollutant = "O3";
//            } else if (i==3){
//                pollutant = "SO2";
//            } else if (i==4){
//                pollutant = "CO";
//            } else if (i==5){
//                pollutant = "NO2";
//            }
            if (sensitivities[i] == true) {
                if (pollutant[i].equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setLocationLatLng(LatLng locationLatLng) {
        System.out.println("Setting Location LL");
        this.locationLatLng = locationLatLng;
    }

    public LatLng getLocationLatLng() {
        return locationLatLng;
    }
}
