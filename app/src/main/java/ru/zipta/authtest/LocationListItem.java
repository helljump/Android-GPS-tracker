package ru.zipta.authtest;

import android.database.Cursor;

import java.util.Date;

/**
 * Created by snoa on 16.08.2015.
 */
public class LocationListItem {

    private double lat;
    private double lng;
    private double alt;
    private Date time;

    public LocationListItem(double lat, double lng, double alt, Date time) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public static LocationListItem fromCursor(Cursor c) {
        LocationListItem l = new LocationListItem(
                c.getDouble(c.getColumnIndex("lat")),
                c.getDouble(c.getColumnIndex("lng")),
                c.getDouble(c.getColumnIndex("alt")),
                new Date(c.getLong(c.getColumnIndex("time")))
        );
        return l;

    }
}
