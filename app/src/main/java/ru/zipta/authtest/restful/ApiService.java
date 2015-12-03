package ru.zipta.authtest.restful;

import java.util.Date;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by snoa on 04.08.2015.
 */


public interface ApiService {

    String BASE_URL = "https://auth-jump.rhcloud.com";

    @POST("/api/locations/")
    Response postLocation(@Body Location l);

    class Location {
        public double lat;
        public double lng;
        public double alt;
        public Date time;

        public Location(double lat, double lng, double alt, Date time ) {
            this.time = time;
            this.alt = alt;
            this.lng = lng;
            this.lat = lat;
        }
    }


    @GET("/test-view/")
    Status test();

    class Status {
        public String status;
    }

    //@POST("/api/location/")


}
