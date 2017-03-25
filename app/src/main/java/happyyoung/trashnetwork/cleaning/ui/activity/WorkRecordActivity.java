package happyyoung.trashnetwork.cleaning.ui.activity;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.ui.fragment.WorkRecordFragment;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class WorkRecordActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_TRASH_ID = "TrashID";
    public static final String BUNDLE_KEY_CLEANER_ID = "CleanerID";

    @BindView(R.id.toolbar_portrait) ImageView userPortrait;
    @BindView(R.id.txt_toolbar_contact_name) TextView txtContactName;

    private Trash trash;
    private User cleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        trash = GlobalInfo.findTrashById(getIntent().getLongExtra(BUNDLE_KEY_TRASH_ID, -1));
        cleaner = GlobalInfo.findUserById(getIntent().getLongExtra(BUNDLE_KEY_CLEANER_ID, -1));
        if(cleaner != null)
            setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_record);
        ButterKnife.bind(this);
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);

        if(cleaner != null){
            setSupportActionBar(toolbar);
            userPortrait.setImageBitmap(cleaner.getPortrait());
            txtContactName.setText(cleaner.getName());
        } else if(trash != null){
            toolbar.setVisibility(View.GONE);
            setTitle(trash.getTrashName(this));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WorkRecordFragment workRecordFragment = WorkRecordFragment.newInstance(this, cleaner, trash);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.work_record_container, workRecordFragment)
                .show(workRecordFragment)
                .commit();
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
