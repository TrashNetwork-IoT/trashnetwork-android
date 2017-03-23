package happyyoung.trashnetwork.cleaning.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.BuildConfig;
import happyyoung.trashnetwork.cleaning.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.about_container) LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AboutPage aboutPage = new AboutPage(this)
                .setImage(R.mipmap.ic_launcher)
                .setDescription(getString(R.string.app_name))
                .addItem(new Element(getString(R.string.version) + ": " + BuildConfig.VERSION_NAME, R.drawable.ic_info_outline_48dp))
                .addEmail("GGGZ-1101-28@Live.cn", "GGGZ-1101-28@Live.cn")
                .addGitHub("TrashNetwork", "https://github.com/TrashNetwork");
        container.addView(aboutPage.create());
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
