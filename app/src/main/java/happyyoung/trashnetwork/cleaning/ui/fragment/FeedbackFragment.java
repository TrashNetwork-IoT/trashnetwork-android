package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.FeedbackAdapter;
import happyyoung.trashnetwork.cleaning.model.Feedback;
import happyyoung.trashnetwork.cleaning.net.PublicResultCode;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.FeedbackListResult;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.ui.widget.DateSelector;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class FeedbackFragment extends Fragment {
    private static final int FEEDBACK_REQUEST_LIMIT = 20;

    private View rootView;
    @BindView(R.id.txt_no_feedback) TextView txtNoFeedback;
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
        }, -1);

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

    private void refreshFeedback(final boolean refresh){
        if(refresh) {
            updateTime();
            dateSelector.setEnable(false);
            feedbackListView.setRefreshing(true);
        }

        String url = HttpApi.getApiUrl(HttpApi.FeedbackApi.QUERY_FEEDBACK, DateTimeUtil.getUnixTimestampStr(startTime.getTime()),
                DateTimeUtil.getUnixTimestampStr(endTime.getTime()), "" + FEEDBACK_REQUEST_LIMIT);
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), url, Request.Method.GET, GlobalInfo.token, null, new HttpApiJsonListener<FeedbackListResult>(FeedbackListResult.class) {
            @Override
            public void onResponse(FeedbackListResult data) {
                showContentView(false, refresh);
                if(refresh) {
                    feedbackList.clear();
                    adapter.notifyDataSetChanged();
                }
                for(Feedback fb : data.getFeedbackList()){
                    feedbackList.add(fb);
                    endTime.setTimeInMillis(fb.getFeedbackTime().getTime() - 1000);
                    adapter.notifyItemInserted(feedbackList.size() - 1);
                }
                if(data.getFeedbackList().size() < FEEDBACK_REQUEST_LIMIT)
                    feedbackListView.setNumberBeforeMoreIsCalled(-1);
                else
                    feedbackListView.setNumberBeforeMoreIsCalled(1);
            }

            @Override
            public boolean onErrorResponse(int statusCode, Result errorInfo) {
                showContentView(false, refresh);
                if(refresh && errorInfo.getResultCode() == PublicResultCode.FEEDBACK_NOT_FOUND)
                    return true;
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
        feedbackListView.setRefreshing(false);
        feedbackListView.hideMoreProgress();
        dateSelector.setEnable(true);
        if(refresh && !hasContent){
            feedbackListView.getRecyclerView().setVisibility(View.INVISIBLE);
            txtNoFeedback.setVisibility(View.VISIBLE);
        }else if(refresh && hasContent){
            feedbackListView.getRecyclerView().setVisibility(View.VISIBLE);
            txtNoFeedback.setVisibility(View.GONE);
        }
    }
}
