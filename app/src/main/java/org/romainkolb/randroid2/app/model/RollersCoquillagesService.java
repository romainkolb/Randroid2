package org.romainkolb.randroid2.app.model;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;


/**
 * Created by romain on 12/8/13.
 */
public interface RollersCoquillagesService {
    //ex : http://www.rollers-coquillages.org/parcours/kml/20101003/10
    @GET("/parcours/kml/{date}/{nbpos}")
    void getRando(@Path("date") String date, @Path("nbpos") Integer nbPos, Callback<Rando> cb);

    @GET("/parcours/kml/{date}/{nbpos}")
    Observable<Rando> getRandoRx(@Path("date") String date, @Path("nbpos") Integer nbPos);
}
