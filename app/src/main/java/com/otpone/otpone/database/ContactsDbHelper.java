package com.otpone.otpone.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.otpone.otpone.model.Contact;
import com.otpone.otpone.database.ContactSchema.*;
import com.otpone.otpone.model.OTPMessage;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
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

        db.execSQL("create table " + ContactTable.NAME + "(" +
                ContactTable.cols.CONTACT_PHONE_NO + "," +
                ContactTable.cols.CONTACT_EMAIL_ID + ")"
        );
        Log.d(TAG, "Contact table creation complete");

        //-------------------------------------------------------------------

        // Now the table creation for Name.
        db.execSQL("create table " + ContactTable.ContactNameTable.NAME + "(" +
                ContactTable.cols.CONTACT_PHONE_NO + "," +
                ContactTable.ContactNameTable.cols.FIRST_NAME + "," +
                ContactTable.ContactNameTable.cols.MIDDLE_NAME + "," +
                ContactTable.ContactNameTable.cols.LAST_NAME + ")"
        );
        Log.d(TAG, "Contact Name table creation complete");

        //-------------------------------------------------------------------

        // Now the table creation for Address.
        db.execSQL("create table " + ContactTable.ContactAddressTable.NAME + "(" +
                ContactTable.cols.CONTACT_PHONE_NO + "," +
                ContactTable.ContactAddressTable.cols.FIRST_LINE + "," +
                ContactTable.ContactAddressTable.cols.SECOND_LINE + "," +
                ContactTable.ContactAddressTable.cols.CITY + "," +
                ContactTable.ContactAddressTable.cols.POSTAL_CODE + ")"
        );
        Log.d(TAG, "Contact Address table creation complete");
    }

    public static void createMesssageTable(SQLiteDatabase db, Contact contact){
        if(!isExistingTable(db, MessagesRecordTable.NAME_PREFIX + contact.getPhoneNo())){
            db.execSQL("create table " + MessagesRecordTable.NAME_PREFIX + contact.getPhoneNo() + "(" +
                    ContactTable.cols.CONTACT_PHONE_NO + "," +
                    MessagesRecordTable.cols.MESSAGE_FROM + "," +
                    MessagesRecordTable.cols.MESSAGE_BODY + "," +
                    MessagesRecordTable.cols.MESSAGE_TIMESTAMP + ")"
            );
            Log.d(TAG, "Contact Message Record table creation complete.");
        }
    }

    public static void createMesssageTable(SQLiteDatabase db, String phoneNo){
        if(!isExistingTable(db, MessagesRecordTable.NAME_PREFIX + phoneNo)){
            db.execSQL("create table " + MessagesRecordTable.NAME_PREFIX + phoneNo + "(" +
                    ContactTable.cols.CONTACT_PHONE_NO + "," +
                    MessagesRecordTable.cols.MESSAGE_FROM + "," +
                    MessagesRecordTable.cols.MESSAGE_BODY + "," +
                    MessagesRecordTable.cols.MESSAGE_TIMESTAMP + ")"
            );
            Log.d(TAG, "Contact Message Record table creation complete.");
        }
    }


    // CONTENT VALUES---------------------------------------------------------------------------------------------------

    public static ContentValues getContentValues(Contact contact){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactTable.cols.CONTACT_PHONE_NO, contact.getPhoneNo());
        contentValues.put(ContactTable.cols.CONTACT_EMAIL_ID, contact.getEmailId());

        return contentValues;
    }

    public static ContentValues getContentValues(Contact.Name name, String phoneNo){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactTable.cols.CONTACT_PHONE_NO, phoneNo);
        contentValues.put(ContactTable.ContactNameTable.cols.FIRST_NAME, name.getFirstName());
        contentValues.put(ContactTable.ContactNameTable.cols.MIDDLE_NAME, name.getMiddleName());
        contentValues.put(ContactTable.ContactNameTable.cols.LAST_NAME, name.getLastName());

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

    public static ContentValues getContentValues(OTPMessage message, String phoneNo){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactTable.cols.CONTACT_PHONE_NO, phoneNo);
        contentValues.put(MessagesRecordTable.cols.MESSAGE_FROM, message.getFrom());
        contentValues.put(MessagesRecordTable.cols.MESSAGE_BODY, message.getMsgBody());
        contentValues.put(MessagesRecordTable.cols.MESSAGE_TIMESTAMP, message.getMessageTimestamp().getTime());

        return contentValues;
    }

    // DB OPERATIONS---------------------------------------------------------------------------------------------------

    public static void insertContactToDatabase(SQLiteDatabase db, Contact contact){

        db.insertOrThrow(ContactTable.NAME, null, getContentValues(contact));
        db.insertOrThrow(ContactTable.ContactNameTable.NAME, null, getContentValues(contact.getContactName(), contact.getPhoneNo()));
        db.insertOrThrow(ContactTable.ContactAddressTable.NAME, null, getContentValues(contact.getAddress(), contact.getPhoneNo()));
    }

    public static void insertMessageToDatabase(SQLiteDatabase db, OTPMessage message){

        if(!isExistingTable(db, MessagesRecordTable.NAME_PREFIX + message.getTo())){
            createMesssageTable(db, message.getTo());
        }
        db.insertOrThrow(MessagesRecordTable.NAME_PREFIX + message.getTo(), null, getContentValues(message, message.getTo()));
    }

    public static void updateMessageInDatabase(SQLiteDatabase database, OTPMessage message, String phoneNo){
        ContentValues values = getContentValues(message, phoneNo);

        database.update(MessagesRecordTable.NAME_PREFIX + phoneNo, values,
                ContactTable.cols.CONTACT_PHONE_NO + "=?", new String[]{phoneNo});
    }


    public static void updateContactInDatabase(SQLiteDatabase database, Contact contact){

        ContentValues values = getContentValues(contact);
        database.update(ContactTable.NAME, values,
                ContactTable.cols.CONTACT_PHONE_NO + "=?", new String[]{contact.getPhoneNo()});
    }

    public static void updateContactNameInDatabase(SQLiteDatabase database, Contact contact){

        ContentValues values = getContentValues(contact.getContactName(), contact.getPhoneNo());

        database.update(ContactTable.ContactNameTable.NAME, values,
                ContactTable.cols.CONTACT_PHONE_NO + "=?", new String[]{contact.getPhoneNo()});
    }

    public static void updateContactAddressInDatabase(SQLiteDatabase database, Contact contact){

        ContentValues values = getContentValues(contact.getAddress(), contact.getPhoneNo());

        database.update(ContactTable.ContactAddressTable.NAME, values,
                ContactTable.cols.CONTACT_PHONE_NO + "=?", new String[]{contact.getPhoneNo()});
    }

    public static boolean isExistingTable(SQLiteDatabase db, String tableName){
        //Check for Learner's table
        Cursor c = db.rawQuery("SELECT name FROM " + "sqlite_master" + " WHERE type = 'table' AND name = ?",
                new String[]{tableName});
        if(c.getCount() == 0){
            c.close();
            return false;
        }
        return true;
    }

    // READ------------------------------------------------------------------------------------------------------------

    public static List<OTPMessage> getMessagesRecordFromDatabase(SQLiteDatabase db, String phoneNo){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }
        List<OTPMessage> messages;

        Cursor cMsg = db.rawQuery("SELECT * FROM " + MessagesRecordTable.NAME_PREFIX + phoneNo, null);
        if(!cMsg.moveToFirst()){
            // Data is corrupt.
            Log.e(TAG, "No message record found.");
            return null;
        }
        else{
            messages = new ArrayList<>(cMsg.getCount());
            do{
                OTPMessage message = null;
                try{
                    message = new OTPMessage(cMsg.getString(cMsg.getColumnIndex(MessagesRecordTable.cols.MESSAGE_FROM)),
                            phoneNo);
                }
                catch(OTPMessage.InvalidPhoneNoException ipne){
                    // Phone No is invalid.
                    Log.e(TAG, "Contact Phone No is invalid");
                }
                // Build the message and update the list.
                if(message != null){
                    message.setMsgBody(cMsg.getString(cMsg.getColumnIndex(MessagesRecordTable.cols.MESSAGE_BODY)));
                    message.setMessageTimestamp(
                            new Timestamp(cMsg.getLong(cMsg.getColumnIndex(MessagesRecordTable.cols.MESSAGE_TIMESTAMP))));
                    // Add Message to the list
                    messages.add(message);
                }

            }while(cMsg.moveToNext());
        }
        cMsg.close();

        return messages;

    }

    public static Map<Contact, List<OTPMessage>> getAllContactsAndMessagesFromDatabase(SQLiteDatabase db){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }
        Map<Contact, List<OTPMessage>> contactsAndMessages;
        Cursor c, cName, cAddr;
        // Before running the query, check if the Table exists
        if(!isExistingTable(db, ContactTable.NAME)){
            return null;
        }
        c = db.rawQuery("SELECT * FROM " + ContactTable.NAME, null);
        contactsAndMessages = new LinkedHashMap<Contact, List<OTPMessage>>(c.getCount());

        // Now using this Phone No, get the Name and Address
        cName = db.rawQuery("SELECT * FROM " + ContactTable.ContactNameTable.NAME, null);

        cAddr = db.rawQuery("SELECT * FROM " + ContactTable.ContactAddressTable.NAME, null);
        if(!cAddr.moveToFirst()){
            // Data is corrupt.
            Log.d(TAG, "Contact address is not empty.");
        }

        if(!c .moveToFirst() || !cName.moveToFirst()){
            // Data is corrupt.
            Log.e(TAG, "Contact data is corrupt.(Names)");
            return null;
        }
        else{
            do{

                List<OTPMessage> messages = new LinkedList<OTPMessage>();

                // Get the Phone No. first
                String phoneNo = c.getString(c.getColumnIndex(ContactTable.cols.CONTACT_PHONE_NO));

                Contact.Name name = null;
                Contact contact = null;
                Contact.Address address = null;

                name = new Contact.Name(
                        cName.getString(cName.getColumnIndex(ContactTable.ContactNameTable.cols.FIRST_NAME)),
                        cName.getString(cName.getColumnIndex(ContactTable.ContactNameTable.cols.MIDDLE_NAME)),
                        cName.getString(cName.getColumnIndex(ContactTable.ContactNameTable.cols.LAST_NAME)));

                if(cAddr.moveToNext()){
                    address = Contact.Address.Builder
                            .newBuilder(
                                    cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.FIRST_LINE)),
                                    cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.CITY)))
                            .setSecondLine(cAddr.getString(cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.SECOND_LINE)))
                            .setPostalCode(cAddr.getInt((cAddr.getColumnIndex(ContactTable.ContactAddressTable.cols.POSTAL_CODE))))
                            .createAddress();
                }

                contact = new Contact(name, phoneNo);

                if(address != null){
                    contact.setAddress(address);
                }
                // Build the rest of the contact now.

                // Get the Email ID
                String emailId = c.getString(c.getColumnIndex(ContactTable.cols.CONTACT_EMAIL_ID));

                contact.setEmailId(emailId);

                // Retrieve the list of all messages sent.
                Cursor cMsg = db.rawQuery("SELECT * FROM " + MessagesRecordTable.NAME_PREFIX + contact.getPhoneNo(), null);
                if(!cMsg.moveToFirst()){
                    // Data is corrupt.
                    Log.d(TAG, "No messages sent to this contact yet.");
                }
                else{
                    do{
                        OTPMessage message = null;
                        try{
                            message = new OTPMessage(cMsg.getString(cMsg.getColumnIndex(MessagesRecordTable.cols.MESSAGE_FROM)),
                                    phoneNo);
                        }
                        catch(OTPMessage.InvalidPhoneNoException ipne){
                            // Phone No is invalid.
                            Log.e(TAG, "Contact Phone No is invalid");
                        }
                        // Build the message and update the list.
                        if(message != null){
                            message.setMsgBody(cMsg.getString(cMsg.getColumnIndex(MessagesRecordTable.cols.MESSAGE_BODY)));
                            message.setMessageTimestamp(
                                    new Timestamp(cMsg.getLong(cMsg.getColumnIndex(MessagesRecordTable.cols.MESSAGE_TIMESTAMP))));
                            // Add Message to the list
                            messages.add(message);
                        }

                    }while(cMsg.moveToNext());
                }
                cMsg.close();

                // Update the Map
                contactsAndMessages.put(contact, messages);

            }while(c.moveToNext() && cName.moveToNext());

            c.close();
            cName.close();
            cAddr.close();
        }

        return contactsAndMessages;
    }

    // Utility Methods------------------------------------------------------------------------------------------------------
    public static File isExistingDatabase(Context context, String databaseName) {

        File dbPath = context.getDatabasePath(databaseName);
        return dbPath;
    }
}
