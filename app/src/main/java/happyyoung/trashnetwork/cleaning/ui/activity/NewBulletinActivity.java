package happyyoung.trashnetwork.cleaning.ui.activity;

import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.request.PostBulletinRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class NewBulletinActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_GROUP_ID = "GroupID";
    private static final int MENU_ID_ACTION_DONE = 0x666666;

    @BindView(R.id.edit_title) EditText editTitle;
    @BindView(R.id.edit_content) EditText editContent;

    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bulletin);
        ButterKnife.bind(this);
        groupId = getIntent().getLongExtra(BUNDLE_KEY_GROUP_ID, -1);
        if(groupId < 0){
            finish();
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, MENU_ID_ACTION_DONE, 0, R.string.action_done);
        item.setIcon(R.drawable.ic_done_white);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    private void postBulletin(){
        String title = editTitle.getText().toString();
        if(title.isEmpty()) {
            editTitle.setError(getString(R.string.error_field_required));
            return;
        }
        String content = editContent.getText().toString();
        if(content.isEmpty()) {
            editContent.setError(getString(R.string.error_field_required));
            return;
        }
        PostBulletinRequest request = new PostBulletinRequest(groupId, title, content);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage(getString(R.string.alert_waiting));
        pd.show();
        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.GroupApi.POST_BULLETIN), Request.Method.POST,
                GlobalInfo.token, request, new HttpApiJsonListener<Result>(Result.class) {
            @Override
            public void onResponse(Result data) {
                pd.dismiss();
                Toast.makeText(NewBulletinActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public boolean onErrorResponse(int statusCode, Result errorInfo) {
                pd.dismiss();
                return super.onErrorResponse(statusCode, errorInfo);
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                pd.dismiss();
                return super.onDataCorrupted(e);
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                pd.dismiss();
                return super.onNetworkError(e);
            }
        }));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                return true;
            case MENU_ID_ACTION_DONE:
                postBulletin();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
