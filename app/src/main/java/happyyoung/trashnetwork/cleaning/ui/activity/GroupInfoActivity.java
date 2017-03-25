package happyyoung.trashnetwork.cleaning.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.BulletinAdapter;
import happyyoung.trashnetwork.cleaning.model.Bulletin;
import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.net.PublicResultCode;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.BulletinListResult;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.ui.widget.PreferenceCard;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class GroupInfoActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_GROUP_ID = "GroupID";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_contact_view) View toolbarContactView;
    @BindView(R.id.txt_toolbar_contact_name) TextView txtContactToolbarName;
    @BindView(R.id.toolbar_portrait) ImageView toolbarPortrait;
    @BindView(R.id.txt_group_name) TextView txtGroupName;
    @BindView(R.id.txt_group_memeber) TextView txtGroupMember;
    @BindView(R.id.group_info_portrait) ImageView groupPortrait;
    @BindView(R.id.group_info_view) ViewGroup groupInfoView;
    private PreferenceCard bulletinCard;
    private PreferenceCard memberCard;
    private View bulletinView;

    private Group group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        ButterKnife.bind(this);
        toolbarContactView.setAlpha(0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                toolbarContactView.setAlpha(Math.abs(verticalOffset / (float)
                        appBarLayout.getTotalScrollRange()));
            }
        });

        group = GlobalInfo.findGroupById(getIntent().getLongExtra(BUNDLE_KEY_GROUP_ID, -1));
        if(group == null){
            finish();
            return;
        }

        txtContactToolbarName.setText(group.getName());
        txtGroupName.setText(group.getName());
        groupPortrait.setImageBitmap(group.getPortrait());
        txtGroupMember.setText(String.format(getString(R.string.group_member_foramt), group.getMemberList().size()));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.item_padding);

        bulletinCard = new PreferenceCard(this)
                .addGroup(getString(R.string.bulletin))
                .addItem(R.drawable.ic_bulletin_board, getString(R.string.action_view_more_bulletin), null,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(GroupInfoActivity.this, BulletinActivity.class);
                                intent.putExtra(BulletinActivity.BUNDLE_KEY_GROUP_ID, group.getGroupId());
                                startActivity(intent);
                            }
                        });
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_MANAGER){
            bulletinCard.addItem(R.drawable.ic_edit, getString(R.string.action_post_bulletin), null,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postNewBulletin();
                        }
                    });
        }

        bulletinCard.getView().setLayoutParams(params);
        groupInfoView.addView(bulletinCard.getView());

        memberCard = new PreferenceCard(this)
                .addGroup(getString(R.string.member));
        for(long userId : group.getMemberList()){
            final User u = GlobalInfo.findUserById(userId);
            if(u == null)
                continue;
            View v = LayoutInflater.from(this).inflate(R.layout.item_contactlist_contact, groupInfoView, false);
            ((ImageView)ButterKnife.findById(v, R.id.contact_portrait)).setImageBitmap(u.getPortrait());
            ((TextView)ButterKnife.findById(v, R.id.txt_contact_name)).setText(u.getName());
            TextView txtContactDesc = ButterKnife.findById(v, R.id.txt_contact_desc);
            txtContactDesc.setVisibility(View.VISIBLE);
            switch (u.getAccountType()){
                case User.ACCOUNT_TYPE_CLEANER:
                    txtContactDesc.setText(R.string.cleaner);
                    break;
                case User.ACCOUNT_TYPE_MANAGER:
                    txtContactDesc.setText(R.string.manager);
                    break;
            }
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GroupInfoActivity.this, UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.BUNDLE_KEY_SHOW_CHATTING, true);
                    intent.putExtra(UserInfoActivity.BUNDLE_KEY_USER_ID, u.getUserId());
                    startActivity(intent);
                }
            });
            memberCard.addCustomView(v);
        }

        groupInfoView.addView(memberCard.getView());
        getLatestBulletin();
    }

    private void getLatestBulletin(){
        String url = HttpApi.getApiUrl(HttpApi.GroupApi.QUERY_BULLETIN, group.getGroupId().toString(), "" + 1);
        HttpApi.startRequest(new HttpApiJsonRequest(this, url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<BulletinListResult>(BulletinListResult.class) {
                    @Override
                    public void onResponse(BulletinListResult data) {
                        Bulletin bulletin = data.getBulletinList().get(0);
                        View v = LayoutInflater.from(GroupInfoActivity.this).inflate(R.layout.item_bulletin, groupInfoView, false);
                        BulletinAdapter.bindViewHolder(bulletin, new BulletinAdapter.BulletinViewHolder(v));
                        bulletinCard.addCustomView(v);
                    }

                    @Override
                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                        bulletinCard.addItem(null, getString(R.string.no_bulletin), null, null);
                        if(errorInfo.getResultCode() == PublicResultCode.BULLETIN_NOT_FOUND)
                            return true;
                        return super.onErrorResponse(statusCode, errorInfo);
                    }

                    @Override
                    public boolean onDataCorrupted(Throwable e) {
                        bulletinCard.addItem(null, getString(R.string.no_bulletin), null, null);
                        return super.onDataCorrupted(e);
                    }

                    @Override
                    public boolean onNetworkError(Throwable e) {
                        bulletinCard.addItem(null, getString(R.string.no_bulletin), null, null);
                        return super.onNetworkError(e);
                    }
                }));
    }

    private void postNewBulletin(){
        Intent intent = new Intent(this, NewBulletinActivity.class);
        intent.putExtra(NewBulletinActivity.BUNDLE_KEY_GROUP_ID, group.getGroupId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
