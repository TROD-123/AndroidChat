package com.thirdarm.chat.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thirdarm.chat.MessageListFragment;
import com.thirdarm.chat.R;
import com.thirdarm.chat.sms.SmsHelper;
import com.thirdarm.chat.sms.SmsObject;
import com.thirdarm.chat.utils.Utils;

/**
 * Created by TROD on 20180303.
 */

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListVH> {

    private Context mContext;
    private Cursor mCursor;

    final private MessageListAdapterOnClickHandler mClickHandler;

    public interface MessageListAdapterOnClickHandler {
        void onClick(SmsObject smsObject);
    }

    public MessageListAdapter(Context context, MessageListAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public MessageListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_list_item, parent, false);
        return new MessageListVH(view);
    }

    @Override
    public void onBindViewHolder(MessageListVH holder, int position) {

        if (mCursor.moveToPosition(position)) {
            String initials = String.valueOf(position);
            String address = mCursor.getString(MessageListFragment.INDEX_MESSAGES_ADDRESS);
            String body = mCursor.getString(MessageListFragment.INDEX_MESSAGES_BODY);
            String creator = mCursor.getString(MessageListFragment.INDEX_MESSAGES_CREATOR);
            long dateReceived = mCursor.getLong(MessageListFragment.INDEX_MESSAGES_DATE_RECEIVED);
            long dateSent = mCursor.getLong(MessageListFragment.INDEX_MESSAGES_DATE_SENT);
            int errorCode = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_ERROR_CODE);
            boolean locked = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_LOCKED) == 1;
            int personSenderId = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_PERSON_SENDER_ID);
            int protocolId = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_PROTOCOL_ID);
            boolean read = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_READ) == 1;
            boolean seen = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_SEEN) == 1;
            String serviceCenter = mCursor.getString(MessageListFragment.INDEX_MESSAGES_SERVICE_CENTER);
            String subject = mCursor.getString(MessageListFragment.INDEX_MESSAGES_SUBJECT);
            int threadId = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_THREAD_ID);
            int type = mCursor.getInt(MessageListFragment.INDEX_MESSAGES_TYPE);

            SmsObject smsObject = new SmsObject(address, body, creator, dateReceived, dateSent, errorCode, locked, personSenderId, protocolId, read, seen, serviceCenter, subject, threadId, type);

            address = SmsHelper.getReadableAddressString(mContext, address);

            if (type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX || type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
                body = "You: " + body;
            }

            holder.bind(
                    smsObject,
                    initials,
                    address,
                    body,
                    Utils.convertMillisToReadableDateTime(dateReceived)
            );
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MessageListVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_initials;
        private TextView tv_name;
        private TextView tv_message;
        private TextView tv_time;

        SmsObject mSmsObject;


        public MessageListVH(View itemView) {
            super(itemView);

            tv_initials = (TextView) itemView.findViewById(R.id.message_list_item_initials_textview);
            tv_name = (TextView) itemView.findViewById(R.id.message_list_item_name_textview);
            tv_message = (TextView) itemView.findViewById(R.id.message_list_item_message_textview);
            tv_time = (TextView) itemView.findViewById(R.id.message_list_item_time_textview);

            itemView.setOnClickListener(this);
        }

        public void bind(SmsObject smsObject, String initials, String name, String message, String time) {
            tv_initials.setText(initials);
            tv_name.setText(name);
            tv_message.setText(message);
            tv_time.setText(time);

            mSmsObject = smsObject;
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String address = mCursor.getString(MessageListFragment.INDEX_MESSAGES_ADDRESS);

            mClickHandler.onClick(mSmsObject);

        }
    }
}
