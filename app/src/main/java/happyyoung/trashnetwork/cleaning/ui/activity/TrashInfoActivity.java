package happyyoung.trashnetwork.cleaning.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.WorkRecordAdapter;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.model.WorkRecord;
import happyyoung.trashnetwork.cleaning.net.PublicResultCode;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.net.model.result.WorkRecordListResult;
import happyyoung.trashnetwork.cleaning.ui.widget.PreferenceCard;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.HttpUtil;
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
                .addItem(R.drawable.ic_history, getString(R.string.action_view_more_records), null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TrashInfoActivity.this, WorkRecordActivity.class);
                        intent.putExtra(WorkRecordActivity.BUNDLE_KEY_TRASH_ID, trash.getTrashId());
                        startActivity(intent);
                    }
                });
        trashInfoView.addView(workRecordCard.getView());
        if(GlobalInfo.user.getAccountType() != User.ACCOUNT_TYPE_CLEANER)
            btnClean.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_clean)
    void onBtnCleanClick(){
        HttpUtil.postWorkRecord(this, trash.getTrashId());
    }

    private void getWorkRecords(){
        String url = HttpApi.getApiUrl(HttpApi.WorkRecordApi.QUERY_RECORD_BY_TRASH, trash.getTrashId().toString(), "" + 5);
        final WorkRecordAdapter.OnItemClickListener listener = new WorkRecordAdapter.OnItemClickListener() {
            @Override
            public void onTrashViewClick(Trash t) {}

            @Override
            public void onCleanerViewClick(User u) {
                Intent intent = new Intent(TrashInfoActivity.this, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_SHOW_CHATTING, true);
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_USER_ID, u.getUserId());
                startActivity(intent);
            }
        };

        HttpApi.startRequest(new HttpApiJsonRequest(this, url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<WorkRecordListResult>(WorkRecordListResult.class) {
                    @Override
                    public void onResponse(WorkRecordListResult data) {
                        for(WorkRecord wr : data.getWorkRecordList()){
                            View v = LayoutInflater.from(TrashInfoActivity.this).inflate(R.layout.item_work_record_cleaner_view, trashInfoView, false);
                            WorkRecordAdapter.bindViewHolder(wr, new WorkRecordAdapter.WorkRecordViewHolder(v),
                                    WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_CLEANER_VIEW, true, listener);
                            workRecordCard.addCustomView(v);
                        }
                    }

                    @Override
                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                        if(errorInfo.getResultCode() == PublicResultCode.WORK_RECORD_NOT_FOUND){
                            workRecordCard.addItem(null, getString(R.string.no_record), null, null);
                            return true;
                        }
                        return super.onErrorResponse(statusCode, errorInfo);
                    }

                    @Override
                    public boolean onDataCorrupted(Throwable e) {
                        workRecordCard.addItem(null, getString(R.string.no_record), null, null);
                        return false;
                    }

                    @Override
                    public boolean onNetworkError(Throwable e) {
                        workRecordCard.addItem(null, getString(R.string.no_record), null, null);
                        return false;
                    }
                }));
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
