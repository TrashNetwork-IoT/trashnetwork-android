package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.service.MqttService;
import happyyoung.trashnetwork.cleaning.ui.fragment.workgroup.ContactFragment;
import happyyoung.trashnetwork.cleaning.ui.fragment.workgroup.MessageFragment;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class WorkgroupFragment extends Fragment {
    private View rootView;
    private MessageFragment messageFragment;
    private ContactFragment contactFragment;

    @BindView(R.id.tab_workgroup) TabLayout tabLayout;
    @BindView(R.id.tab_viewpager_workgroup) ViewPager viewPager;
    @BindView(R.id.txt_no_contact) TextView txtNoContact;
    @BindView(R.id.workgroup_view) View workgroupView;

    private ServiceConnection mqttServConn;

    public WorkgroupFragment() {
        // Required empty public constructor
    }

    public static WorkgroupFragment newInstance(Context context) {
        WorkgroupFragment fragment = new WorkgroupFragment();
        fragment.contactFragment = ContactFragment.newInstance(context);
        fragment.messageFragment = MessageFragment.newInstance(context);
        fragment.bindMqttService(context);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(rootView == null)
            rootView = inflater.inflate(R.layout.fragment_workgroup, container, false);
        ButterKnife.bind(this, rootView);

        if(messageFragment == null)
            messageFragment = MessageFragment.newInstance(getContext());
        if(contactFragment == null)
            contactFragment = ContactFragment.newInstance(getContext());
        if(mqttServConn == null)
            bindMqttService(getContext());

        viewPager.setAdapter(new WorkgroupPagerAdapter(getFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        if(!GlobalInfo.groupWorkers.isEmpty()){
            txtNoContact.setVisibility(View.GONE);
            workgroupView.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onDestroy() {
        getContext().unbindService(mqttServConn);
        super.onDestroy();
    }

    private void bindMqttService(Context context){
        Intent mqttIntent = new Intent(context, MqttService.class);
        context.startService(mqttIntent);
        mqttServConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    MqttService mqttService = ((MqttService.Binder) service).getService();
                    mqttService.addMQTTAction(new MqttService.MqttSubscriptionAction(
                            Application.MQTT_TOPIC_CHATTING, MqttService.TOPIC_TYPE_PRIVATE, GlobalInfo.user.getUserId(),
                            1, Application.ACTION_CHAT_MESSAGE_RECEIVED
                    ));
                    for (Group g : GlobalInfo.groupList){
                        mqttService.addMQTTAction(new MqttService.MqttSubscriptionAction(
                                Application.MQTT_TOPIC_CHATTING, MqttService.TOPIC_TYPE_GROUP, g.getGroupId(),
                                1, Application.ACTION_CHAT_MESSAGE_RECEIVED
                        ));
                    }
                    ((MqttService.Binder) service).getService().startWork(GlobalInfo.user.getUserId(),
                            GlobalInfo.token);
                }catch (MqttException me){
                    me.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        context.bindService(mqttIntent, mqttServConn, Context.BIND_AUTO_CREATE);
    }
}
