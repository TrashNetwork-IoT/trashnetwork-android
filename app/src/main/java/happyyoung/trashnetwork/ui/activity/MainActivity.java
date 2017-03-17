package happyyoung.trashnetwork.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.google.zxing.integration.android.IntentIntegrator;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.Application;
import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.model.Trash;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.service.LocationService;
import happyyoung.trashnetwork.ui.fragment.FeedbackFragment;
import happyyoung.trashnetwork.ui.fragment.MonitorFragment;
import happyyoung.trashnetwork.ui.fragment.WorkRecordFragment;
import happyyoung.trashnetwork.ui.fragment.WorkgroupFragment;
import happyyoung.trashnetwork.util.DatabaseUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.toolbar_main) Toolbar toolbar;
    @BindView(R.id.nav_main) NavigationView navView;
    private View mNavHeaderView;

    private MonitorFragment monitorFragment;
    private WorkgroupFragment workgroupFragment;
    private WorkRecordFragment workRecordFragment;
    private FeedbackFragment feedbackFragment;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);
        mNavHeaderView = navView.getHeaderView(0);
        updateUserInfo();

        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        monitorFragment = MonitorFragment.newInstance(this);
        workgroupFragment = WorkgroupFragment.newInstance(this);
        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_CLEANER:
                workRecordFragment = WorkRecordFragment.newInstance(this, GlobalInfo.user, null);
                break;
            case User.ACCOUNT_TYPE_MANAGER:
                workRecordFragment = WorkRecordFragment.newInstance(this, null, null);
                break;
        }
        feedbackFragment = FeedbackFragment.newInstance(this);
        transaction.add(R.id.main_container, monitorFragment)
                   .add(R.id.main_container, workgroupFragment)
                   .add(R.id.main_container, workRecordFragment)
                   .add(R.id.main_container, feedbackFragment)
                   .commit();
        onNavigationItemSelected(navView.getMenu().getItem(0));

        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_CLEANER){
            Application.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            Application.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            startService(new Intent(this, LocationService.class));
        }
        Application.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
            case R.id.nav_work_record:
                ft = mFragmentManager.beginTransaction();
                hideAllFragment(ft);
                ft.show(workRecordFragment);
                ft.commit();
                setTitle(getString(R.string.action_work_record));
                break;
            case R.id.nav_feedback:
                ft = mFragmentManager.beginTransaction();
                hideAllFragment(ft);
                ft.show(feedbackFragment);
                ft.commit();
                setTitle(getString(R.string.action_feedback));
                break;
            case R.id.nav_scan_qrcode:
                scanQRCode();
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
        ft.hide(workRecordFragment);
        ft.hide(feedbackFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_scan:
                scanQRCode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanQRCode(){
        new IntentIntegrator(this)
                .setOrientationLocked(false)
                .setCaptureActivity(ScanQRCodeActivity.class)
                .initiateScan();
    }
}
