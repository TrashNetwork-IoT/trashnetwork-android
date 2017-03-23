package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

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
import happyyoung.trashnetwork.cleaning.ui.widget.DateSelector;

public class WorkRecordFragment extends Fragment {
    private View rootView;
    @BindView(R.id.txt_no_record) TextView txtNoRecord;
    @BindView(R.id.work_record_list) SuperRecyclerView workRecordListView;
    private DateSelector dateSelector;

    private List<WorkRecord> workRecordList = new ArrayList<>();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        }, 0);

        if (cleaner == null && trash != null){
            adapter = new WorkRecordAdapter(getContext(), workRecordList, WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_CLEANER_VIEW,
                    false, null);
        }else if(cleaner != null && trash == null){
            adapter = new WorkRecordAdapter(getContext(), workRecordList, WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_TRASH_VIEW,
                    false, null);
        }else{
            adapter = new WorkRecordAdapter(getContext(), workRecordList, WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_FULL,
                    false, null);
        }
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

    private void refreshWorkRecord(boolean refresh){
        //TODO
        if(refresh)
            updateTime();
    }
}
