package happyyoung.trashnetwork.cleaning.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;

import com.android.volley.Request;

import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.database.model.LoginUserRecord;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.util.DatabaseUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.settings);
        findPreference("pref_logout").
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
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
                        return true;
                    }
                });
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
