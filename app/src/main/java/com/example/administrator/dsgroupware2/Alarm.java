package com.example.administrator.dsgroupware2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class Alarm extends BroadcastReceiver implements LocationListener {

    private LocationManager locationManager;
    private MainActivity mainActivity;
    private double lng;
    private double lat;

    private List<String> listProviders;

    public Alarm(){
        mainActivity = (MainActivity) (MainActivity.mContext);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationCoordinates locationInfo = getLocation();

        lat = locationInfo.latitude;
        lng = locationInfo.longitude;

        if(lat > 0.0 || lng > 0.0){
            mainActivity.SaveLocationInfo(lat, lng);
        }
    }

    public LocationCoordinates getLocation(){
        LocationCoordinates ret = new LocationCoordinates();

        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ret.latitude = 0.0;
            ret.longitude = 0.0;
            return ret;
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation != null){
            ret.latitude = lastKnownLocation.getLatitude();
            ret.longitude = lastKnownLocation.getLongitude();
        }

        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation != null) {
            ret.latitude = lastKnownLocation.getLatitude();
            ret.longitude = lastKnownLocation.getLongitude();
        }

        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnownLocation != null) {
            ret.latitude = lastKnownLocation.getLatitude();
            ret.longitude = lastKnownLocation.getLongitude();
        }

        listProviders = locationManager.getAllProviders();
        boolean [] isEnable = new boolean[3];
        for(int i=0; i<listProviders.size();i++) {
            if(listProviders.get(i).equals(LocationManager.GPS_PROVIDER)) {
                isEnable[0] = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            } else if(listProviders.get(i).equals(LocationManager.NETWORK_PROVIDER)) {
                isEnable[1] = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) this);
            } else if(listProviders.get(i).equals(LocationManager.PASSIVE_PROVIDER)) {
                isEnable[2] = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, (LocationListener) this);
            }
        }

        return ret;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = 0.0;
        double longitude = 0.0;

        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        if(location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}
