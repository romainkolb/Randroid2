package org.romainkolb.randroid2.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.romainkolb.randroid2.app.model.Rando;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RandoMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RandoMapFragment extends SupportMapFragment {

    private OnFragmentInteractionListener mListener;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private LatLng paris;

    private Polyline currentAller;
    private Polyline currentRetour;
    private Rando currentRando;

    private Marker startingLocation;
    private Marker pauseLocation;
    private Marker randoLocation;

    private double maxLat;
    private double minLat;
    private double maxLon;
    private double minLon;

    public RandoMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        mMap.setMyLocationEnabled(false);
        super.onPause();
    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #initMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                initMap();
            }
        }
    }


    public void setMyLocationEnabled(boolean enabled){
        mMap.setMyLocationEnabled(enabled);
    }

    private void initCoords() {
        float lat, lng;
        TypedValue tv = new TypedValue();

        getResources().getValue(R.dimen.paris_lat, tv, true);
        lat = tv.getFloat();
        getResources().getValue(R.dimen.paris_lng, tv, true);
        lng = tv.getFloat();
        paris = new LatLng(lat, lng);
    }

    private void initMap() {
        if (paris == null) {
            initCoords();
        }

        // Move the camera instantly to Paris with a zoom of 5.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 5));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11), 2000, null);

    }

    public void drawRando(Rando rando) {
        if (currentAller != null) currentAller.remove();
        if (currentRetour != null) currentRetour.remove();
        if (startingLocation != null) startingLocation.remove();
        if (pauseLocation != null) pauseLocation.remove();
        if (randoLocation != null) randoLocation.remove();

        currentRando = rando;

        maxLat = Double.MIN_VALUE;
        minLat = Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = Double.MIN_VALUE;

        currentAller = drawSegment(rando.getAller(), 0xff67c547);
        currentRetour = drawSegment(rando.getRetour(), 0xffc03639);

        //Draw POIs
        if (currentAller != null && currentAller.getPoints().size() > 0) {
            startingLocation = mMap.addMarker(new MarkerOptions().position(currentAller.getPoints().get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_start)).title(getString(R.string.startingLocation)));
        }
        if (currentRetour != null && currentRetour.getPoints().size() > 0) {
            StringBuilder title = new StringBuilder(getString(R.string.pauseLocation));
            if (rando.getPauseThoroughfare() != null && rando.getPauseThoroughfare().length() > 0) {
                title.append(" : ");
                title.append(rando.getPauseThoroughfare());
            }
            pauseLocation = mMap.addMarker(new MarkerOptions().position(currentRetour.getPoints().get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_pause)).title(title.toString()));
        }

        if (rando.getLastRandoPosition() != null) {
            randoLocation = mMap.addMarker(new MarkerOptions().position(rando.getLastRandoPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_rando_position)).title(getString(R.string.randoLocation)));
        }

        //Zoom to rando
        LatLng maxBound = new LatLng(maxLat + 0.005, maxLon + 0.005);
        LatLng minBound = new LatLng(minLat - 0.005, minLon - 0.005);
        LatLngBounds randoBounds = LatLngBounds.builder().include(maxBound).include(minBound).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(randoBounds, 0));
    }

    private Polyline drawSegment(List<LatLng> segment, int color) {
        if (segment != null && segment.size() > 0) {
            LatLng depart;
            LatLng arrivee;
            PolylineOptions po = new PolylineOptions().width(7).color(color);

            depart = segment.get(0);
            for (int i = 1; i < segment.size(); i++) {

                if (depart.latitude > maxLat) maxLat = depart.latitude;
                if (depart.latitude < minLat) minLat = depart.latitude;
                if (depart.longitude > maxLon) maxLon = depart.longitude;
                if (depart.longitude < minLon) minLon = depart.longitude;

                arrivee = segment.get(i);

                po.add(depart, arrivee);

                depart = arrivee;
            }
            return mMap.addPolyline(po);
        }
        return null;
    }

    public Rando getCurrentRando() {
        return currentRando;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
