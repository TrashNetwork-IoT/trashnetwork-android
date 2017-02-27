package happyyoung.trashnetwork.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.net.http.HttpApi;
import happyyoung.trashnetwork.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.net.model.result.Result;
import happyyoung.trashnetwork.net.model.result.UserListResult;
import happyyoung.trashnetwork.net.model.result.UserResult;
import happyyoung.trashnetwork.ui.fragment.workgroup.ContactFragment;
import happyyoung.trashnetwork.ui.fragment.workgroup.MessageFragment;
import happyyoung.trashnetwork.util.GlobalInfo;

public class WorkgroupFragment extends Fragment {
    private boolean finFlag = false;
    private View rootView;
    private MessageFragment messageFragment;
    private ContactFragment contactFragment;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public WorkgroupFragment() {
        // Required empty public constructor
    }

    public static WorkgroupFragment newInstance(Context context) {
        WorkgroupFragment fragment = new WorkgroupFragment();
        fragment.contactFragment = ContactFragment.newInstance(context);
        fragment.messageFragment = MessageFragment.newInstance(context);
        fragment.getContacts(context);
        return fragment;
    }

    private void getContacts(final Context context){
        HttpApi.startRequest(new HttpApiJsonRequest(context, HttpApi.getApiUrl(HttpApi.AccountApi.ALL_GROUP_USERS), Request.Method.GET,
                GlobalInfo.token, null, new HttpApiJsonListener<UserListResult>(UserListResult.class) {
            @Override
            public void onResponse(UserListResult data) {
                finFlag = true;
                showContent();
                GlobalInfo.groupWorkers = data.getUserList();
            }

            @Override
            public boolean onErrorResponse(int statusCode, Result errorInfo) {
                return false;
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       getContacts(context);
                    }
                }, 1000);
                return false;
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                return false;
            }
        }));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(rootView == null)
            rootView = inflater.inflate(R.layout.fragment_workgroup, container, false);
        if(messageFragment == null)
            messageFragment = MessageFragment.newInstance(getContext());
        if(contactFragment == null)
            contactFragment = ContactFragment.newInstance(getContext());
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_workgroup);
        viewPager = (ViewPager) rootView.findViewById(R.id.tab_viewpager_workgroup);
        if(finFlag)
            showContent();

        return rootView;
    }

    private class WorkgroupPagerAdapter extends FragmentPagerAdapter{
        public WorkgroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.action_message);
                case 1:
                    return getString(R.string.action_contact);
            }
            return "";
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return messageFragment;
                case 1:
                    return contactFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void showContent(){
        if(rootView == null || rootView.findViewById(R.id.view_workgroup).getVisibility() == View.VISIBLE)
            return;
        viewPager.setAdapter(new WorkgroupPagerAdapter(getFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        rootView.findViewById(R.id.workgroup_progress).setVisibility(View.GONE);
        rootView.findViewById(R.id.view_workgroup).setVisibility(View.VISIBLE);
    }

}
