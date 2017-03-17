package happyyoung.trashnetwork.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.adapter.WorkRecordAdapter;
import happyyoung.trashnetwork.model.Trash;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.model.WorkRecord;
import happyyoung.trashnetwork.util.DateTimeUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

public class WorkRecordFragment extends Fragment {
    private View rootView;
    @BindView(R.id.txt_no_record) TextView txtNoRecord;
    @BindView(R.id.work_record_list) PullLoadMoreRecyclerView workRecordListView;
    @BindView(R.id.edit_date) EditText dateEdit;

    private DatePickerDialog datePickerDialog;
    private Calendar workRecordDate;
    private List<WorkRecord> workRecordList = new ArrayList<>();
    private WorkRecordAdapter adapter;
    private User cleaner;
    private Trash trash;

    public WorkRecordFragment() {
        // Required empty public constructor
    }

    public static WorkRecordFragment newInstance(Context context, @Nullable User cleaner, @Nullable Trash trash) {
        WorkRecordFragment fragment = new WorkRecordFragment();
        fragment.cleaner = cleaner;
        fragment.trash = trash;
        return fragment;
    }

    @OnClick({R.id.btn_date_decrease, R.id.btn_date_increase, R.id.edit_date})
    void onDateChangeClick(View v){
        switch (v.getId()){
            case R.id.btn_date_decrease:
                workRecordDate.set(Calendar.DAY_OF_MONTH, workRecordDate.get(Calendar.DAY_OF_MONTH) - 1);
                dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), workRecordDate.getTime(), true, false));
                refreshWorkRecord();
                break;
            case R.id.btn_date_increase:
                workRecordDate.set(Calendar.DAY_OF_MONTH, workRecordDate.get(Calendar.DAY_OF_MONTH) + 1);
                dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), workRecordDate.getTime(), true, false));
                refreshWorkRecord();
                break;
            case R.id.edit_date:
                datePickerDialog.updateDate(workRecordDate.get(Calendar.YEAR), workRecordDate.get(Calendar.MONTH), workRecordDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_work_record, container, false);
        ButterKnife.bind(this, rootView);

        workRecordDate = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                workRecordDate.set(year, month, dayOfMonth);
                dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), workRecordDate.getTime(), true, false));
                refreshWorkRecord();
            }
        }, workRecordDate.get(Calendar.YEAR), workRecordDate.get(Calendar.MONTH), workRecordDate.get(Calendar.DAY_OF_MONTH));

        dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), workRecordDate.getTime(), true, false));
        workRecordListView.setColorSchemeResources(R.color.colorAccent);
        workRecordListView.setLinearLayout();
        workRecordListView.setPullRefreshEnable(true);
        workRecordListView.setPushRefreshEnable(false);
        workRecordListView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                refreshWorkRecord();
            }

            @Override
            public void onLoadMore() {

            }
        });

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
        refreshWorkRecord();
        return rootView;
    }

    private void refreshWorkRecord(){
        //TODO
    }
}
