package happyyoung.trashnetwork.cleaning.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */

public class ContactListContactHolder extends TreeNode.BaseNodeViewHolder<ContactListContactHolder.IconTextItem> {
    private Context mContext;
    private View.OnClickListener mCLickListener;

    private View mViewRoot;
    @BindView(R.id.txt_contact_name) TextView txtContactName;
    @BindView(R.id.contact_portrait) ImageView contactPortrait;

    public ContactListContactHolder(Context context, View.OnClickListener itemClicklistener) {
        super(context);
        mContext = context;
        mCLickListener = itemClicklistener;
    }

    @Override
    public View createNodeView(TreeNode node, IconTextItem value) {
        if(mViewRoot != null)
            return mViewRoot;
        mViewRoot = LayoutInflater.from(mContext).inflate(R.layout.item_contactlist_contact, null, false);
        ButterKnife.bind(this, mViewRoot);
        mViewRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mViewRoot.setOnClickListener(mCLickListener);
        updateNodeView(value);
        return mViewRoot;
    }

    public void updateNodeView(IconTextItem value) {
        txtContactName.setText(value.text);
        contactPortrait.setImageBitmap(value.icon);
    }

    public static class IconTextItem{
        private Bitmap icon;
        private String text;

        public IconTextItem(Bitmap icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }
}
