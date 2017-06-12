package com.otpone.otpone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;


import static com.otpone.otpone.OTPOneApplication.REGISTERED_SENDER_PHONE_NO;
import static com.otpone.otpone.SendOTPActivity.EXTRA_OTP_MESSAGE;

/**
 * This Activity displays contact info. of a person that includes,
 * {Contact Name, Contact Phone No, Contact Email Id, Contact Address}
 * and the option to send an OTP message to the person.
 */
public class ContactInfoActivity extends AppCompatActivity {

    private static final String TAG = ContactInfoActivity.class.getSimpleName();

    public static final String EXTRA_CONTACTS_DATA = "EXTRA_CONTACTS_DATA";

    // Names of fields are pretty much self-explanatory.
    private TextView contactName;
    private TextView contactPhoneNo;
    private TextView emailId;
    private TextView address;
    private Button sendButton;
    private OTPMessage otpMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        // Check if state is available to restore
        if(savedInstanceState != null){
            otpMessage = savedInstanceState.getParcelable(EXTRA_OTP_MESSAGE);
        }

        // Retrieve the views
        contactName = (TextView) findViewById(R.id.contact_name);
        contactPhoneNo = (TextView) findViewById(R.id.contact_phone_no);
        emailId = (TextView) findViewById(R.id.email_id);
        address = (TextView) findViewById(R.id.address);

        sendButton = (Button) findViewById(R.id.send_msg_button);

        final Contact contact = getIntent().getParcelableExtra(EXTRA_CONTACTS_DATA);

        if(contact == null){
            Log.e(TAG, "There wan't any contact data sent to be displayed." +
                    "Displaying a blank page in that case with Placeholders.");
            // TODO: We might want to display an error dialog in this case so that
            // the user might go back and retry if that works.
            return;
        }

        // Bind data to views
        contactName.setText(contact.getName().toString());
        contactPhoneNo.setText(Contact.toFormattedPhoneNo(contact.getPhoneNo()));
        emailId.setText(contact.getEmailId());
        address.setText(contact.getAddress().toString());

        // Prepare parts of OTPMessage to be sent.
        otpMessage = null;
        try {
            otpMessage = new OTPMessage(REGISTERED_SENDER_PHONE_NO, contact.getPhoneNo());
        } catch (OTPMessage.InvalidPhoneNoException e) {
            // If the phone number is not valid, try another one maybe.
            // Since, right now we have just one number, we can't do much
            // other than logging and stopping the message to be sent.

            // There is one more thing we might be able to do BTW - Send an Email.
            // TODO: Either send Receiver's Email as a Backup or try a different Twilio number.
            Log.e(TAG, "One or more Twilio numbers turned out to be invalid. Message could not be sent!!");
            e.printStackTrace();
        }

        // Attach listener on Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(otpMessage != null){
                    Intent i = new Intent(getApplicationContext(), SendOTPActivity.class);
                    i.putExtra(EXTRA_OTP_MESSAGE, otpMessage);
                    i.putExtra(SendOTPActivity.EXTRA_CONTACTS_DATA, contact);
                    startActivity(i);
                }
                else{
                    // TODO: We can show some error dialog probably
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the OTP Message we generated
        outState.putParcelable(EXTRA_OTP_MESSAGE, otpMessage);
    }
}
