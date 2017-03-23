package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.FeedbackAdapter;
import happyyoung.trashnetwork.cleaning.model.Feedback;
import happyyoung.trashnetwork.cleaning.ui.widget.DateSelector;

public class FeedbackFragment extends Fragment {
    private View rootView;
    @BindView(R.id.feedback_list) SuperRecyclerView feedbackListView;
    private DateSelector dateSelector;

    private List<Feedback> feedbackList = new ArrayList<>();
    private FeedbackAdapter adapter;
    private Calendar endTime;
    private Calendar startTime;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    public static FeedbackFragment newInstance(Context context) {
        FeedbackFragment fragment = new FeedbackFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_feedback, container, false);
        ButterKnife.bind(this, rootView);
        endTime = Calendar.getInstance();
        startTime = Calendar.getInstance();
        dateSelector = new DateSelector(rootView, endTime, new DateSelector.OnDateChangedListener() {
            @Override
            public void onDateChanged(Calendar newDate) {
                endTime = newDate;
                refreshFeedback(true);
            }
        });

        feedbackListView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbackListView.getRecyclerView().setNestedScrollingEnabled(false);
        feedbackListView.getSwipeToRefresh().setColorSchemeResources(R.color.colorAccent);
        feedbackListView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeedback(true);
            }
        });

        feedbackListView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
                refreshFeedback(false);
            }
        }, 0);

        adapter = new FeedbackAdapter(getContext(), feedbackList);
        feedbackListView.setAdapter(adapter);
        refreshFeedback(true);
        return rootView;
    }

    private void updateTime(){
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);
        startTime.set(endTime.get(Calendar.YEAR), endTime.get(Calendar.MONTH), endTime.get(Calendar.DATE),
                0, 0, 0);
    }

    private void refreshFeedback(boolean refresh){
        //TODO
        if(refresh)
            updateTime();
    }
}
