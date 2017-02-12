package happyyoung.trashnetwork.ui.activity;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import happyyoung.trashnetwork.R;


public class LoginActivity extends AppCompatActivity {
    // UI references.
    private AutoCompleteTextView mIdView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mIdView = (AutoCompleteTextView) findViewById(R.id.login_id);

        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.button_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin(){
        String idNum = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        if(idNum.isEmpty()){
            mIdView.setError(getString(R.string.error_field_required));
            return;
        }else if(password.isEmpty()){
            mPasswordView.setError(getString(R.string.error_field_required));
            return;
        }
    }
}

