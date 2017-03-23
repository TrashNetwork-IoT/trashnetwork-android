package happyyoung.trashnetwork.cleaning.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.Request;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.database.model.LoginUserRecord;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.ui.widget.PreferenceCard;
import happyyoung.trashnetwork.cleaning.util.DatabaseUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class SettingsActivity extends AppCompatActivity {
    @BindView(R.id.settings_container) LinearLayout container;
    private PreferenceCard accountPref;
    private PreferenceCard otherPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        accountPref = new PreferenceCard(this)
                .addGroup(getString(R.string.account))
                .addItem(R.drawable.ic_exit, getString(R.string.action_logout), null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ProgressDialog pd = new ProgressDialog(SettingsActivity.this);
                        pd.setMessage(getString(R.string.alert_waiting));
                        pd.setCancelable(false);
                        pd.show();
                        HttpApi.startRequest(new HttpApiJsonRequest(SettingsActivity.this, HttpApi.getApiUrl(HttpApi.AccountApi.LOGOUT), Request.Method.DELETE, GlobalInfo.token, null,
                                new HttpApiJsonListener<Result>(Result.class) {
                                    @Override
                                    public void onResponse(Result data) {
                                        pd.dismiss();
                                        logout();
                                    }

                                    @Override
                                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                                        pd.dismiss();
                                        logout();
                                        return true;
                                    }

                                    @Override
                                    public boolean onDataCorrupted(Throwable e) {
                                        pd.dismiss();
                                        logout();
                                        return true;
                                    }

                                    @Override
                                    public boolean onNetworkError(Throwable e) {
                                        pd.dismiss();
                                        logout();
                                        return true;
                                    }
                                }));
                    }
                });

        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.item_padding);

        accountPref.getView().setLayoutParams(params);
        container.addView(accountPref.getView());

        otherPref = new PreferenceCard(this)
                .addGroup(getString(R.string.other))
                .addItem(R.drawable.ic_info_outline_48dp, getString(R.string.action_about), null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                    }
                });
        container.addView(otherPref.getView());
    }

    private void logout(){
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(LoginActivity.BUNDLE_KEY_AUTO_USER_ID, GlobalInfo.user.getUserId());

        LoginUserRecord lur = DatabaseUtil.findLoginUserRecord(GlobalInfo.user.getUserId());
        if(lur != null) {
            lur.setToken(null);
            lur.save();
        }
        GlobalInfo.logout(this);
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
