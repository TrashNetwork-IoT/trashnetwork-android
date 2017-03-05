package happyyoung.trashnetwork.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.database.model.ChatMessageRecord;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.util.DateTimeUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-28
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_CHAT_MESSAGE_START = 1;
    private static final int VIEW_TYPE_CHAT_MESSAGE_END = 2;

    private List<Object> messageList;
    private Context context;
    private boolean showSenderName = false;

    public ChatMessageAdapter(Context context, List<Object> messageList) {
        this(context, messageList, false);
    }

    public ChatMessageAdapter(Context context, List<Object> messageList, boolean showSenderName) {
        this.messageList = messageList;
        this.context = context;
        this.showSenderName = showSenderName;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = messageList.get(position);
        if(obj instanceof MessageItem){
            switch (((MessageItem) obj).position){
                case MessageItem.POSITION_START:
                    return VIEW_TYPE_CHAT_MESSAGE_START;
                case MessageItem.POSITION_END:
                    return VIEW_TYPE_CHAT_MESSAGE_END;
            }
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_CHAT_MESSAGE_START:
                return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatbox_start, parent, false));
            case VIEW_TYPE_CHAT_MESSAGE_END:
                return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatbox_end, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = messageList.get(position);
        if(holder instanceof MessageViewHolder && obj instanceof MessageItem){
            MessageItem messageItem = (MessageItem) obj;
            ((MessageViewHolder) holder).senderPortrait.setImageBitmap(messageItem.sender.getPortrait());
            ((MessageViewHolder) holder).messageTimeText.setText(DateTimeUtil.convertTimestamp(context, messageItem.message.getMessageTime(), true, true));
            if(messageItem.message.getMessageType() == ChatMessageRecord.MESSAGE_TYPE_TEXT){
                TextView chatText = (TextView) ((MessageViewHolder) holder).chatMessageView.findViewWithTag("text");
                if(chatText == null) {
                    chatText = new TextView(context);
                    chatText.setTag("text");
                    chatText.setTextIsSelectable(true);
                    if(messageItem.position == MessageItem.POSITION_START)
                        chatText.setTextColor(Color.BLACK);
                    else
                        chatText.setTextColor(Color.WHITE);
                    ((MessageViewHolder) holder).chatMessageView.addView(chatText);
                }
                if(((MessageViewHolder) holder).progressBar != null){
                    if(messageItem.getMessage().getStatus() == ChatMessageRecord.MESSAGE_STATUS_SENDING)
                        ((MessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    else
                        ((MessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                }
                if(((MessageViewHolder) holder).senderNameText != null){
                    if(!showSenderName)
                        ((MessageViewHolder) holder).senderNameText.setVisibility(View.GONE);
                    else {
                        ((MessageViewHolder) holder).senderNameText.setVisibility(View.VISIBLE);
                        ((MessageViewHolder) holder).senderNameText.setText(messageItem.sender.getName());
                    }
                }
                chatText.setText(messageItem.message.getStrContent());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private ImageView senderPortrait;
        private ViewGroup chatMessageView;
        private TextView messageTimeText;
        private TextView senderNameText;
        private ProgressBar progressBar;

        public MessageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            senderPortrait = (ImageView) itemView.findViewById(R.id.chat_sender_portrait);
            chatMessageView = (ViewGroup) itemView.findViewById(R.id.chat_chatbox_view);
            messageTimeText = (TextView) itemView.findViewById(R.id.chat_msg_time);
            progressBar = (ProgressBar) itemView.findViewById(R.id.chat_send_progress);
            senderNameText = (TextView) itemView.findViewById(R.id.chat_sender_name);
        }
    }

    public static class MessageItem{
        public static final int POSITION_START = 1;
        public static final int POSITION_END = 2;

        private int position;
        private User sender;
        private ChatMessageRecord message;

        public MessageItem(int position, User sender, ChatMessageRecord message) {
            this.position = position;
            this.sender = sender;
            this.message = message;
        }

        public ChatMessageRecord getMessage() {
            return message;
        }

        public User getSender() {
            return sender;
        }

        public int getPosition() {
            return position;
        }
    }
}
