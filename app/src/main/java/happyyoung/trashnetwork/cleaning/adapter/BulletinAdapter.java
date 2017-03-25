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
import happyyoung.trashnetwork.cleaning.model.Bulletin;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-25
 */
public class BulletinAdapter extends RecyclerView.Adapter<BulletinAdapter.BulletinViewHolder> {
    private Context context;
    private List<Bulletin> bulletinList;

    public BulletinAdapter(Context context, List<Bulletin> bulletinList) {
        this.context = context;
        this.bulletinList = bulletinList;
    }

    @Override
    public BulletinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BulletinViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bulletin, parent, false));
    }

    @Override
    public void onBindViewHolder(BulletinViewHolder holder, int position) {
        Bulletin bulletin = bulletinList.get(position);
        bindViewHolder(bulletin, holder);
    }

    public static void bindViewHolder(Bulletin bulletin, BulletinViewHolder holder){
        User u = GlobalInfo.findUserById(bulletin.getPosterId());
        if(u == null)
            return;
        holder.posterPortrait.setImageBitmap(u.getPortrait());
        holder.txtPosterName.setText(u.getName());
        holder.txtPostTime.setText(DateTimeUtil.convertTimestamp(holder.itemView.getContext(), bulletin.getPostTime(),
                true, true, false));
        holder.txtTitle.setText(bulletin.getTitle());
        holder.txtContent.setText(bulletin.getTextContent());
    }

    @Override
    public int getItemCount() {
        return bulletinList.size();
    }

    public static class BulletinViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        @BindView(R.id.bulletin_portrait) ImageView posterPortrait;
        @BindView(R.id.txt_bulletin_username) TextView txtPosterName;
        @BindView(R.id.txt_bulletin_time) TextView txtPostTime;
        @BindView(R.id.txt_bulletin_title) TextView txtTitle;
        @BindView(R.id.txt_bulletin_content) TextView txtContent;

        public BulletinViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
