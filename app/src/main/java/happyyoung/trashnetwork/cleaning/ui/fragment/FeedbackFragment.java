package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.FeedbackAdapter;
import happyyoung.trashnetwork.cleaning.model.Feedback;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;

public class FeedbackFragment extends Fragment {
    private View rootView;
    @BindView(R.id.feedback_list) PullLoadMoreRecyclerView feedbackListView;
    @BindView(R.id.edit_date) EditText dateEdit;
    private DatePickerDialog datePickerDialog;

    private Calendar feedbackDate;
    private List<Feedback> feedbackList = new ArrayList<>();
    private FeedbackAdapter adapter;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    public static FeedbackFragment newInstance(Context context) {
        FeedbackFragment fragment = new FeedbackFragment();
        return fragment;
    }

    @OnClick({R.id.btn_date_decrease, R.id.btn_date_increase, R.id.edit_date})
    void onDateChangeClick(View v){
        switch (v.getId()){
            case R.id.btn_date_decrease:
                feedbackDate.set(Calendar.DAY_OF_MONTH, feedbackDate.get(Calendar.DAY_OF_MONTH) - 1);
                dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), feedbackDate.getTime(), true, false));
                refreshFeedback();
                break;
            case R.id.btn_date_increase:
                feedbackDate.set(Calendar.DAY_OF_MONTH, feedbackDate.get(Calendar.DAY_OF_MONTH) + 1);
                dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), feedbackDate.getTime(), true, false));
                refreshFeedback();
                break;
            case R.id.edit_date:
                datePickerDialog.updateDate(feedbackDate.get(Calendar.YEAR), feedbackDate.get(Calendar.MONTH), feedbackDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_feedback, container, false);
        ButterKnife.bind(this, rootView);
        feedbackDate = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                feedbackDate.set(year, month, dayOfMonth);
                dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), feedbackDate.getTime(), true, false));
                refreshFeedback();
            }
        }, feedbackDate.get(Calendar.YEAR), feedbackDate.get(Calendar.MONTH), feedbackDate.get(Calendar.DAY_OF_MONTH));

        dateEdit.setText(DateTimeUtil.convertTimestamp(getContext(), feedbackDate.getTime(), true, false));
        feedbackListView.setColorSchemeResources(R.color.colorAccent);
        feedbackListView.setLinearLayout();
        feedbackListView.setPullRefreshEnable(true);
        feedbackListView.setPushRefreshEnable(false);
        feedbackListView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                refreshFeedback();
            }

            @Override
            public void onLoadMore() {

            }
        });

        adapter = new FeedbackAdapter(getContext(), feedbackList);
        feedbackListView.setAdapter(adapter);
        refreshFeedback();
        return rootView;
    }

    private void refreshFeedback(){
        //TODO
    }
}
