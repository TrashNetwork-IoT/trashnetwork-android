package happyyoung.trashnetwork.cleaning.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
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
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.model.WorkRecord;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-17
 */
public class WorkRecordAdapter extends RecyclerView.Adapter<WorkRecordAdapter.WorkRecordViewHolder> {
    public static final int VIEW_TYPE_WORK_RECORD_TRASH_VIEW = 1;
    public static final int VIEW_TYPE_WORK_RECORD_CLEANER_VIEW = 2;
    public static final int VIEW_TYPE_WORK_RECORD_FULL = 3;

    private Context context;
    private List<WorkRecord> workRecordList;
    private int viewType;
    private OnItemClickListener listener;
    private boolean showDate;

    public WorkRecordAdapter(Context context, List<WorkRecord> workRecordList, int viewType, boolean showDate, @Nullable OnItemClickListener listener) {
        this.viewType = viewType;
        this.context = context;
        this.workRecordList = workRecordList;
        this.listener = listener;
        this.showDate = showDate;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @Override
    public WorkRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_WORK_RECORD_TRASH_VIEW:
                return new WorkRecordViewHolder(LayoutInflater.from(context).inflate(R.layout.item_work_record_trash_view, parent, false));
            case VIEW_TYPE_WORK_RECORD_CLEANER_VIEW:
                return new WorkRecordViewHolder(LayoutInflater.from(context).inflate(R.layout.item_work_record_cleaner_view, parent, false));
            case VIEW_TYPE_WORK_RECORD_FULL:
                return new WorkRecordViewHolder(LayoutInflater.from(context).inflate(R.layout.item_work_record, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(WorkRecordViewHolder holder, int position) {
        WorkRecord wr = workRecordList.get(position);
        bindViewHolder(wr, holder, viewType, showDate, listener);
    }

    @Override
    public int getItemCount() {
        return workRecordList.size();
    }

    public interface OnItemClickListener{
        void onTrashViewClick(Trash t);
        void onCleanerViewClick(User u);
    }

    public static void bindViewHolder(WorkRecord wr, WorkRecordViewHolder holder,
                                      int viewType, boolean showDate, @Nullable final OnItemClickListener listener){
        if(showDate) {
            holder.txtWorkRecordDate.setVisibility(View.VISIBLE);
            holder.txtWorkRecordDate.setText(DateTimeUtil.convertTimestamp(holder.itemView.getContext(), wr.getRecordTime(), true, false));
        }else{
            holder.txtWorkRecordDate.setVisibility(View.GONE);
        }
        holder.txtWorkRecordTime.setText(DateTimeUtil.convertTimestamp(holder.itemView.getContext(), wr.getRecordTime(), false, true, false));
        if(viewType == VIEW_TYPE_WORK_RECORD_TRASH_VIEW || viewType == VIEW_TYPE_WORK_RECORD_FULL){
            final Trash t = GlobalInfo.findTrashById(wr.getTrashId());
            if(t == null)
                return;
            holder.txtTrashName.setText(t.getTrashName(holder.itemView.getContext()));
            holder.txtTrashDesc.setText(t.getDescription());
            View v;
            if(viewType == VIEW_TYPE_WORK_RECORD_TRASH_VIEW)
                v = holder.itemView;
            else
                v = holder.trashView;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onTrashViewClick(t);
                }
            });
        }
        if(viewType == VIEW_TYPE_WORK_RECORD_CLEANER_VIEW || viewType == VIEW_TYPE_WORK_RECORD_FULL){
            final User u = GlobalInfo.findUserById(wr.getCleanerId());
            if(u == null)
                return;
            holder.cleanerPortrait.setImageBitmap(u.getPortrait());
            holder.txtCleanerName.setText(u.getName());
            View v;
            if(viewType == VIEW_TYPE_WORK_RECORD_CLEANER_VIEW)
                v = holder.itemView;
            else
                v = holder.cleanerView;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                        listener.onCleanerViewClick(u);
                }
            });
        }
    }

    public static class WorkRecordViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        @BindView(R.id.txt_work_record_time) TextView txtWorkRecordTime;
        @BindView(R.id.txt_work_record_date) TextView txtWorkRecordDate;

        // For VIEW_TYPE_WORK_RECORD_CLEANER_VIEW and VIEW_TYPE_WORK_RECORD_FULL
        @Nullable @BindView(R.id.cleaner_portrait)
        ImageView cleanerPortrait;
        @Nullable @BindView(R.id.txt_cleaner_name)
        TextView txtCleanerName;

        // For VIEW_TYPE_WORK_RECORD_TRASH_VIEW and VIEW_TYPE_WORK_RECORD_FULL
        @Nullable @BindView(R.id.txt_trash_name)
        TextView txtTrashName;
        @Nullable @BindView(R.id.txt_trash_desc)
        TextView txtTrashDesc;

        // Only for VIEW_TYPE_WORK_RECORD_FULL
        @Nullable @BindView(R.id.trash_view_area)
        View trashView;
        @Nullable @BindView(R.id.cleaner_view_area)
        View cleanerView;

        public WorkRecordViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
