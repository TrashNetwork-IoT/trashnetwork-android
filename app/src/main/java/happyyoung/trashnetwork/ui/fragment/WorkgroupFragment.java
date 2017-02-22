package happyyoung.trashnetwork.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import happyyoung.trashnetwork.R;

public class WorkgroupFragment extends Fragment {

    public WorkgroupFragment() {
        // Required empty public constructor
    }

    public static WorkgroupFragment newInstance(Context context) {
        WorkgroupFragment fragment = new WorkgroupFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workgroup, container, false);
    }

}
