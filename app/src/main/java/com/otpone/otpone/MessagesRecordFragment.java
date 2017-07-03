package com.otpone.otpone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
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

    private List<Pair<OTPMessage, Contact>> messagesAndContacts;

    private Repository repo;

    private static final String TAG = "MessagesRecordFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
            // If still the repo is null(it isn't supposed to be!),
            // we don't invoke method on it.
            if(repo != null){
                messagesAndContacts = repo.getMessagesAndContacts();
                adapter.refreshMessagesAndContacts(messagesAndContacts);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

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

        private List<Pair<OTPMessage, Contact>> messagesAndContacts;

        public MessagesRecordListAdapter(List<Pair<OTPMessage, Contact>> messagesAndContacts) {

            this.messagesAndContacts = new LinkedList<>();

            if(messagesAndContacts != null){
                this.messagesAndContacts.addAll(messagesAndContacts);
            }
        }

        public void refreshMessagesAndContacts(List<Pair<OTPMessage, Contact>> messagesAndContacts) {
            if(messagesAndContacts != null && !messagesAndContacts.isEmpty()){
                this.messagesAndContacts.clear();
                this.messagesAndContacts.addAll(messagesAndContacts);
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

            Pair<OTPMessage, Contact> pair = messagesAndContacts.get(position);
            OTPMessage messageToBeBound = pair.first;
            Contact contact= pair.second;

            // Bind the Name
            holder.getContactName().setText(contact.getName().toString());
            // Bind the Time Stamp
            holder.getTimeMessageSent().setText(DateTimeUtils.timeStampToHH_MMFormat(messageToBeBound.getMessageTimestamp()));
            // Bind the Message content.
            holder.getMsgContent().setText(messageToBeBound.getMsgBody());
        }

        @Override
        public int getItemCount() {
            return messagesAndContacts.size();
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
