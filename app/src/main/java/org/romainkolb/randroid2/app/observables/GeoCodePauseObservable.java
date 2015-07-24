package org.romainkolb.randroid2.app.observables;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.romainkolb.randroid2.app.model.Rando;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.List;

public class GeoCodePauseObservable implements Observable.OnSubscribe<Rando>{
        private final Context ctx;
        private final Rando rando;

        public static Observable<Rando> createObservable(Context ctx, Rando rando) {
            return Observable.create(new GeoCodePauseObservable(ctx, rando))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        private GeoCodePauseObservable(Context ctx, Rando rando) {
            this.ctx = ctx;
            this.rando = rando;
        }

        @Override
        public void call(Subscriber<? super Rando> subscriber) {
            Geocoder geocoder = new Geocoder(ctx);

            if (rando != null && rando.getRetour() != null && rando.getRetour().size() > 0) {
                LatLng pause = rando.getRetour().get(0);
                try {
                    List<Address> pauseAddress = geocoder.getFromLocation(pause.latitude, pause.longitude, 1);
                    if (pauseAddress != null && pauseAddress.size() > 0) {
                        String thoroughfare = pauseAddress.get(0).getThoroughfare();
                        rando.setPauseThoroughfare(thoroughfare);
                    }

                    subscriber.onNext(rando);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    Log.e(this.getClass().getSimpleName(), "error while geocoding pause address", e);
                    subscriber.onError(e);
                }
            }else {
                Log.e(this.getClass().getSimpleName(),"invalid Rando : "+rando);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }


    }