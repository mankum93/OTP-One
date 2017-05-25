package com.otpone.otpone;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.test.mock.MockApplication;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.otpone.otpone.database.ContactsDbHelper;
import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;
import com.otpone.otpone.util.DateTimeUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by DJ on 5/15/2017.
 */

public class MessagesRecordFragment extends Fragment {

    private RecyclerView messagesRecord;
    private MessagesRecordListAdapter adapter;

    private boolean initialized = false;
    private Map<Contact, List<OTPMessage>> contactsAndMessages;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve all messages from the Database.
        // This is what has been retrieved at the Application startup.
        // From this point ownwards(for this session), we shall keep track of
        // these records and keep updating them from cache
        contactsAndMessages = ((OTPOneApplication) getActivity().getApplication()).getContactsAndMessages();

        initialized = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_messages_record, container, false);

        // Retrieve the RecyclerView
        messagesRecord = (RecyclerView) root.findViewById(R.id.contact_list);

        // Setup the Recycler View
        adapter = new MessagesRecordListAdapter(contactsAndMessages);
        messagesRecord.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        messagesRecord.setLayoutManager(layoutManager);

        // Setup a RecyclerView divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(messagesRecord.getContext(),
                layoutManager.getOrientation());
        messagesRecord.addItemDecoration(dividerItemDecoration);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Register with the EventBus for receiving sent messages
        EventBus.getDefault().register(this);

        if(!initialized){
            // Retrieve fresh data.
            // Retrieve all messages from the Database.
            ContactsDbHelper helper = new ContactsDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase db = helper.getWritableDatabase();
            Map<Contact, List<OTPMessage>> contactsAndMessages = ContactsDbHelper.getAllContactsAndMessagesFromDatabase(db);

            // Refresh the data.
            adapter.setContactsAndMessages(contactsAndMessages);
        }
        initialized = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the messages records we have.
        outState.

    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister with the EventBus
        EventBus.getDefault().register(this);
    }

    /**
     * Adapter for Contacts List Recycler View.
     */
    private static class MessagesRecordListAdapter extends RecyclerView.Adapter<MessageViewHolder> implements RecyclerViewClickListener{

        private Map<OTPMessage, Contact> messagesAncContacts;

        private Map<Contact, List<OTPMessage>> contactsAndMessages;
        private List<OTPMessage> aggregateList;

        public MessagesRecordListAdapter(Map<Contact, List<OTPMessage>> contactsAndMessages) {
            this.contactsAndMessages = contactsAndMessages;
            // Initialize the list of aggregate messages
            mapMessagesWithContacts();
            createAggregateListofMessages();
            sortAggregateListOfMessages();
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
            holder.getContactName().setText(messagesAncContacts.get(messageToBeBound).getContactName().toString());
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
            //i.putExtra(EXTRA_CONTACTS_DATA, messagesRecord.get(position));
            //v.getContext().startActivity(i);
        }

        public void setContactsAndMessages(Map<Contact, List<OTPMessage>> contactsAndMessages) {
            this.contactsAndMessages = contactsAndMessages;
            refreshData();
        }

        private void refreshData(){
            mapMessagesWithContacts();
            createAggregateListofMessages();
            sortAggregateListOfMessages();
            notifyDataSetChanged();
        }

        private void mapMessagesWithContacts(){
            if(messagesAncContacts == null){
                messagesAncContacts = new LinkedHashMap<>();
            }
            else{
                messagesAncContacts.clear();
            }
            for(Map.Entry<Contact, List<OTPMessage>> entry : contactsAndMessages.entrySet()){
                Contact contact = entry.getKey();
                List<OTPMessage> messages = entry.getValue();

                if(messages == null || messages.isEmpty()){
                    continue;
                }
                for(OTPMessage msg : messages){
                    messagesAncContacts.put(msg, contact);
                }

            }
        }

        private void createAggregateListofMessages(){
            if(aggregateList == null){
                aggregateList = new LinkedList<>();
            }
            else{
                aggregateList.clear();
            }
            for(List<OTPMessage> list : contactsAndMessages.values()){
                if(list == null || list.isEmpty()){
                    continue;
                }
                aggregateList.addAll(list);
            }
        }

        private void sortAggregateListOfMessages(){
            Collections.sort(aggregateList);
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
