package com.otpone.otpone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;
import com.otpone.otpone.model.Repository;
import com.otpone.otpone.util.DateTimeUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by DJ on 5/15/2017.
 */

public class MessagesRecordFragment extends Fragment {

    private RecyclerView messagesRecordRecyclerView;
    private MessagesRecordListAdapter adapter;

    private boolean noChange = false;
    private Map<OTPMessage, Contact> messagesAndContacts;
    private static final String EXTRA_DISPLAY_STATUS = "EXTRA_DISPLAY_STATUS";

    private Repository repo;

    private static final String TAG = "MessagesRecordFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*if(savedInstanceState != null){
            // In this case, we just have to display the data that had been shown in the
            // previous orientation. So, no need to load it afresh.
            noChange = savedInstanceState.getBoolean(EXTRA_DISPLAY_STATUS);
        }*/

        repo = Repository.getRepository();
        messagesAndContacts = repo.getMessagesAndContacts();

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_messages_record, container, false);

        // Retrieve the RecyclerView
        messagesRecordRecyclerView = (RecyclerView) root.findViewById(R.id.contact_list);

        // Setup the Recycler View Adapter
        adapter = new MessagesRecordListAdapter(messagesAndContacts);
        messagesRecordRecyclerView.setAdapter(adapter);

        // Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        messagesRecordRecyclerView.setLayoutManager(layoutManager);

        // Setup a RecyclerView divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(messagesRecordRecyclerView.getContext(),
                layoutManager.getOrientation());
        messagesRecordRecyclerView.addItemDecoration(dividerItemDecoration);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Refreshing of data has to be done in case any new messages were sent.
        if(repo == null){
            // Repo will only be null because of reference release.
            repo = Repository.getRepository();
            messagesAndContacts = repo.getMessagesAndContacts();
            adapter.refreshMessagesAndContacts(messagesAndContacts);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        //outState.putBoolean(EXTRA_DISPLAY_STATUS, noChange);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Release the references.
        repo = null;
        messagesAndContacts = null;
    }

    /**
     * Adapter for Contacts List Recycler View.
     */
    private static class MessagesRecordListAdapter extends RecyclerView.Adapter<MessageViewHolder> implements RecyclerViewClickListener{

        private Map<OTPMessage, Contact> messagesAndContacts;
        private List<OTPMessage> aggregateList;

        public MessagesRecordListAdapter(Map<OTPMessage, Contact> messagesAndContacts) {
            this.messagesAndContacts = messagesAndContacts;

            if(aggregateList == null){
                aggregateList = new LinkedList<>();
            }
            else{
                aggregateList.clear();
            }
            if(messagesAndContacts != null){
                aggregateList.addAll(messagesAndContacts.keySet());
            }
        }

        public void refreshMessagesAndContacts(Map<OTPMessage, Contact> messagesAndContacts) {
            this.messagesAndContacts = messagesAndContacts;

            // Setting this means refreshing the list as well.
            if(aggregateList == null){
                aggregateList = new LinkedList<>();
            }
            else{
                aggregateList.clear();
            }
            if(messagesAndContacts != null){
                aggregateList.addAll(messagesAndContacts.keySet());
            }
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate the List item
            View itemRoot = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sent_messages_list, parent, false);

            MessageViewHolder messageViewHolder = new MessageViewHolder(itemRoot, this);

            // Return the populated ViewHolder
            return messageViewHolder;
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {

            OTPMessage messageToBeBound = aggregateList.get(position);

            // Bind the Name
            holder.getContactName().setText(messagesAndContacts.get(messageToBeBound).getName().toString());
            // Bind the Time Stamp
            holder.getTimeMessageSent().setText(DateTimeUtils.timeStampToHH_MMFormat(messageToBeBound.getMessageTimestamp()));
            // Bind the Message content.
            holder.getMsgContent().setText(messageToBeBound.getMsgBody());
        }

        @Override
        public int getItemCount() {
            return aggregateList.size();
        }

        @Override
        public void recyclerViewItemClicked(View v, int position) {
            // NOTE: This listener is reserved for launching any Activity through
            // clicking any list item.

            //Intent i = new Intent(v.getContext(), ContactInfoActivity.class);
            //i.putExtra(EXTRA_CONTACTS_DATA, messagesRecordRecyclerView.get(position));
            //v.getContext().startActivity(i);
        }
    }

    /**
     * ViewHolder for a Contact List item.(R.layout.item_contact_list)
     */
    private static class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView contactName;
        private TextView timeMessageSent;
        private TextView msgContent;

        private RecyclerViewClickListener itemListener;

        public MessageViewHolder(View itemView, RecyclerViewClickListener itemListener) {
            super(itemView);
            this.itemView.setOnClickListener(this);
            this.itemListener = itemListener;

            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            timeMessageSent = (TextView) itemView.findViewById(R.id.time_msg_sent);
            msgContent = (TextView) itemView.findViewById(R.id.msg_content);
        }

        @Override
        public void onClick(View v) {
            if(itemListener != null){
                itemListener.recyclerViewItemClicked(v, getAdapterPosition());
            }
        }

        public TextView getContactName() {
            return contactName;
        }

        public TextView getTimeMessageSent() {
            return timeMessageSent;
        }

        public TextView getMsgContent() {
            return msgContent;
        }
    }

    /**
     * Listener to be implemented by whoever knows the w.r.t the position
     * of the view.
     */
    public interface RecyclerViewClickListener {
        void recyclerViewItemClicked(View v, int position);
    }
}
