package happyyoung.trashnetwork.cleaning.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.WorkRecordAdapter;
import happyyoung.trashnetwork.cleaning.database.model.SessionRecord;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.model.WorkRecord;
import happyyoung.trashnetwork.cleaning.net.PublicResultCode;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.net.model.result.WorkRecordListResult;
import happyyoung.trashnetwork.cleaning.ui.widget.PreferenceCard;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class UserInfoActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_USER_ID = "UserId";
    public static final String BUNDLE_KEY_SHOW_CHATTING = "ShowChatting";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_contact_view) View toolbarContactView;
    @BindView(R.id.txt_toolbar_contact_name) TextView txtContactToolbarName;
    @BindView(R.id.toolbar_portrait) ImageView toolbarPortrait;
    @BindView(R.id.txt_user_id) TextView txtUserId;
    @BindView(R.id.txt_user_name) TextView txtUserName;
    @BindView(R.id.user_info_portrait) ImageView userInfoPortrait;
    @BindView(R.id.txt_user_jobtype) TextView txtUserJobType;
    @BindView(R.id.btn_call) FloatingActionButton btnCall;
    @BindView(R.id.user_info_view) ViewGroup userInfoView;

    private User user;
    private PreferenceCard contactCard;
    private PreferenceCard workRecordCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
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

        user = GlobalInfo.findUserById(getIntent().getLongExtra(BUNDLE_KEY_USER_ID, -1));
        if(user == null){
            finish();
            return;
        }
        toolbarPortrait.setImageBitmap(user.getPortrait());
        userInfoPortrait.setImageBitmap(user.getPortrait());
        txtContactToolbarName.setText(user.getName());
        txtUserName.setText(user.getName());
        txtUserId.setText(user.getUserId().toString());
        switch (user.getAccountType()){
            case User.ACCOUNT_TYPE_MANAGER:
                txtUserJobType.setText(getString(R.string.manager));
                break;
            case User.ACCOUNT_TYPE_CLEANER:
                txtUserJobType.setText(getString(R.string.cleaner));
                break;
        }

        contactCard = new PreferenceCard(this)
                .addGroup(getString(R.string.action_contact));

        if(user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()){
            contactCard.addItem(R.drawable.ic_phone, getString(R.string.phone_number), user.getPhoneNumber(), null);
        }else{
            btnCall.setVisibility(View.GONE);
        }
        if(getIntent().getBooleanExtra(BUNDLE_KEY_SHOW_CHATTING, true)){
            contactCard.addItem(R.drawable.ic_chat, getString(R.string.action_send_message), null, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserInfoActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, SessionRecord.SESSION_TYPE_USER);
                    intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, user.getUserId());
                    startActivity(intent);
                    finish();
                }
            });
        }
        View v = contactCard.getView();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.item_padding);
        v.setLayoutParams(params);
        userInfoView.addView(v);

        if(user.getAccountType() == User.ACCOUNT_TYPE_CLEANER) {
            workRecordCard = new PreferenceCard(this)
                    .addGroup(getString(R.string.action_work_record))
                    .addItem(R.drawable.ic_history, getString(R.string.action_view_more_records), null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(UserInfoActivity.this, WorkRecordActivity.class);
                            intent.putExtra(WorkRecordActivity.BUNDLE_KEY_CLEANER_ID, user.getUserId());
                            startActivity(intent);
                        }
                    });
            userInfoView.addView(workRecordCard.getView());
            getWorkRecords();
        }
    }

    @OnClick(R.id.btn_call)
    void onBtnCallClick(View v){
        Uri number = Uri.parse("tel:" + user.getPhoneNumber());
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    private void getWorkRecords(){
        String url = HttpApi.getApiUrl(HttpApi.WorkRecordApi.QUERY_RECORD_BY_USER, user.getUserId().toString(), "" + 5);
        final WorkRecordAdapter.OnItemClickListener listener = new WorkRecordAdapter.OnItemClickListener() {
            @Override
            public void onTrashViewClick(Trash t) {
                Intent intent = new Intent(UserInfoActivity.this, TrashInfoActivity.class);
                intent.putExtra(TrashInfoActivity.BUNDLE_KEY_TRASH_ID, t.getTrashId());
                startActivity(intent);
            }

            @Override
            public void onCleanerViewClick(User u) {}
        };

        HttpApi.startRequest(new HttpApiJsonRequest(this, url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<WorkRecordListResult>(WorkRecordListResult.class) {
                    @Override
                    public void onResponse(WorkRecordListResult data) {
                        for(WorkRecord wr : data.getWorkRecordList()){
                            View v = LayoutInflater.from(UserInfoActivity.this).inflate(R.layout.item_work_record_trash_view, userInfoView, false);
                            WorkRecordAdapter.bindViewHolder(wr, new WorkRecordAdapter.WorkRecordViewHolder(v),
                                    WorkRecordAdapter.VIEW_TYPE_WORK_RECORD_TRASH_VIEW, true, listener);
                            workRecordCard.addCustomView(v);
                        }
                    }

                    @Override
                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                        if(errorInfo.getResultCode() == PublicResultCode.WORK_RECORD_NOT_FOUND){
                            workRecordCard.addItem(null, getString(R.string.no_record), null, null);
                            return true;
                        }
                        return super.onErrorResponse(statusCode, errorInfo);
                    }

                    @Override
                    public boolean onDataCorrupted(Throwable e) {
                        workRecordCard.addItem(null, getString(R.string.no_record), null, null);
                        return false;
                    }

                    @Override
                    public boolean onNetworkError(Throwable e) {
                        workRecordCard.addItem(null, getString(R.string.no_record), null, null);
                        return false;
                    }
                }));
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
