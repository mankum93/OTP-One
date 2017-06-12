package com.otpone.otpone.model;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.otpone.otpone.SendOTPActivity;
import com.otpone.otpone.database.ContactsDbHelper;

import org.apache.commons.collections4.map.ListOrderedMap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.otpone.otpone.database.ContactsDbHelper.DATABASE_NAME;

/**
 * Created by DJ on 6/9/2017.
 */

public class Repository {

    private static final String TAG = Repository.class.getSimpleName();

    /**
     * Mapping of every Contact's sent messages sorted by the timestamps
     * of messages in descending order.
     */
    private Map<Contact, List<OTPMessage>> contactsAndMessages;

    /**
     * Mapping of every {@link OTPMessage} sent for its corresponding {@link Contact}
     */
    private ListOrderedMap<OTPMessage, Contact> messagesAndContacts;

    public SQLiteDatabase db;
    public ContactsDbHelper helper;

    private static Repository repoInstance;

    /**
     * Application Context.
     */
    private Application context;

    private final UUID repoSentMsgConsumptionToken = UUID.randomUUID();

    private Repository(@NonNull Application context) {

        this.context = context;

        helper = new ContactsDbHelper(context);
        db = helper.getWritableDatabase();

        // Register with the EventBus for receiving sent messages
        EventBus.getDefault().register(this);
    }

    /**
     * To perform initialization as per known requirements.
     */
    public void init(){
        // It is known that all the messages and contacts should be loaded.
        if(isExistingDbOnThisDevice(context)){
            contactsAndMessages = ContactsDbHelper.getAllContactsAndMessagesFromDatabase(db, ContactsDbHelper.SORT_ORDER_DESC);
        }

        // And now, retrieve the contacts for whom messages have been sent.
        if(contactsAndMessages != null && !contactsAndMessages.isEmpty()){
            Map<OTPMessage, Contact> msgsAndCtcts= ContactsDbHelper.getAllMessagesAndContactsFromDb(db, ContactsDbHelper.SORT_ORDER_DESC);
            if(msgsAndCtcts != null){
                messagesAndContacts = ListOrderedMap.listOrderedMap(msgsAndCtcts);
            }
        }

        // Unify duplicate Contacts.
        if(messagesAndContacts != null && !messagesAndContacts.isEmpty()){
            unifyDuplicateContacts();
        }
    }

    public static Repository getRepository(Application context){

        if(repoInstance == null){

            if(context == null){
                Log.e(TAG, "Context can't be null.");
                return null;
            }
            repoInstance = new Repository(context);
        }
        else{
            // Refresh context.
            if(context != null){
                repoInstance.context = context;
            }
        }
        return repoInstance;
    }

    public static Repository getRepository(){

        if(repoInstance == null){
            return null;
        }
        return repoInstance;
    }

    public Map<Contact, List<OTPMessage>> getContactsAndMessages() {
        return contactsAndMessages;
    }

    public Map<Contact, List<OTPMessage>> refreshContactsAndMessages() {

        // It is known that all the messages and contacts should be loaded.
        if(isExistingDbOnThisDevice(context)){
            Map<OTPMessage, Contact> msgsAndCtcts= ContactsDbHelper.getAllMessagesAndContactsFromDb(db, ContactsDbHelper.SORT_ORDER_DESC);
            if(msgsAndCtcts != null){
                messagesAndContacts = ListOrderedMap.listOrderedMap(msgsAndCtcts);
            }
        }
        return contactsAndMessages;
    }

    public void setContactsAndMessages(Map<Contact, List<OTPMessage>> contactsAndMessages) {
        this.contactsAndMessages = contactsAndMessages;
    }

    public Map<OTPMessage, Contact> getMessagesAndContacts() {
        return messagesAndContacts;
    }

    public Map<OTPMessage, Contact> refreshMessagesAndContacts() {

        // Retrieve the contacts for whom messages have been sent.
        if(contactsAndMessages != null && !contactsAndMessages.isEmpty()){
            Map<OTPMessage, Contact> msgsAndCtcts= ContactsDbHelper.getAllMessagesAndContactsFromDb(db, ContactsDbHelper.SORT_ORDER_DESC);
            if(msgsAndCtcts != null){
                messagesAndContacts = ListOrderedMap.listOrderedMap(msgsAndCtcts);
            }
        }

        // Unify duplicate Contacts.
        if(messagesAndContacts != null && !messagesAndContacts.isEmpty()){
            unifyDuplicateContacts();
        }

        return messagesAndContacts;
    }

    public void setMessagesAndContacts(Map<OTPMessage, Contact> messagesAndContacts) {
        this.messagesAndContacts = ListOrderedMap.listOrderedMap(messagesAndContacts);
    }

    public void updateMessagesAndContacts(Map<OTPMessage, Contact> messagesAndContacts) {

        // For first time, messagesAndContacts will be null. So, instantiate it
        if(this.messagesAndContacts == null){
            this.messagesAndContacts = ListOrderedMap.listOrderedMap(new LinkedHashMap<OTPMessage, Contact>());
        }
        if(messagesAndContacts != null && !messagesAndContacts.isEmpty()){
            for(Map.Entry<OTPMessage, Contact> entry : messagesAndContacts.entrySet()){
                updateMessagesAndContacts(entry.getKey(), entry.getValue());
            }
        }
    }

    // TODO: Right now, I am assuming that the "duplicate" contact is supposed
    // to be replaced. That may easily not be true and the caller of this method
    // must specify whether to replace it.
    public void updateMessagesAndContacts(OTPMessage message, Contact contact) {

        // For first time, messagesAndContacts will be null. So, instantiate it
        if(this.messagesAndContacts == null){
            this.messagesAndContacts = ListOrderedMap.listOrderedMap(new LinkedHashMap<OTPMessage, Contact>());
        }

        // Check if this Contact is existing.
        List<OTPMessage> messages = this.contactsAndMessages.get(contact);
        if(messages == null){
            // If not, update the Contacts and Messages too
            messages = new LinkedList<>();
            // Add it to the beginning because the messages are
            // arranged in descending order.
            messages.add(0, message);
            this.contactsAndMessages.put(contact, messages);

            // Then update the Messages and Contacts
            this.messagesAndContacts.put(0, message, contact);
        }
        else{
            // Add it to the beginning because the messages are
            // arranged in descending order.
            messages.add(0, message);
            this.contactsAndMessages.remove(contact);
            this.contactsAndMessages.put(contact, messages);

            this.messagesAndContacts.put(0, message, contact);
        }
    }

    private void unifyDuplicateContacts(){

        for(Contact contact : new HashSet<>(messagesAndContacts.values())){
            if(contactsAndMessages.containsKey(contact)){
                // Duplicate Contact. Replace.
                contactsAndMessages.put(contact, contactsAndMessages.remove(contact));
            }
        }
    }



    // Event Bus----------------------------------------------------------------------------------------------------------

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onMessageSent(SendOTPActivity.SentMessageEvent sentMessageEvent){

        Log.d(TAG, "Message sending event delivered to Repository.");

        // Get all the sent messages with their corresponding Contacts.
        updateMessagesAndContacts(sentMessageEvent.getSentMessages(repoSentMsgConsumptionToken));
    }

    // Utilities----------------------------------------------------------------------------------------------------------

    public static boolean isExistingDbOnThisDevice(Context context) {
        File dbPath = ContactsDbHelper.isExistingDatabase(context, DATABASE_NAME);
        if(dbPath == null){
            return false;
        }
        else{
            return true;
        }
    }

}
