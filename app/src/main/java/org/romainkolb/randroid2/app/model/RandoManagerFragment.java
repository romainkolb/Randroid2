package org.romainkolb.randroid2.app.model;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.commonsware.android.retrofit.ContractFragment;
import com.google.android.gms.maps.model.LatLng;
import org.romainkolb.randroid2.app.R;
import org.romainkolb.randroid2.app.data.RandoDbHelper;
import org.romainkolb.randroid2.app.data.Utils;
import org.romainkolb.randroid2.app.observables.GeoCodePauseObservable;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by romain on 12/8/13.
 */
public class RandoManagerFragment extends ContractFragment<RandoManagerFragment.Contract> implements RandoDbHelper.RandoListener {
    private RollersCoquillagesService rc;
    private RandoDbHelper db;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        RestAdapter restAdapter =
                new RestAdapter.Builder().setEndpoint(getString(R.string.rollers_coquillages_server)).setConverter(new RandoConverter())
                        .build();
        rc = restAdapter.create(RollersCoquillagesService.class);

        db = new RandoDbHelper(getActivity().getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefs.getAll();

        return null;
    }

    public void getRandoFromWsRx(Calendar date) {
        Integer nbPos = getNbPositions();
        getRandoFromWsRx(date, nbPos);
    }

    public void getRandoFromWsRx(Calendar date, Integer nbPos) {
        String textDate = date == null ? "" : DateFormat.format("yyyyMMdd", date).toString();

        rc.getRandoRx(textDate, nbPos)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Func1<Throwable, Rando>() {
                    @Override
                    public Rando call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            processRetrofitError((RetrofitError) throwable);
                        }else{
                            Log.e(rc.getClass().getSimpleName(),"Remote error",throwable);
                        }
                        return null;
                    }
                })
                .flatMap(new Func1<Rando, Observable<Rando>>() {
                    @Override
                    public Observable<Rando> call(Rando rando) {
                        return GeoCodePauseObservable.createObservable(getActivity(), rando);
                    }
                })
                .subscribe(new Action1<Rando>() {
                    @Override
                    public void call(Rando rando) {
                        if (rando != null) {
                            getContract().drawRando(rando);
                        }
                    }
                });
    }


    public void getRandoFromDb(Calendar date) {
        db.getRandoAsync(date, this);
    }

    public void initRandoList() {
        db.getAllRandosAsync(this);
    }

    public void resetRandos() {
        //delete all Randos. resetComplete() will then be called to init the Randos
        db.deleteAllRandosAsync(this);
    }

    private void saveRando(Rando rando) {
        List<Rando> randos = new ArrayList<>(1);
        randos.add(rando);
        saveRandos(randos);
    }

    private void saveRandos(List<Rando> randos) {
        db.saveRandosAsync(randos, this);
    }

    @Override
    public void setRando(Rando rando) {
        if (rando != null) {
            if (rando.getAller() != null && rando.getAller().size() > 0) {
                getContract().drawRando(rando);
            } else {
                //We haven't downloaded this rando yet, let's do it
                getRandoFromWsRx(rando.getDate());
            }
        }
    }

    @Override
    public void resetComplete() {
        //Initialize DB with most recent Rando
        getRandoFromWsRx(null);
    }

    @Override
    public void randosSaved() {
        db.getAllRandosAsync(this);
    }

    @Override
    public void updateCursor(Cursor cursor) {
        //getContract().updateRandoCursor(cursor);
    }

    /**
     * Must be implemented by the Activity
     */
    public interface Contract {
        void drawRando(Rando rando);

        //void updateRandoCursor(Cursor cursor);

        void cancelProgress();
    }


    public int getNbRandos() {
        int def = getResources().getInteger(R.integer.defaultNbRandos);
        return Integer.parseInt(prefs.getString(getString(R.string.prefKeyNbRandos), String.valueOf(def)));
    }

    public int getNbPositions() {
        int def = getResources().getInteger(R.integer.defaultNbPositions);
        //Not user configurable for now
        return def;
    }

    public boolean isRefreshOnStartup() {
        boolean def = getResources().getBoolean(R.bool.defaultRefreshOnStartup);
        return prefs.getBoolean(getString(R.string.prefKeyRefreshOnStartup), def);
    }

    public boolean isDisplayGPSOverlay() {
        boolean def = getResources().getBoolean(R.bool.defaultGPSOverlay);
        return prefs.getBoolean(getString(R.string.prefKeyGPSOverlay), def);
    }


    private void processRetrofitError(RetrofitError retrofitError) {
        if (retrofitError != null) {
            String errorMessage;
            if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
                errorMessage = getResources().getString(R.string.network_error);
            } else {
                errorMessage = String.format(getResources().getString(R.string.read_error), retrofitError.getLocalizedMessage());
            }
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        getContract().cancelProgress();
    }
}
