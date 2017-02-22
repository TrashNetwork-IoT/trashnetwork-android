package happyyoung.trashnetwork.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import happyyoung.trashnetwork.R;

public class MonitorFragment extends Fragment {

    public MonitorFragment() {
        // Required empty public constructor
    }

    public static MonitorFragment newInstance(Context context) {
        MonitorFragment fragment = new MonitorFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monitor, container, false);
    }

}
