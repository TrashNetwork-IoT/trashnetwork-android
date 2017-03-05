package happyyoung.trashnetwork.ui.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.ui.fragment.MonitorFragment;
import happyyoung.trashnetwork.ui.fragment.WorkgroupFragment;
import happyyoung.trashnetwork.util.DatabaseUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private View mNavHeaderView;

    private MonitorFragment monitorFragment;
    private WorkgroupFragment workgroupFragment;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = (NavigationView) findViewById(R.id.nav_main);
        navView.setNavigationItemSelectedListener(this);
        mNavHeaderView = navView.getHeaderView(0);
        updateUserInfo();

        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        monitorFragment = MonitorFragment.newInstance(this);
        workgroupFragment = WorkgroupFragment.newInstance(this);
        transaction.add(R.id.main_container, monitorFragment)
                   .add(R.id.main_container, workgroupFragment)
                   .commit();

        onNavigationItemSelected(navView.getMenu().getItem(0));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("SetTextI18n")
    private void updateUserInfo(){
        ((ImageView)mNavHeaderView.findViewById(R.id.nav_header_portrait)).setImageBitmap(GlobalInfo.user.getPortrait());
        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_name)).setText(GlobalInfo.user.getName());
        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_id)).setText(GlobalInfo.user.getUserId().toString());
        DatabaseUtil.updateLoginRecord(GlobalInfo.user, GlobalInfo.token);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction ft;
        switch (id){
            case R.id.nav_monitor:
                setTitle(getString(R.string.action_monitor));
                ft = mFragmentManager.beginTransaction();
                hideAllFragment(ft);
                ft.show(monitorFragment);
                ft.commit();
                break;
            case R.id.nav_work_group:
                ft = mFragmentManager.beginTransaction();
                hideAllFragment(ft);
                ft.show(workgroupFragment);
                ft.commit();
                setTitle(getString(R.string.action_work_group));
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_exit:
                AlertDialog ad = new AlertDialog.Builder(this).setMessage(R.string.exit_query)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO: other actions before exit
                                GlobalInfo.logout(MainActivity.this);
                                finish();
                            }
                        }).setNegativeButton(R.string.action_cancel, null).create();
                ad.show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideAllFragment(FragmentTransaction ft){
        ft.hide(monitorFragment);
        ft.hide(workgroupFragment);
    }
}
