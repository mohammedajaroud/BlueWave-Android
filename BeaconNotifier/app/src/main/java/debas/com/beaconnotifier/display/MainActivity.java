package debas.com.beaconnotifier.display;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import debas.com.beaconnotifier.BeaconNotifierApp;
import debas.com.beaconnotifier.R;

import com.astuetz.PagerSlidingTabStrip;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by debas on 18/10/14.
 */
public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener, BeaconConsumer {

    private List<Fragment> fragmentList = new ArrayList<Fragment>();
    private BroadcastReceiver mReceiver;
    private BeaconManager mBeaconManager = BeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentList.add(new BeaconViewer());
        fragmentList.add(new HistoryBeacon());
        fragmentList.add(new FavoritesBeacons());

        Log.d("onCreate", "MainActivity");
        setContentView(R.layout.activity_main);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList);
        pager.setAdapter(myPagerAdapter);
        pager.setOffscreenPageLimit(3);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);

        mBeaconManager.bind(this);

        BeaconManager.debug = true;
//        BeaconConsumer fragment = (BeaconConsumer) myPagerAdapter.getItem(0);
//        fragment.getApplicationContext();
//        mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                final String action = intent.getAction();
//
//                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
//                            BluetoothAdapter.ERROR);
//                    LinearLayout linearLayout;
//                    ListView listView;
//                    switch (state) {
//                        case BluetoothAdapter.STATE_OFF:
////                            imageView = (ImageView) findViewById(R.id.bluetooth_disabled);
//                            listView = (ListView) findViewById(R.id.listView);
//                            linearLayout = (LinearLayout) findViewById(R.id.bluetooth_disabled);
////                            imageView.setVisibility(ImageView.VISIBLE);
//                            listView.setVisibility(ListView.GONE);
//                            linearLayout.setVisibility(LinearLayout.VISIBLE);
//                            Toast.makeText(MainActivity.this, "bluetooth turning off ...", Toast.LENGTH_LONG).show();
//                            break;
//                        case BluetoothAdapter.STATE_ON:
//                            linearLayout = (LinearLayout) findViewById(R.id.bluetooth_disabled);
//                            listView = (ListView) findViewById(R.id.listView);
//                            linearLayout.setVisibility(LinearLayout.GONE);
//                            listView.setVisibility(ListView.VISIBLE);
//                            Toast.makeText(MainActivity.this, "bluetooth turning on ...", Toast.LENGTH_LONG).show();
//                            break;
//                    }
//                }
//            }
//        };
//        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onStart() {
        Log.d("Beacon", "started");
        super.onStart();
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(false);
        }
        ((BeaconNotifierApp) getApplication()).setCreateNotif(false);
    }

    @Override
    public void onResume() {
        Log.d("Beacon", "resume");
        super.onResume();
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(false);
        }
        ((BeaconNotifierApp) getApplication()).setCreateNotif(false);
    }

    @Override
    public void onPause() {
        Log.d("Beacon", "Pause");

        super.onResume();
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(true);
        }
        ((BeaconNotifierApp) getApplication()).setCreateNotif(true);
    }

    @Override
    public void onStop() {
        Log.d("Beacon", "stoped");

        super.onStop();
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.setBackgroundMode(true);
        }
        ((BeaconNotifierApp) getApplicationContext()).setCreateNotif(true);
    }

    @Override
    public void onDestroy() {
        Log.d("Beacon", "destroyed");
        super.onDestroy();
        mBeaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                Log.d("MainActivity", "didrangebeaconsregion : " + beacons.size());
                BeaconViewer beaconViewer = (BeaconViewer) fragmentList.get(0);

                beaconViewer.updateBeaconList(beacons);
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId",
                    Identifier.parse("53168465-4534-6543-2134-546865413213"),
                    Identifier.fromInt(10),
                    Identifier.fromInt(1)));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public class MyPagerAdapter extends FragmentPagerAdapter {
        private final String[] TITLES = { "Beacon", "History", "Favorites"};
        private List<Fragment> fragmentList;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);

            this.fragmentList = fragmentList;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}