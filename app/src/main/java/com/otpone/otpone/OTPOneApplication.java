package com.otpone.otpone;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDexApplication;
import android.support.v4.util.Pair;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.otpone.otpone.database.ContactsDbHelper;
import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;
import com.otpone.otpone.model.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.otpone.otpone.database.ContactsDbHelper.DATABASE_NAME;

/**
 * Created by DJ on 5/16/2017.
 */

public class OTPOneApplication extends MultiDexApplication {

    private static final String TAG = OTPOneApplication.class.getSimpleName();

    private static final String DUMMY_CONTACTS_FILE_NAME = "ContactsTestData.json";

    // PLIVO AC DETAILS----------------------------------------------------------------------------------------------
    //public static final String PLIVO_AUTH_ID = "MAZGE5ZMUZZTJKZTQ3OW";
    //public static final String PLIVO_AUTH_TOKEN = "ZDFlMTY0NDA2Y2Y2OTc1NmJmZmQxYTEyZGVkZjBh";

    // PLIVO PARALLEL AC---------------------------------------------------------------------------------------------
    public static final String PLIVO_AUTH_ID = "MAOWVHMDZIOTBKNJFMND";
    public static final String PLIVO_AUTH_TOKEN = "ZTIzMzFiZTg3YTFkMzI4YjUxZWNkN2Y3ZGFhZjQz";

    // Registered Sender Phone No
    // Currently just 1 as a trial
    public static final String REGISTERED_SENDER_PHONE_NO = "9717552439";
    // A Plivo SandBox verified number
    public static final String REGISTERED_RECEIVER_PHONE_NO = "9818539195";
    //---------------------------------------------------------------------------------------------------------------

    // Initialize Plivo
    //public static final RestAPI PLIVO_API = new RestAPI(PLIVO_AUTH_ID, PLIVO_AUTH_TOKEN, "v1");

    private Repository repo;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Stetho
        Stetho.initializeWithDefaults(this);

        // Fetch the repo. and init() it.
        repo = Repository.getRepository(this);
        repo.init();

        // Check the Repository for existing data.
        if(repo.getContactsAndMessages() == null){
            populateContactsFromFile();
        }
    }

    private void populateContactsFromFile(){

        final List<Contact> dummyContactsData;
        Gson gson = new Gson();

        // Access the file using the Asset Manager
        final AssetManager assetManager = getAssets();

        try{
            Contact[] contacts = gson.fromJson(
                    new JsonReader(
                            new BufferedReader(
                                    new InputStreamReader(assetManager.open(DUMMY_CONTACTS_FILE_NAME)))), Contact[].class);
            dummyContactsData = new ArrayList<Contact>(Arrays.asList(contacts));
        }
        catch(IOException ioe){

            Log.e(TAG, "Error loading dummy contacts data.");
            ioe.printStackTrace();

            // TODO: Check the below behavior for production
            // ---------For Testing Mode only:-----------
            // App can't continue without Dummy Data. So, we throw
            // a RuntimeException.
            throw new RuntimeException("Error loading dummy contacts data.", ioe);
        }
        Log.d(TAG, "Dummy Contacts data loaded successfully from file");

        // Prepare for these contacts to be stashed directly in the Repository.
        Map<Contact, List<OTPMessage>> contactAndMessages = new LinkedHashMap<>();

        // Initialize the Repository with initially loaded dummy data
        for(Contact contact : dummyContactsData){
            contactAndMessages.put(contact, null);
        }
        repo.setContactsAndMessages(contactAndMessages);

        // Start a background thread for writing these records to Database
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Insert this data to Db
                // Create the required tables in the Db first

                // Contacts Table
                ContactsDbHelper.createContactTables(repo.db);
                // Messages Table
                ContactsDbHelper.createMessageRecordsTable(repo.db);

                // Insert the contacts into the table.
                ContactsDbHelper.insertContactsToDatabase(repo.db, dummyContactsData);

                Log.d(TAG, "Dummy Contacts data inserted successfully to Db.");

            }
        }).start();
    }
}
