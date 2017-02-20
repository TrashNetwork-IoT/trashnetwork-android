package happyyoung.trashnetwork.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import com.android.volley.Request;

import java.util.List;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.database.model.LoginUserRecord;
import happyyoung.trashnetwork.net.http.HttpApi;
import happyyoung.trashnetwork.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.net.model.result.Result;
import happyyoung.trashnetwork.util.DatabaseUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class WelcomeActivity extends AppCompatActivity {
    private void startLoginActivity() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<LoginUserRecord> loginUserRecords = DatabaseUtil.findAllLoginUserRecords(10);
                if(loginUserRecords.isEmpty()){
                    startLoginActivity();
                    return;
                }
                final LoginUserRecord record = loginUserRecords.get(0);
                HttpApi.startRequest(new HttpApiJsonRequest(getApplicationContext(), HttpApi.getApiUrl(HttpApi.AccountApi.CHECK_LOGIN, Long.toString(record.getUserId())), Request.Method.GET,
                        record.getToken(), null, new HttpApiJsonListener<Result>(Result.class) {
                    @Override
                    public void onResponse(Result data) {
                        GlobalInfo.token = record.getToken();
                        LoginActivity.afterLogin(WelcomeActivity.this, record.getUserId(), new HttpApiJsonListener<Result>(Result.class) {
                            @Override
                            public void onResponse(Result data) {}

                            @Override
                            public boolean onErrorResponse(int statusCode, Result errorInfo) {
                                startLoginActivity();
                                return false;
                            }

                            @Override
                            public boolean onDataCorrupted(Throwable e) {
                                startLoginActivity();
                                return false;
                            }

                            @Override
                            public boolean onNetworkError(Throwable e) {
                                startLoginActivity();
                                return false;
                            }
                        });
                    }

                    @Override
                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                        startLoginActivity();
                        return false;
                    }

                    @Override
                    public boolean onDataCorrupted(Throwable e) {
                        startLoginActivity();
                        return false;
                    }

                    @Override
                    public boolean onNetworkError(Throwable e) {
                        startLoginActivity();
                        return false;
                    }
                }));
            }
        }, 1000);
    }
}
