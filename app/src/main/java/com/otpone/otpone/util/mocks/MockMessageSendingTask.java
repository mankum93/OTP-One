package com.otpone.otpone.util.mocks;

import android.util.Log;

import com.otpone.otpone.SendOTPActivity;
import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.MessageResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import static com.otpone.otpone.OTPOneApplication.PLIVO_AUTH_ID;
import static com.otpone.otpone.OTPOneApplication.PLIVO_AUTH_TOKEN;
import static com.otpone.otpone.OTPOneApplication.REGISTERED_RECEIVER_PHONE_NO;

public class MockMessageSendingTask implements Runnable{

    private static final String TAG = MockMessageSendingTask.class.getSimpleName();

        private OTPMessage otpMessage;
        private SendOTPActivity.MessageSendingListener<MessageResponse> messageSendResponseListener;

        public MockMessageSendingTask(OTPMessage otpMessage) {
            this.otpMessage = otpMessage;
        }

        public SendOTPActivity.MessageSendingListener<MessageResponse> getMessageSendResponseListener() {
            return messageSendResponseListener;
        }

        public void setMessageSendResponseListener(SendOTPActivity.MessageSendingListener<MessageResponse> messageSendResponseListener) {
            this.messageSendResponseListener = messageSendResponseListener;
        }

        public void removeMessageSendResponseListener() {
            this.messageSendResponseListener = null;
        }

        @Override
        public void run() {
            // Send OTP OTPMessage using Plivo

            // Initialize client
            // Plivo client
            RestAPI plivoApi = new RestAPI(PLIVO_AUTH_ID, PLIVO_AUTH_TOKEN, "v1");

            LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
            parameters.put("src", Contact.toFormattedAndStrippedPhoneNo(otpMessage.getFrom())); // Sender's phone number with country code
            parameters.put("dst", Contact.toFormattedAndStrippedPhoneNo(REGISTERED_RECEIVER_PHONE_NO)); // Receiver's phone number with country code
            parameters.put("text", otpMessage.getMsgBody()); // Your SMS text message

            // parameters.put("url", "http://example.com/report/"); // The URL to which with the status of the message is sent
            parameters.put("method", "GET"); // The method used to call the url

            // Send the message
            // NOTE: Plivo client instance is not reusable. It mandates using
            // a new instance for each SMS. This is an open issue on their side.
            //MessageResponse msgResponse = plivoApi.sendMessage(parameters);

            // Mocked server response assuming a successful delivery.
            MessageResponse msgResponse = new MessageResponse();
            msgResponse.serverCode = 202;
            msgResponse.messageUuids = new ArrayList<>();
            msgResponse.messageUuids.add(UUID.randomUUID().toString());
            msgResponse.message = "Message successfully sent.";

            // Print the response
            Log.d(TAG, msgResponse.toString());
            // Print the Api ID
            Log.d(TAG, "Api ID : " + msgResponse.apiId);
            // Print the Response Message
            Log.d(TAG, "Message : " + msgResponse.message);

            if (msgResponse.serverCode == 202) {
                // Print the Message UUID
                Log.d(TAG, "Message UUID : " + msgResponse.messageUuids.get(0));
                if(messageSendResponseListener != null){
                    messageSendResponseListener.onMessageSent(msgResponse);
                }
            } else {
                Log.e(TAG, msgResponse.error);
                if(messageSendResponseListener != null){
                    messageSendResponseListener.onMessageSendFailed(msgResponse);
                }
            }

            // Old Twilio Message(Ignore)
                /*Message message = Message.creator(new PhoneNumber(Contact.toFormattedPhoneNo(otpMessage.getFrom())),
                        new PhoneNumber(Contact.toFormattedPhoneNo(otpMessage.getTo())),
                        otpMessage.getMsgBody())
                        .create();*/

            // Enable the button again.
        }
    }