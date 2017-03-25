package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
import happyyoung.trashnetwork.cleaning.ui.activity.TrashInfoActivity;
import happyyoung.trashnetwork.cleaning.ui.activity.UserInfoActivity;
import happyyoung.trashnetwork.cleaning.ui.widget.DateSelector;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class WorkRecordFragment extends Fragment {
    private static final int RECORD_REQUEST_LIMIT = 20;

    private View rootView;
    @BindView(R.id.txt_no_record) TextView txtNoRecord;
    @BindView(R.id.work_record_list) SuperRecyclerView workRecordListView;
    private int workRecordViewType;
    private DateSelector dateSelector;

    private ArrayList<WorkRecord> workRecordList = new ArrayList<>();
    private WorkRecordAdapter adapter;
    private User cleaner;
    private Trash trash;
    private Calendar endTime;
    private Calendar startTime;

    public WorkRecordFragment() {
        // Required empty public constructor
    }

    public static WorkRecordFragment newInstance(Context context, @Nullable User cleaner, @Nullable Trash trash) {
        WorkRecordFragment fragment = new WorkRecordFragment();
        fragment.cleaner = cleaner;
        fragment.trash = trash;
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_work_record, container, false);
        ButterKnife.bind(this, rootView);

        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        dateSelector = new DateSelector(rootView, endTime, new DateSelector.OnDateChangedListener() {
            @Override
            public void onDateChanged(Calendar newDate) {
                endTime = newDate;
                refreshWorkRecord(true);
            }
        });

        workRecordListView.setLayoutManager(new LinearLayoutManager(getContext()));
        workRecordListView.getRecyclerView().setNestedScrollingEnabled(false);
        workRecordListView.getSwipeToRefresh().setColorSchemeResources(R.color.colorAccent);
        workRecordListView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWorkRecord(true);
            }
        });

        workRecordListView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
                refreshWorkRecord(false);
            }
        }, -1);

        if (cleaner == null && trash != null)
            workRecordViewType = WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_CLEANER_VIEW;
        else if(cleaner != null && trash == null)
            workRecordViewType = WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_TRASH_VIEW;
        else
            workRecordViewType = WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_FULL;

        WorkRecordAdapter.OnItemClickListener listener = new WorkRecordAdapter.OnItemClickListener() {
            @Override
            public void onTrashViewClick(Trash t) {
                Intent intent = new Intent(getContext(), TrashInfoActivity.class);
                intent.putExtra(TrashInfoActivity.BUNDLE_KEY_TRASH_ID, t.getTrashId());
                startActivity(intent);
            }

            @Override
            public void onCleanerViewClick(User u) {
                Intent intent = new Intent(getContext(), UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_SHOW_CHATTING, true);
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_USER_ID, u.getUserId());
                startActivity(intent);
            }
        };

        adapter = new WorkRecordAdapter(getContext(), workRecordList, workRecordViewType, false, listener);
        workRecordListView.setAdapter(adapter);
        refreshWorkRecord(true);
        return rootView;
    }

    private void updateTime(){
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);
        startTime.set(endTime.get(Calendar.YEAR), endTime.get(Calendar.MONTH), endTime.get(Calendar.DATE),
                0, 0, 0);
    }

    private void refreshWorkRecord(final boolean refresh){
        if(refresh) {
            updateTime();
            dateSelector.setEnable(false);
            workRecordListView.setRefreshing(true);
        }

        String url = "";
        switch (workRecordViewType){
            case WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_FULL:
                url = HttpApi.getApiUrl(HttpApi.WorkRecordApi.QUERY_RECORD, DateTimeUtil.getUnixTimestampStr(startTime.getTime()),
                        DateTimeUtil.getUnixTimestampStr(endTime.getTime()), "" + RECORD_REQUEST_LIMIT);
                break;
            case WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_TRASH_VIEW:
                url = HttpApi.getApiUrl(HttpApi.WorkRecordApi.QUERY_RECORD_BY_USER, cleaner.getUserId().toString(), DateTimeUtil.getUnixTimestampStr(startTime.getTime()),
                        DateTimeUtil.getUnixTimestampStr(endTime.getTime()), "" + RECORD_REQUEST_LIMIT);
                break;
            case WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_CLEANER_VIEW:
                url = HttpApi.getApiUrl(HttpApi.WorkRecordApi.QUERY_RECORD_BY_USER, trash.getTrashId().toString(), DateTimeUtil.getUnixTimestampStr(startTime.getTime()),
                        DateTimeUtil.getUnixTimestampStr(endTime.getTime()), "" + RECORD_REQUEST_LIMIT);
                break;
        }
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), url, Request.Method.GET, GlobalInfo.token, null, new HttpApiJsonListener<WorkRecordListResult>(WorkRecordListResult.class) {
            @Override
            public void onResponse(WorkRecordListResult data) {
                showContentView(true, refresh);
                if(refresh) {
                    workRecordList.clear();
                    adapter.notifyDataSetChanged();
                }
                for(WorkRecord wr : data.getWorkRecordList()){
                    workRecordList.add(wr);
                    endTime.setTimeInMillis(wr.getRecordTime().getTime() - 1000);
                    adapter.notifyItemInserted(workRecordList.size() - 1);
                }
                if(data.getWorkRecordList().size() < RECORD_REQUEST_LIMIT)
                    workRecordListView.setNumberBeforeMoreIsCalled(-1);
                else
                    workRecordListView.setNumberBeforeMoreIsCalled(1);
            }

            @Override
            public boolean onErrorResponse(int statusCode, Result errorInfo) {
                showContentView(false, refresh);
                if(errorInfo.getResultCode() == PublicResultCode.WORK_RECORD_NOT_FOUND) {
                    if(!refresh)
                        workRecordListView.setNumberBeforeMoreIsCalled(-1);
                    else
                        return true;
                }
                return super.onErrorResponse(statusCode, errorInfo);
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                showContentView(false, refresh);
                return super.onDataCorrupted(e);
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                showContentView(false, refresh);
                return super.onNetworkError(e);
            }
        }));
    }

    private void showContentView(boolean hasContent, boolean refresh){
        workRecordListView.setRefreshing(false);
        workRecordListView.hideMoreProgress();
        dateSelector.setEnable(true);
        if(refresh && !hasContent){
            workRecordListView.getRecyclerView().setVisibility(View.INVISIBLE);
            txtNoRecord.setVisibility(View.VISIBLE);
        }else if(refresh && hasContent){
            workRecordListView.getRecyclerView().setVisibility(View.VISIBLE);
            txtNoRecord.setVisibility(View.GONE);
        }
    }
}
