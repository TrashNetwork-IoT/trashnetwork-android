package happyyoung.trashnetwork.adapter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.util.ImageUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */

public class ContactListRootHolder extends TreeNode.BaseNodeViewHolder<ContactListRootHolder.IconTextItem> {
    private Context mContext;
    private AndroidTreeView mTreeView;
    private View mViewRoot;

    public ContactListRootHolder(Context context, AndroidTreeView treeView) {
        super(context);
        mContext = context;
        mTreeView = treeView;
    }

    @Override
    public View createNodeView(final TreeNode node, IconTextItem value) {
        mViewRoot = LayoutInflater.from(mContext).inflate(R.layout.item_contactlist_root, null, false);
        mViewRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final ImageView arrowIcon = (ImageView) mViewRoot.findViewById(R.id.item_icon_arrow);
        mViewRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!node.isExpanded()){
                    mTreeView.expandNode(node);
                    arrowIcon.setImageBitmap(ImageUtil.getBitmapFromDrawable(mContext, R.drawable.ic_expand_more));
                }else{
                    mTreeView.collapseNode(node);
                    arrowIcon.setImageBitmap(ImageUtil.getBitmapFromDrawable(mContext, R.drawable.ic_chevron_right));
                }
            }
        });
        updateNodeView(value);
        return mViewRoot;
    }

    public void updateNodeView(IconTextItem value){
        TextView itemText = (TextView) mViewRoot.findViewById(R.id.item_text);
        ImageView itemIcon = (ImageView) mViewRoot.findViewById(R.id.item_icon);
        TextView itemNumText = (TextView) mViewRoot.findViewById(R.id.item_num_text);
        itemText.setText(value.text);
        itemIcon.setImageResource(value.iconRes);
        itemNumText.setText(Integer.toString(value.num));
    }

    public static class IconTextItem{
        private int iconRes;
        private String text;
        private int num;

        public IconTextItem(@DrawableRes int iconRes, String text, int num) {
            this.iconRes = iconRes;
            this.text = text;
            this.num = num;
        }
    }
}
