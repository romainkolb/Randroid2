package org.romainkolb.randroid2.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import org.romainkolb.randroid2.app.model.Rando;
import org.romainkolb.randroid2.app.model.RandoManagerFragment;
import org.romainkolb.randroid2.app.navigationdrawer.NavigationDrawerFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, RandoMapFragment.OnFragmentInteractionListener, RandoManagerFragment.Contract, OnMapReadyCallback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private RandoMapFragment mRandoMapFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mRandoMapFragment = (RandoMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        mRandoMapFragment.getMapAsync(this);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        if (getRandoManagerFragment() == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new RandoManagerFragment()).commit();
        }
    }



    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();*/
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void drawRando(Rando rando) {
        if (rando != null) {
            mRandoMapFragment.drawRando(rando);
        }
        //toggleRefresh(false);
    }


    @Override
    public void cancelProgress() {
        //toggleRefresh(false);
    }

    private void resetRandos() {
        //toggleRefresh(true);
        getSupportActionBar().setSelectedNavigationItem(0);
        getRandoManagerFragment().resetRandos();
    }

    private void refreshCurrentRando() {
        Rando rando = mRandoMapFragment.getCurrentRando();
        if (rando != null) {
            //toggleRefresh(true);
            getRandoManagerFragment().getRandoFromWsRx(rando.getDate());
        }
    }


    private RandoManagerFragment getRandoManagerFragment() {
        return (RandoManagerFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        RandoManagerFragment randoManagerFragment = getRandoManagerFragment();

        if (randoManagerFragment != null) {
            if (mRandoMapFragment != null) {
                mRandoMapFragment.setMyLocationEnabled(randoManagerFragment.isDisplayGPSOverlay());
            }

            if(mRandoMapFragment.getCurrentRando()==null) {
                randoManagerFragment.resetRandos();
            }
        }
    }
}
