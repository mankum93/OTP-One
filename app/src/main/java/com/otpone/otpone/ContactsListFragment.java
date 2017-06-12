package com.otpone.otpone;


import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.otpone.otpone.ContactInfoActivity.EXTRA_CONTACTS_DATA;


/**
 * Fragment to display the list of contacts available to send messages to
 * in form of a list.
 */
public class ContactsListFragment extends Fragment {


    private RecyclerView contactsListView;
    private Repository repo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_contacts_list, container, false);

        // Retrieve the RecyclerView
        contactsListView = (RecyclerView) root.findViewById(R.id.contact_list);

        // Initialize the Repository
        repo = Repository.getRepository();

        // Retrieve the data from the Application
        Map<Contact, List<OTPMessage>> contactOTPMessageMap = repo.getContactsAndMessages();

        List<Contact> contactsData = null;
        if(contactOTPMessageMap != null){
            contactsData = new ArrayList<>(contactOTPMessageMap.keySet());
        }

        // Setup the Recycler View
        contactsListView.setAdapter(new ContactsListAdapter(contactsData));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        contactsListView.setLayoutManager(layoutManager);

        // Set a RecyclerView divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(contactsListView.getContext(),
                layoutManager.getOrientation());
        contactsListView.addItemDecoration(dividerItemDecoration);

        return root;
    }


    /**
     * Adapter for Contacts List Recycler View.
     */
    private static class ContactsListAdapter extends RecyclerView.Adapter<ContactViewHolder> implements RecyclerViewClickListener{

        private List<Contact> contactList;

        public ContactsListAdapter(List<Contact> contactList) {
            this.contactList = contactList;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate the List item
            View itemRoot = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact_list, parent, false);

            ContactViewHolder contactViewHolder = new ContactViewHolder(itemRoot, this);

            // Return the populated ViewHolder
            return contactViewHolder;
        }

        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {

            final Contact contactToBeBound = contactList.get(position);
            // Bind the Name
            holder.getContactName().setText(contactToBeBound.getName().toString());
            // Bind the Phone No.
            holder.getContactPhoneNo().setText(Contact.toFormattedPhoneNo(contactToBeBound.getPhoneNo()));
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        @Override
        public void recyclerViewItemClicked(View v, int position) {
            Intent i = new Intent(v.getContext(), ContactInfoActivity.class);
            i.putExtra(EXTRA_CONTACTS_DATA, contactList.get(position));
            v.getContext().startActivity(i);
        }
    }

    /**
     * ViewHolder for a Contact List item.(R.layout.item_contact_list)
     */
    private static class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView contactName;
        private TextView contactPhoneNo;

        private RecyclerViewClickListener itemListener;

        public ContactViewHolder(View itemView, RecyclerViewClickListener itemListener) {
            super(itemView);
            this.itemView.setOnClickListener(this);
            this.itemListener = itemListener;

            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            contactPhoneNo = (TextView) itemView.findViewById(R.id.contact_phone_no);
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

        public TextView getContactPhoneNo() {
            return contactPhoneNo;
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
