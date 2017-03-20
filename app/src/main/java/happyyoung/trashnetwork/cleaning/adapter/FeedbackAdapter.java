package happyyoung.trashnetwork.cleaning.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.model.Feedback;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-15
 */
public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private Context context;
    private List<Feedback> feedbackList;

    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @Override
    public FeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FeedbackViewHolder(LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false));
    }

    @Override
    public void onBindViewHolder(FeedbackViewHolder holder, int position) {
        Feedback fb = feedbackList.get(position);
        if(fb.getUserName() != null)
            holder.txtFeedbackUsername.setText(fb.getUserName());
        else
            holder.txtFeedbackUsername.setText(context.getString(R.string.anonymous_user));
        holder.txtFeedbackTime.setText(DateTimeUtil.convertTimestamp(context, fb.getFeedbackTime(), true, true, false));
        holder.txtFeedbackTitle.setText(fb.getTitle());
        holder.txtFeedbackContent.setText(fb.getTextContent());
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    class FeedbackViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.feedback_portrait) ImageView feedbackPortrait;
        @BindView(R.id.txt_feedback_username) TextView txtFeedbackUsername;
        @BindView(R.id.txt_feedback_time) TextView txtFeedbackTime;
        @BindView(R.id.txt_feedback_title) TextView txtFeedbackTitle;
        @BindView(R.id.txt_feedback_content) TextView txtFeedbackContent;

        public FeedbackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
