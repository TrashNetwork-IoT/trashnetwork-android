package happyyoung.trashnetwork.cleaning.ui.activity;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.ui.widget.PreferenceCard;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.ImageUtil;

public class TrashInfoActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_TRASH_ID = "TrashId";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.txt_trash_name) TextView txtTrashName;
    @BindView(R.id.txt_trash_desc) TextView txtTrashDesc;
    @BindView(R.id.btn_clean) FloatingActionButton btnClean;
    @BindView(R.id.trash_info_view) ViewGroup trashInfoView;
    private MapView mapView;

    private Trash trash;
    private PreferenceCard locationCard;
    private PreferenceCard workRecordCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_info);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarLayout.setTitle(" ");

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    toolbarLayout.setTitle(trash.getTrashName(TrashInfoActivity.this));
                    isShow = true;
                } else if(isShow) {
                    toolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        trash = GlobalInfo.findTrashById(getIntent().getLongExtra(BUNDLE_KEY_TRASH_ID, -1));
        if(trash == null){
            finish();
            return;
        }

        txtTrashName.setText(trash.getTrashName(this));
        txtTrashDesc.setText(trash.getDescription());

        locationCard = new PreferenceCard(this)
                .addGroup(getString(R.string.location));
        BaiduMapOptions options = new BaiduMapOptions();
        options.zoomControlsEnabled(false);
        options.scrollGesturesEnabled(false);
        mapView = new MapView(this, options);
        mapView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.trash_info_map_height)));
        locationCard.addCustomView(mapView);
        BaiduMap bmap = mapView.getMap();
        LatLng pos = new LatLng(trash.getLatitude(), trash.getLongitude());
        bmap.setMapStatus(MapStatusUpdateFactory.newLatLng(pos));
        bmap.setMapStatus(MapStatusUpdateFactory.zoomTo(19));
        bmap.addOverlay(new MarkerOptions()
                        .draggable(false)
                        .alpha((float) 0.9)
                        .position(pos)
                        .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(this, R.drawable.ic_location_red))));


        View v = locationCard.getView();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.item_padding);
        v.setLayoutParams(params);
        trashInfoView.addView(v);

        workRecordCard = new PreferenceCard(this)
                .addGroup(getString(R.string.action_work_record))
                .addItem(R.drawable.ic_history, getString(R.string.action_view_more_records), null, null);
        trashInfoView.addView(workRecordCard.getView());
        if(GlobalInfo.user.getAccountType() != User.ACCOUNT_TYPE_CLEANER)
            btnClean.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
