package com.otpone.otpone.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;
import android.util.Log;

import com.otpone.otpone.model.Contact;
import com.otpone.otpone.database.ContactSchema.*;
import com.otpone.otpone.model.OTPMessage;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by DJ on 5/17/2017.
 */

public class ContactsDbHelper extends SQLiteOpenHelper{

    private static final String TAG = "ContactsDbHelper";

    private static final int VERSION  = 1;
    public static final String DATABASE_NAME = "contact_record.db";

    public ContactsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void createContactTables(SQLiteDatabase db){

        db.execSQL("CREATE TABLE " + ContactTable.NAME + "(" +
                ContactTable.cols.CONTACT_PHONE_NO + " NOT NULL " + " PRIMARY KEY " + "," +
                ContactTable.cols.FIRST_NAME + " NOT NULL " + "," +
                ContactTable.cols.MIDDLE_NAME + "," +
                ContactTable.cols.LAST_NAME + "," +
                ContactTable.cols.CONTACT_EMAIL_ID + ")"
        );
        Log.d(TAG, "Contact table creation complete");

        //-------------------------------------------------------------------

        // Now the table creation for Address.
        db.execSQL("create table " + ContactTable.ContactAddressTable.NAME + "(" +
                ContactTable.cols.CONTACT_PHONE_NO + " NOT NULL " + " PRIMARY KEY " + "," +
                ContactTable.ContactAddressTable.cols.FIRST_LINE + "," +
                ContactTable.ContactAddressTable.cols.SECOND_LINE + "," +
                ContactTable.ContactAddressTable.cols.CITY + "," +
                ContactTable.ContactAddressTable.cols.POSTAL_CODE + ")"
        );
        Log.d(TAG, "Contact Address table creation complete");
    }

    public static void createMessageRecordsTable(SQLiteDatabase db){
        if(!isExistingTable(db, MessagesRecordsTable.NAME)){
            db.execSQL("create table " + MessagesRecordsTable.NAME + "(" +
                    MessagesRecordsTable.cols.MESSAGE_FROM + " NOT NULL " + "," +
                    MessagesRecordsTable.cols.MESSAGE_TO + " NOT NULL " + "," +
                    MessagesRecordsTable.cols.MESSAGE_BODY + "," +
                    MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " NOT NULL " + ")"
            );
            Log.d(TAG, "Contacts Messages Records table creation complete.");
        }
    }


    // CONTENT VALUES---------------------------------------------------------------------------------------------------

    public static ContentValues getContentValues(Contact contact){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactTable.cols.CONTACT_PHONE_NO, contact.getPhoneNo());

        Contact.Name name = contact.getName();
        contentValues.put(ContactTable.cols.FIRST_NAME, name.getFirstName());
        contentValues.put(ContactTable.cols.MIDDLE_NAME, name.getMiddleName());
        contentValues.put(ContactTable.cols.LAST_NAME, name.getLastName());

        contentValues.put(ContactTable.cols.CONTACT_EMAIL_ID, contact.getEmailId());

