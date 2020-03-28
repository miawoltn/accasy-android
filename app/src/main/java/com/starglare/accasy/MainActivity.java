package com.starglare.accasy;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.starglare.accasy.activity.BaseActivity;
import com.starglare.accasy.core.Helper;
import com.starglare.accasy.fragments.ReportMapFragment;

import static com.starglare.accasy.models.FragmentNames.CATEGORY_FRAGMENT;
import static com.starglare.accasy.models.FragmentNames.FEEDBACK_FRAGMENT;
import static com.starglare.accasy.models.FragmentNames.FORM_FRAGMENT;
import static com.starglare.accasy.models.FragmentNames.HISTORY_FRAGMENT;
import static com.starglare.accasy.models.FragmentNames.REPORT_MAP_FRAGMENT;

public class MainActivity extends BaseActivity {
    private BottomNavigationView bottomNavigationView;

    private Handler mHandler;
    public static int navItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.nav_view);

        mHandler = new Handler();

        setupNavigation();

        loadHomeFragment();

    }

    private void loadHomeFragment() {

        Fragment fragment = getHomeFragment();
        loadFragment(fragment,fragment.getClass().getName(),true);

        // refresh toolbar menu
        //invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // report
                return Helper.getCurrentFragment(FORM_FRAGMENT,null,null);
            case 1:
                // report map
                return Helper.getCurrentFragment(REPORT_MAP_FRAGMENT,null,null);
            case 2:
                // history
                return Helper.getCurrentFragment(HISTORY_FRAGMENT,null,null);
            case 3:
                // feedback
                return Helper.getCurrentFragment(FEEDBACK_FRAGMENT,null,null);
            default:
                return new Fragment();
        }
    }


    private void setupNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        if(navItemIndex == 1) {
                            FragmentManager fm = getSupportFragmentManager();
                            ReportMapFragment fragment = (ReportMapFragment) fm.findFragmentById(R.id.fragment);
                            fragment.changeBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
                            navItemIndex = 0;
                            return true;
                        }

                        if(navItemIndex == 0)
                            return true;

                        navItemIndex = 0;
                        break;
                    case R.id.nav_report:
                        if(navItemIndex == 2 || navItemIndex == 3) {
                            navItemIndex = 0;
                            loadHomeFragment();
                        }
                        FragmentManager fm = getSupportFragmentManager();


                        Fragment fragment = fm.findFragmentById(R.id.fragment);
                        if(fragment instanceof ReportMapFragment)
                            ((ReportMapFragment)fragment).changeBottomSheetState(BottomSheetBehavior.STATE_EXPANDED);
                        navItemIndex = 1;
                        return true;
                    case R.id.nav_history:
                        navItemIndex = 2;
                        break;
                    case R.id.nav_feedback:
                        navItemIndex = 3;
                        break;
                    default:
                        navItemIndex = 0;
                }

                loadHomeFragment();
                return true;
            }
        });

    }

    public void selectedNavigation(int Id) {
        bottomNavigationView.getMenu().findItem(Id).setChecked(true);
    }

}
