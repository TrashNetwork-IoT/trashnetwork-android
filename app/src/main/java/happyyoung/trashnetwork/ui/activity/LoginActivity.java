package happyyoung.trashnetwork.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;

import java.util.ArrayList;
import java.util.List;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.database.model.LoginUserRecord;
import happyyoung.trashnetwork.net.PublicResultCode;
import happyyoung.trashnetwork.net.http.HttpApi;
import happyyoung.trashnetwork.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.net.model.request.LoginRequest;
import happyyoung.trashnetwork.net.model.result.LoginResult;
import happyyoung.trashnetwork.net.model.result.Result;
import happyyoung.trashnetwork.net.model.result.UserResult;
import happyyoung.trashnetwork.util.DatabaseUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class LoginActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_AUTO_USER_ID = "AutoUserId";

    // UI references.
    private AutoCompleteTextView mIdView;
    private EditText mPasswordView;
    private ProgressBar mProgress;
    private Button mSignInButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgress = (ProgressBar) findViewById(R.id.login_progress);
        mIdView = (AutoCompleteTextView) findViewById(R.id.login_id);
        final ImageView mPortraitView = (ImageView) findViewById(R.id.login_portrait);
        final List<LoginUserRecord> loginRecords = DatabaseUtil.findAllLoginUserRecords(10);
        List<String> loginIdRecords = new ArrayList<>();
        for(LoginUserRecord lur : loginRecords)
            loginIdRecords.add(Long.toString(lur.getUserId()));
        mIdView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, loginIdRecords));
        mIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(!s.toString().isEmpty()) {
                        long idNum = Long.valueOf(s.toString());
                        for (LoginUserRecord lur : loginRecords) {
                            if (lur.getUserId() == idNum) {
                                mPortraitView.setImageBitmap(lur.getPortrait());
                                return;
                            }
                        }
                    }
                }catch (NumberFormatException ignored){}
                mPortraitView.setImageResource(R.mipmap.ic_launcher);
            }
        });
        long autoId = getIntent().getLongExtra(BUNDLE_KEY_AUTO_USER_ID, -1);
        if(autoId > 0)
            mIdView.setText(Long.toString(autoId));

        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSignInButton = (Button) findViewById(R.id.button_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin(){
        final String idNum = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        if(idNum.isEmpty()){
            mIdView.setError(getString(R.string.error_field_required));
            return;
        }else if(password.isEmpty()){
            mPasswordView.setError(getString(R.string.error_field_required));
            return;
        }
        try{
            LoginRequest lr = new LoginRequest(Long.valueOf(idNum), password);
            mProgress.setVisibility(View.VISIBLE);
            mSignInButton.setEnabled(false);
            HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.AccountApi.LOGIN), Request.Method.PUT, null, lr,
                    new HttpApiJsonListener<LoginResult>(LoginResult.class) {
                        @Override
                        public boolean onDataCorrupted(Throwable e) {
                            mProgress.setVisibility(View.INVISIBLE);
                            mSignInButton.setEnabled(true);
                            return false;
                        }

                        @Override
                        public boolean onNetworkError(Throwable e) {
                            mProgress.setVisibility(View.INVISIBLE);
                            mSignInButton.setEnabled(true);
                            return false;
                        }

                        @Override
                        public void onResponse(LoginResult data) {
                            GlobalInfo.token = data.getToken();
                            afterLogin(LoginActivity.this, Long.valueOf(idNum), new HttpApiJsonListener<Result>(Result.class) {
                                @Override
                                public void onResponse(Result data) {}

                                @Override
                                public boolean onErrorResponse(int statusCode, Result errorInfo) {
                                    mProgress.setVisibility(View.INVISIBLE);
                                    mSignInButton.setEnabled(true);
                                    return false;
                                }

                                @Override
                                public boolean onDataCorrupted(Throwable e) {
                                    mProgress.setVisibility(View.INVISIBLE);
                                    mSignInButton.setEnabled(true);
                                    return false;
                                }

                                @Override
                                public boolean onNetworkError(Throwable e) {
                                    mProgress.setVisibility(View.INVISIBLE);
                                    mSignInButton.setEnabled(true);
                                    return false;
                                }
                            });
                        }

                        @Override
                        public boolean onErrorResponse(int statusCode, Result errorInfo) {
                            mProgress.setVisibility(View.INVISIBLE);
                            mSignInButton.setEnabled(true);
                            if(errorInfo.getResultCode() == PublicResultCode.LOGIN_USER_NOT_EXIST){
                                mIdView.setError(errorInfo.getMessage());
                                return true;
                            }else if (errorInfo.getResultCode() == PublicResultCode.LOGIN_INCORRECT_PASSWORD){
                                mPasswordView.setError(errorInfo.getMessage());
                                return true;
                            }
                            return false;
                        }
                    }));
        }catch (NumberFormatException nfe){
            nfe.printStackTrace();
            mIdView.setError(getString(R.string.error_illegal_id));
        }
    }

    public static void afterLogin(final Activity activity, long userId, final HttpApiJsonListener<Result> listener){
        HttpApi.startRequest(new HttpApiJsonRequest(activity, HttpApi.getApiUrl(HttpApi.AccountApi.USER_INFO_BY_ID, Long.toString(userId)), Request.Method.GET,
                GlobalInfo.token, null, new HttpApiJsonListener<UserResult>(UserResult.class) {
            @Override
            public void onResponse(UserResult data) {
                GlobalInfo.user = data.getUser();
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }

            @Override
            public boolean onErrorResponse(int statusCode, Result errorInfo) {
                return listener.onErrorResponse(statusCode, errorInfo);
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                return listener.onDataCorrupted(e);
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                return listener.onNetworkError(e);
            }
        }));
    }
}