        return contentValues;
    }

    public static ContentValues getContentValues(Contact.Address address, String phoneNo){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactTable.cols.CONTACT_PHONE_NO, phoneNo);
        contentValues.put(ContactTable.ContactAddressTable.cols.FIRST_LINE, address.getFirstLine());
        contentValues.put(ContactTable.ContactAddressTable.cols.SECOND_LINE, address.getSecondLine());
        contentValues.put(ContactTable.ContactAddressTable.cols.CITY, address.getCity());
        contentValues.put(ContactTable.ContactAddressTable.cols.POSTAL_CODE, address.getPostalCode());

        return contentValues;
    }

    public static ContentValues getContentValues(OTPMessage message){
        ContentValues contentValues = new ContentValues();

        contentValues.put(MessagesRecordsTable.cols.MESSAGE_FROM, message.getFrom());
        contentValues.put(MessagesRecordsTable.cols.MESSAGE_TO, message.getTo());
        contentValues.put(MessagesRecordsTable.cols.MESSAGE_BODY, message.getMsgBody());
        contentValues.put(MessagesRecordsTable.cols.MESSAGE_TIMESTAMP, message.getMessageTimestamp().getTime());

        return contentValues;
    }

    // DB OPERATIONS---------------------------------------------------------------------------------------------------

    public static void insertContactToDb(SQLiteDatabase db, Contact contact){

        db.insertOrThrow(ContactTable.NAME, null, getContentValues(contact));
        db.insertOrThrow(ContactTable.ContactAddressTable.NAME, null, getContentValues(contact.getAddress(), contact.getPhoneNo()));
    }

    public static void insertContactsToDb(SQLiteDatabase db, Contact[] contacts){

        for(Contact contact : contacts){
            db.insertOrThrow(ContactTable.NAME, null, getContentValues(contact));
            db.insertOrThrow(ContactTable.ContactAddressTable.NAME, null, getContentValues(contact.getAddress(), contact.getPhoneNo()));
        }
    }

    public static void insertContactsToDb(SQLiteDatabase db, List<Contact> contacts){

        for(Contact contact : contacts){
            db.insertOrThrow(ContactTable.NAME, null, getContentValues(contact));
            db.insertOrThrow(ContactTable.ContactAddressTable.NAME, null, getContentValues(contact.getAddress(), contact.getPhoneNo()));
        }
    }

    public static void updateContactInDb(SQLiteDatabase database, Contact contact){

        ContentValues values = getContentValues(contact);
        database.update(ContactTable.NAME, values,
                ContactTable.cols.CONTACT_PHONE_NO + " = ? ", new String[]{contact.getPhoneNo()});
    }

    //--------------------------------------------------------------------------------------------------------------------------

    public static void insertMessageToDb(SQLiteDatabase db, OTPMessage message){
        if(!isExistingTable(db, MessagesRecordsTable.NAME)){
            createMessageRecordsTable(db);
        }
        db.insertOrThrow(MessagesRecordsTable.NAME, null, getContentValues(message));
    }

    public static void updateMessageToDb(SQLiteDatabase database, OTPMessage message){
        ContentValues values = getContentValues(message);

        database.update(MessagesRecordsTable.NAME, values,
                MessagesRecordsTable.cols.MESSAGE_TO + " = ? ", new String[]{message.getTo()});
    }


    public static boolean isExistingTable(SQLiteDatabase db, String tableName){
        //Check for Learner's table
        Cursor c = null;
        try{
            c = db.rawQuery("SELECT name FROM " + "sqlite_master" + " WHERE type = 'table' AND name = ? ",
                    new String[]{tableName});
        }
        finally {
            if(c != null){
                if(c.getCount() == 0){
                    c.close();
                    return false;
                }
                c.close();
                return true;
            }
        }
        return false;
    }

    // READ------------------------------------------------------------------------------------------------------------

    public static final int SORT_ORDER_ASC = 0x002;
    public static final int SORT_ORDER_DESC = 0x004;
    public static final int SORT_ORDER_NONE = 0x008;
    public static final int SORT_BY_MSG_TIMESTAMPS = 0x0016;
    public static List<OTPMessage> getAllMessageRecordsFromDb(SQLiteDatabase db, int sortOrder){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }
        List<OTPMessage> messages;

        Cursor cMsg = null;

        try{
            switch(sortOrder){

                case SORT_ORDER_ASC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                                    + " ORDER BY " + MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " ASC "
                            , null);
                    break;

                case SORT_ORDER_DESC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                                    + " ORDER BY " + MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " DESC "
                            , null);
                    break;

                case SORT_ORDER_NONE:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                            , null);
                    break;

                default:
                    // No such(the provided one) order valid.
                    Log.w(TAG, "The provided sort order: " + sortOrder + " is invalid.");
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                            , null);
                    break;
            }

            if(!cMsg.moveToFirst()){
                // Data is corrupt.
                Log.e(TAG, "No message record found.");
                return null;
            }
            else{
                messages = new ArrayList<>(cMsg.getCount());

                // Populate the array.
                do{
                    OTPMessage message = null;
                    try{
                        message = new OTPMessage(cMsg.getString(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_FROM)),
                                cMsg.getString(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_TO)));
                    }
                    catch(OTPMessage.InvalidPhoneNoException ipne){
                        // Phone No is invalid.
                        Log.e(TAG, "Contact Phone No is invalid");
                    }
                    // Build the message and update the list.
                    if(message != null){
                        message.setMsgBody(cMsg.getString(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_BODY)));
                        message.setMessageTimestamp(
                                new Timestamp(cMsg.getLong(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_TIMESTAMP))));
                        // Add Message to the list
                        messages.add(message);
                    }

                }while(cMsg.moveToNext());
            }
        }
        finally {
            if(cMsg != null){
                cMsg.close();
            }
        }

        return messages;

    }

    public static List<OTPMessage> getAMessagesRecordFromDb(SQLiteDatabase db, String contactPhoneNo, int sortOrder){

        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }
        List<OTPMessage> messages;

        Cursor cMsg = null;

        try{
            switch(sortOrder){

                case SORT_ORDER_ASC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                                    + " WHERE " + MessagesRecordsTable.cols.MESSAGE_TO + " = ? "
                                    + " ORDER BY " + MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " ASC "
                            , new String[]{contactPhoneNo});
                    break;

                case SORT_ORDER_DESC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                                    + " WHERE " + MessagesRecordsTable.cols.MESSAGE_TO + " = ? "
                                    + " ORDER BY " + MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " DESC "
                            , new String[]{contactPhoneNo});
                    break;

                case SORT_ORDER_NONE:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                                    + " WHERE " + MessagesRecordsTable.cols.MESSAGE_TO + " = ? "
                            , new String[]{contactPhoneNo});
                    break;

                default:
                    // No such(the provided one) order valid.
                    Log.w(TAG, "The provided sort order: " + sortOrder + " is invalid.");
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + MessagesRecordsTable.NAME
                                    + " WHERE " + MessagesRecordsTable.cols.MESSAGE_TO + " = ? "
                            , new String[]{contactPhoneNo});
                    break;
            }

            if(!cMsg.moveToFirst()){
                // Data is corrupt.
                Log.e(TAG, "No message record found.");
                return null;
            }
            else{
                messages = new ArrayList<>(cMsg.getCount());

                // Populate the array.
                do{
                    OTPMessage message = null;
                    try{
                        message = new OTPMessage(cMsg.getString(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_FROM)),
                                cMsg.getString(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_TO)));
                    }
                    catch(OTPMessage.InvalidPhoneNoException ipne){
                        // Phone No is invalid.
                        Log.e(TAG, "Contact Phone No is invalid");
                    }
                    // Build the message and update the list.
                    if(message != null){
                        message.setMsgBody(cMsg.getString(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_BODY)));
                        message.setMessageTimestamp(
                                new Timestamp(cMsg.getLong(cMsg.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_TIMESTAMP))));
                        // Add Message to the list
                        messages.add(message);
                    }

                }while(cMsg.moveToNext());
            }
        }
        finally {
            if(cMsg != null){
                cMsg.close();
            }
        }

        return messages;

    }

    public static Map<Contact, List<OTPMessage>> getAllContactsAndMessagesFromDb(SQLiteDatabase db, int sortOrder){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }

        // Correct the sort order if its not.
        switch(sortOrder){
            case SORT_ORDER_ASC:
                break;
            case SORT_ORDER_DESC:
                break;
            case SORT_ORDER_NONE:
                break;
            default:
                sortOrder = SORT_ORDER_NONE;
                break;
        }

        Map<Contact, List<OTPMessage>> contactsAndMessages;
        Cursor c = null, cAddr = null;
        // Before running the query, check if the Table exists
        if(!isExistingTable(db, ContactTable.NAME)){
            return null;
        }

        try{
            c = db.rawQuery("SELECT * FROM " + ContactTable.NAME, null);
            contactsAndMessages = new LinkedHashMap<Contact, List<OTPMessage>>(c.getCount());

            if(!c .moveToFirst()){
                // Data is corrupt.
                Log.e(TAG, "Contact data is corrupt.(Names)");
                return null;
            }
            else{
                do{

                    List<OTPMessage> messages;

                    Contact contact;

                    // Get the Phone No. first
                    String phoneNo = c.getString(c.getColumnIndex(ContactTable.cols.CONTACT_PHONE_NO));

                    Contact.Name name = null;
                    name = new Contact.Name(
                            c.getString(c.getColumnIndex(ContactTable.cols.FIRST_NAME)),
                            c.getString(c.getColumnIndex(ContactTable.cols.MIDDLE_NAME)),
                            c.getString(c.getColumnIndex(ContactTable.cols.LAST_NAME)));

                    // Using the Phone No, retrieve the corresponding Address from the Address table.

                    Contact.Address address = null;

                    try{
                        cAddr = db.rawQuery(
                                "SELECT * FROM " + ContactTable.ContactAddressTable.NAME
                                        + " WHERE " + ContactTable.cols.CONTACT_PHONE_NO + " = ? "
                                , new String[]{phoneNo});

                        // Is there a valid address?
                        if(!cAddr.moveToFirst()){
                            Log.i(TAG, "The Address corresponding to the Contact with Phone No: " + phoneNo + " is non-existent/empty.");
                        }
                        else{
                            address = Contact.Address.Builder
                                    .newBuilder(
                                            cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.FIRST_LINE)),
                                            cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.CITY))
                                    )
                                    .setSecondLine(cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.SECOND_LINE)))
                                    .setPostalCode(cAddr.getInt((cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.POSTAL_CODE))))
                                    .createAddress();

                        }
                    }
                    finally {
                        if(cAddr != null){
                            cAddr.close();
                        }
                    }


                    contact = new Contact(name, phoneNo);

                    if(address != null){
                        contact.setAddress(address);
                    }
                    // Build the rest of the contact now.

                    // Get the Email ID
                    String emailId = c.getString(c.getColumnIndex(ContactTable.cols.CONTACT_EMAIL_ID));

                    contact.setEmailId(emailId);

                    // Retrieve the list of all the messages sent to this contact.
                    messages = getAMessagesRecordFromDb(db, phoneNo, sortOrder);

                    // Update the Map
                    contactsAndMessages.put(contact, messages);

                }while(c.moveToNext());
            }
        }
        finally {
            if(c != null){
                c.close();
            }
        }


        return contactsAndMessages;
    }

    /**
     * Get the entire messages' combined sent history sorted by the specified order with
     * A {@link Map} of all the {@link Contact}(s) for which at least one message has been sent.
     *
     * @param db : A readable {@link SQLiteDatabase} instance.
     * @param sortOrder : The order in which the entire(combined) message history should be sorted
     * @return : Mapping of every {@link OTPMessage} sent and its corresponding {@link Contact}
     */
    public static List<Pair<OTPMessage, Contact>> getAllMessagesAndContactsFromDb(SQLiteDatabase db, int sortOrder){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }

        String sOrder = null;
        // Correct the sort order if its not.
        switch(sortOrder){
            case SORT_ORDER_ASC:
                sOrder = "ASC";
                break;
            case SORT_ORDER_DESC:
                sOrder = "DESC";
                break;
            case SORT_ORDER_NONE:
                break;
            default:
                sortOrder = SORT_ORDER_NONE;
                break;
        }

        Map<String, Contact> contactsMap = new HashMap<>();
        List<Pair<OTPMessage, Contact>> messagesAndContacts;

        Cursor c = null, cAddr = null;
        // Before running the query, check if the Tables exist
        if(!isExistingTable(db, ContactTable.NAME)){
            Log.e(TAG, "Contact Table doesn't exist.");
            return null;
        }
        if(!isExistingTable(db, MessagesRecordsTable.NAME)){
            Log.e(TAG, "Messages Table doesn't exist.");
            return null;
        }

        try{
            // Create a JOIN query for the Contacts and Messages Table.
            c = db.rawQuery(
                    "SELECT * FROM " + MessagesRecordsTable.NAME
                            + " LEFT JOIN " + ContactTable.NAME
                            + " ON " + ContactTable.cols.CONTACT_PHONE_NO + " = " + MessagesRecordsTable.cols.MESSAGE_TO
                            + (sOrder != null ? (" ORDER BY " + MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " " + sOrder) : "")
                    , null);

            messagesAndContacts = new LinkedList<>();

            if(!c .moveToFirst()){
                Log.e(TAG, "Both Contact and Messages tables have no data.");
                return null;
            }
            else{

                do{

                    Contact contact;
                    OTPMessage message = null;

                    // Get the Phone No. first
                    String phoneNo = c.getString(c.getColumnIndex(ContactTable.cols.CONTACT_PHONE_NO));
                    // Check if this Contact for this Phone No has already been retrieved.
                    if(contactsMap.get(phoneNo) != null){
                        // Reuse the same contact
                        contact = contactsMap.get(phoneNo);
                    }
                    else{
                        // Build a new contact.
                        Contact.Name name = null;
                        name = new Contact.Name(
                                c.getString(c.getColumnIndex(ContactTable.cols.FIRST_NAME)),
                                c.getString(c.getColumnIndex(ContactTable.cols.MIDDLE_NAME)),
                                c.getString(c.getColumnIndex(ContactTable.cols.LAST_NAME)));

                        Contact.Address address = null;

                        try{
                            // Using the Phone No, retrieve the corresponding Address from the Address table.
                            cAddr = db.rawQuery(
                                    "SELECT * FROM " + ContactTable.ContactAddressTable.NAME
                                            + " WHERE " + ContactTable.cols.CONTACT_PHONE_NO + " = ? "
                                    , new String[]{phoneNo});

                            // Is there a valid address?
                            if(!cAddr.moveToFirst()){
                                Log.i(TAG, "The Address corresponding to the Contact with Phone No: " + phoneNo + " is non-existent/empty.");
                            }
                            else{
                                address = Contact.Address.Builder
                                        .newBuilder(
                                                cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.FIRST_LINE)),
                                                cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.CITY))
                                        )
                                        .setSecondLine(cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.SECOND_LINE)))
                                        .setPostalCode(cAddr.getInt((cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.POSTAL_CODE))))
                                        .createAddress();

                            }
                        }
                        finally {
                            if(cAddr != null){
                                cAddr.close();
                            }
                        }


                        contact = new Contact(name, phoneNo);

                        if(address != null){
                            contact.setAddress(address);
                        }
                        // Build the rest of the contact now.

                        // Get the Email ID
                        String emailId = c.getString(c.getColumnIndex(ContactTable.cols.CONTACT_EMAIL_ID));

                        contact.setEmailId(emailId);

                        // Save this contact for reuse.
                        contactsMap.put(phoneNo, contact);
                    }

                    // Create a Message
                    try{
                        message = new OTPMessage(c.getString(c.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_FROM)),
                                c.getString(c.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_TO)));
                    }
                    catch(OTPMessage.InvalidPhoneNoException ipne){
                        // Phone No is invalid.
                        Log.e(TAG, "Contact Phone No is invalid");
                        // TODO: Think of something appropriate.
                    }
                    // Build the message and update the list.
                    // TODO: Since a contact without any message would be meaningless in the current
                    // situation, move this to the top.
                    if(message != null){
                        message.setMsgBody(c.getString(c.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_BODY)));
                        message.setMessageTimestamp(
                                new Timestamp(c.getLong(c.getColumnIndex(MessagesRecordsTable.cols.MESSAGE_TIMESTAMP))));

                        // Pair up this message with the contact
                        messagesAndContacts.add(new Pair<OTPMessage, Contact>(message, contact));
                    }

                }while(c.moveToNext());
            }
        }
        finally {
            if(c != null){
                c.close();
            }
        }

        return messagesAndContacts;
    }

    // Utility Methods------------------------------------------------------------------------------------------------------
    public static File isExistingDatabase(Context context, String databaseName) {

        File dbPath = context.getDatabasePath(databaseName);
        return dbPath;
    }
}
