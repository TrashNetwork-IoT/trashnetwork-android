package happyyoung.trashnetwork.cleaning.ui.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.BulletinAdapter;
import happyyoung.trashnetwork.cleaning.model.Bulletin;
import happyyoung.trashnetwork.cleaning.net.PublicResultCode;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.BulletinListResult;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class BulletinActivity extends AppCompatActivity {
    private static final int BULLETIN_REQUEST_LIMIT = 10;
    public static final String BUNDLE_KEY_GROUP_ID = "GroupID";

    @BindView(R.id.txt_no_bulletin) TextView txtNoBulletin;
    @BindView(R.id.bulletin_list) SuperRecyclerView bulletinListView;

    private List<Bulletin> bulletinList = new ArrayList<>();
    private BulletinAdapter adapter;
    private Calendar endTime;
    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupId = getIntent().getLongExtra(BUNDLE_KEY_GROUP_ID, -1);
        if(groupId < 0){
            finish();
            return;
        }
        bulletinListView.getRecyclerView().setNestedScrollingEnabled(false);
        bulletinListView.getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        bulletinListView.getSwipeToRefresh().setColorSchemeResources(R.color.colorAccent);
        bulletinListView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshBulletin(true);
            }
        });

        bulletinListView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
                refreshBulletin(false);
            }
        }, -1);

        adapter = new BulletinAdapter(this, bulletinList);
        bulletinListView.setAdapter(adapter);
        refreshBulletin(true);
    }

    private void refreshBulletin(final boolean refresh){
        if(refresh) {
            bulletinListView.setRefreshing(true);
            endTime = Calendar.getInstance();
        }
        String url = HttpApi.getApiUrl(HttpApi.GroupApi.QUERY_BULLETIN, "" + groupId,
                DateTimeUtil.getUnixTimestampStr(endTime.getTime()), "" + BULLETIN_REQUEST_LIMIT);
        HttpApi.startRequest(new HttpApiJsonRequest(this, url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<BulletinListResult>(BulletinListResult.class) {
                    @Override
                    public void onResponse(BulletinListResult data) {
                        showContent(true, refresh);
                        if(refresh){
                            bulletinList.clear();
                            adapter.notifyDataSetChanged();
                        }
                        for(Bulletin bulletin : data.getBulletinList()){
                            bulletinList.add(bulletin);
                            endTime.setTimeInMillis(bulletin.getPostTime().getTime() - 1000);
                            adapter.notifyItemInserted(bulletinList.size() - 1);
                        }
                        if(data.getBulletinList().size() < BULLETIN_REQUEST_LIMIT)
                            bulletinListView.setNumberBeforeMoreIsCalled(-1);
                        else
                            bulletinListView.setNumberBeforeMoreIsCalled(1);
                    }

                    @Override
                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                        showContent(false, refresh);
                        if(errorInfo.getResultCode() == PublicResultCode.BULLETIN_NOT_FOUND){
                            if(!refresh)
                                bulletinListView.setNumberBeforeMoreIsCalled(-1);
                            else
                                return true;
                        }
                        return super.onErrorResponse(statusCode, errorInfo);
                    }

                    @Override
                    public boolean onDataCorrupted(Throwable e) {
                        showContent(false, refresh);
                        return super.onDataCorrupted(e);
                    }

                    @Override
                    public boolean onNetworkError(Throwable e) {
                        showContent(false, refresh);
                        return super.onNetworkError(e);
                    }
                }));

    }

    private void showContent(boolean hasContent, boolean refresh){
        bulletinListView.setRefreshing(false);
        bulletinListView.hideMoreProgress();
        if(refresh && !hasContent){
            bulletinListView.getRecyclerView().setVisibility(View.INVISIBLE);
            txtNoBulletin.setVisibility(View.VISIBLE);
        }else if(refresh){
            bulletinListView.getRecyclerView().setVisibility(View.VISIBLE);
            txtNoBulletin.setVisibility(View.GONE);
        }
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
