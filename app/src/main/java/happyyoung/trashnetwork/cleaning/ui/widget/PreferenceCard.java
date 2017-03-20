package happyyoung.trashnetwork.cleaning.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindDimen;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.util.ImageUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-19
 */
public class PreferenceCard {
    private Context context;
    private CardView cardView;
    private LinearLayout linearLayout;

    @BindDimen(R.dimen.item_padding) int ITEM_PADDING;
    @BindDimen(R.dimen.activity_vertical_margin) int VERTICAL_MARGIN;
    @BindDimen(R.dimen.activity_horizontal_margin) int HORIZONTAL_MARGIN;
    @BindDimen(R.dimen.normal_icon_size) int NORMAL_ICON_SIZE;
    @BindDimen(R.dimen.normal_text_size) int NORMAL_TEXT_SIZE;
    @BindDimen(R.dimen.small_text_size) int SMALL_TEXT_SIZE;
    @BindDimen(R.dimen.separator_size) int SEPARATOR_SIZE;

    public PreferenceCard(Context context){
        this.context = context;
        cardView = new CardView(context);
        ButterKnife.bind(this, cardView);
        cardView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        cardView.setRadius(0);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        cardView.addView(linearLayout);
    }


    public PreferenceCard addGroup(String name){
        TextView titleTextView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(HORIZONTAL_MARGIN, ITEM_PADDING, HORIZONTAL_MARGIN, 0);
        titleTextView.setLayoutParams(params);
        titleTextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SMALL_TEXT_SIZE);
        titleTextView.setText(name);
        linearLayout.addView(titleTextView);

        return this;
    }

    public PreferenceCard addItem(@Nullable @DrawableRes Integer iconRes, String title, @Nullable String value, @Nullable View.OnClickListener listener){
        LinearLayout itemLayout = new LinearLayout(context);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        itemLayout.setBackgroundResource(outValue.resourceId);
        itemLayout.setPadding(HORIZONTAL_MARGIN, ITEM_PADDING, HORIZONTAL_MARGIN, ITEM_PADDING);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLayout.setLayoutParams(params);
        itemLayout.setClickable(true);
        if(listener != null)
            itemLayout.setOnClickListener(listener);
        linearLayout.addView(itemLayout);


        if(iconRes != null) {
            ImageView iconView = new ImageView(context);
            params = new LinearLayout.LayoutParams(NORMAL_ICON_SIZE, NORMAL_ICON_SIZE);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.setMarginEnd(HORIZONTAL_MARGIN);
            iconView.setLayoutParams(params);
            iconView.setImageBitmap(ImageUtil.getBitmapFromDrawable(context, iconRes));
            itemLayout.addView(iconView);
        }

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        if(iconRes == null)
            params.setMarginStart(NORMAL_ICON_SIZE + HORIZONTAL_MARGIN);
        textLayout.setLayoutParams(params);
        itemLayout.addView(textLayout);

        TextView titleTextView = new TextView(context);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, NORMAL_TEXT_SIZE);
        titleTextView.setText(title);
        textLayout.addView(titleTextView);

        if(value != null){
            TextView valueTextView = new TextView(context);
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SMALL_TEXT_SIZE);
            valueTextView.setText(value);
            textLayout.addView(valueTextView);
        }

        View separator = new View(context);
        separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SEPARATOR_SIZE));
        separator.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
        linearLayout.addView(separator);

        return this;
    }

    public PreferenceCard addCustomView(View v){
        linearLayout.addView(v);
        return this;
    }

    public CardView getView(){
        return cardView;
    }
}
