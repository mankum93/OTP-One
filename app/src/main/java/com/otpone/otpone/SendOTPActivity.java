package com.otpone.otpone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.otpone.otpone.database.ContactsDbHelper;
import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;
import com.otpone.otpone.model.Repository;
import com.otpone.otpone.util.MapUtil;
import com.otpone.otpone.util.mocks.MockMessageSendingTask;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.MessageResponse;
import com.plivo.helper.exception.PlivoException;

import org.greenrobot.eventbus.EventBus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.otpone.otpone.OTPOneApplication.PLIVO_AUTH_ID;
import static com.otpone.otpone.OTPOneApplication.PLIVO_AUTH_TOKEN;
import static com.otpone.otpone.OTPOneApplication.REGISTERED_RECEIVER_PHONE_NO;

/**
 * Activity to send OTP. The details of the message are displayed.
 */
public class SendOTPActivity extends AppCompatActivity {

    private static final String TAG = SendOTPActivity.class.getSimpleName();

    public static final String EXTRA_CONTACTS_DATA = "EXTRA_CONTACTS_DATA";
    public static final String EXTRA_OTP_MESSAGE = "EXTRA_OTP_MESSAGE";
    private TextView msgContent;
    private Button sendButton;

    private MessageSendingHandlerThread handlerThread;
    private Map<OTPMessage, Contact> messagesAndContacts;

    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        msgContent = (TextView) findViewById(R.id.msg_content);
        sendButton = (Button) findViewById(R.id.send_button);

        // Retrieve the OTP Message extra
        final OTPMessage otpMessage = getIntent().getParcelableExtra(EXTRA_OTP_MESSAGE);
        // Retrieve the Contact extra.
        final Contact contact = getIntent().getParcelableExtra(EXTRA_CONTACTS_DATA);

        // Retrieve the Repository.
        repo = Repository.getRepository();

        // Bind the TextView with the message to be sent
        msgContent.setText(otpMessage.getMsgBody());

        // Setup the Handler Thread to send the message.
        handlerThread = new MessageSendingHandlerThread("Message Send Service");
        handlerThread.start();
        handlerThread.prepareHandler();

        // Set the listener on Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Disable the button while the message is being sent.
                sendButton.setEnabled(false);

                //MessageSendingTask task = new MessageSendingTask(otpMessage);
                MockMessageSendingTask task = new MockMessageSendingTask(otpMessage);
                task.setMessageSendResponseListener(new MessageSendingListener<MessageResponse>() {

                    @Override
                    public void onMessageSent(MessageResponse messageResponse) {
                        // Record the Timestamp of the Message sent.
                        otpMessage.setMessageTimestamp(new Timestamp(System.currentTimeMillis()));

                        // Stash this message in the Db with the list of sent of message
                        ContactsDbHelper.insertMessageToDatabase2(repo.db, otpMessage);

                        // Fill the Event Bus cache with this message with other sent messages.
                        // And then fire the sent message event through the Event Bus.
                        EventBus eventBus = EventBus.getDefault();
                        SentMessageEvent existingEvent = eventBus.getStickyEvent(SentMessageEvent.class);
                        if(existingEvent == null){
                            // Create a new event
                            existingEvent = new SentMessageEvent();
                        }

                        // Fire it.
                        existingEvent.addNewSentMessage(otpMessage, contact);
                        Log.d(TAG, "Firing SentMessageEvent...");
                        eventBus.postSticky(existingEvent);

                        // Update the UI
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showMessageSentToast(getApplicationContext());
                                sendButton.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onMessageSendFailed(MessageResponse messageResponse) {
                        // Update the UI
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showMessageNotSentToast(getApplicationContext());
                                sendButton.setEnabled(true);
                            }
                        });
                    }
                });
                handlerThread.postTask(task);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        messagesAndContacts = null;
        repo = null;
    }

    @Override
    protected void onStop() {
        handlerThread.quit();
        handlerThread = null;

        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Setup the thread again.
        handlerThread = new MessageSendingHandlerThread("Message Send Service");
        handlerThread.start();
        handlerThread.prepareHandler();
    }

    private void showMessageNotSentToast(Context context){
        Toast.makeText(context, "Message could not be sent.", Toast.LENGTH_LONG).show();
    }

    private void showMessageSentToast(Context context){
        Toast.makeText(context, "Message sent successfully.", Toast.LENGTH_LONG).show();
    }

    // Handler Thread Impl. for Sending Messages-----------------------------------------------------------------------------
    public static class MessageSendingHandlerThread extends HandlerThread{

        private Handler handler;

        public MessageSendingHandlerThread(String name) {
            super(name);
        }

        public MessageSendingHandlerThread(String name, int priority) {
            super(name, priority);
        }

        public void prepareHandler(){
            handler = new Handler(getLooper());
        }

        public void postTask(Runnable task){
            if(handler == null){
                handler = new Handler(getLooper());
            }
            handler.post(task);
        }
    }

    // Messaging Sending Task--------------------------------------------------------------------------------------------------

    public static class MessageSendingTask implements Runnable{

        private OTPMessage otpMessage;
        private MessageSendingListener<MessageResponse> messageSendResponseListener;

        public MessageSendingTask(OTPMessage otpMessage) {
            this.otpMessage = otpMessage;
        }

        public MessageSendingListener<MessageResponse> getMessageSendResponseListener() {
            return messageSendResponseListener;
        }

        public void setMessageSendResponseListener(MessageSendingListener<MessageResponse> messageSendResponseListener) {
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

            try {
                // Send the message
                // NOTE: Plivo client instance is not reusable. It mandates using
                // a new instance for each SMS. This is an open issue on their side.
                MessageResponse msgResponse = plivoApi.sendMessage(parameters);

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
            } catch (PlivoException e) {
                Log.e(TAG, e.getLocalizedMessage());
                if(messageSendResponseListener != null){
                    messageSendResponseListener.onMessageSendFailed(null);
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

    public interface MessageSendingListener<Response>{
        void onMessageSent(Response response);
        void onMessageSendFailed(Response response);
    }

    // Message sent Event(for GreenRobot EventBus)--------------------------------------------------------------------------

    /**
     * This is an event for Messages sent. It facilitates providing
     * a UUID token(provided by a event consumer) to maintain a list of messages consumed by that
     * consumer.<p>
     *     The consumer can reset the consumed messages status to non-consumed.
     */
    public static class SentMessageEvent {

        private Map<OTPMessage, Contact> sentMessagesAndContacts = new LinkedHashMap<>();
        private Map<UUID, List<Integer>> messagesConsumptionStatuses = new LinkedHashMap<>();
        private Map<UUID, Integer> currentConsumptionStatus;

        public void addNewSentMessages(Map<OTPMessage, Contact> messagesToBeAdded){
            sentMessagesAndContacts.putAll(messagesToBeAdded);
            updateMessageConsumptionTrackingCounters(messagesToBeAdded);
        }

        public void addNewSentMessages(List<OTPMessage> messages, Contact contact){
            Map<OTPMessage, Contact> messagesToBeAdded = MapUtil.getMap(new LinkedHashMap<>(), (List)messages, contact);
            sentMessagesAndContacts.putAll(messagesToBeAdded);
            updateMessageConsumptionTrackingCounters(messagesToBeAdded);
        }

        private void updateMessageConsumptionTrackingCounters(Map<OTPMessage, Contact> messages){
            int i;
            Set<UUID> currentKeys = messagesConsumptionStatuses.keySet();

            List<Integer> counters;
            // For each Token, update the list with new sent messages count tracker
            for(UUID token : currentKeys){
                counters = messagesConsumptionStatuses.get(token);
                // If concurrent access is allowed to the following block,
                // it is possible that the new counter might be same for
                // both as per the counters' same size accessed at the same time
                // and therefore new counter for the same UUID.
                synchronized (this){
                    // Get the new counter value for these new messages
                    i = counters.get(counters.size() - 1) + 1;
                    // Make a list for this counter(corresponding to the new messages) with "nCopies"
                    counters.addAll(Collections.nCopies(messages.size(), i));
                }
            }
        }

        private void updateMessageConsumptionTrackingCounter(){
            int i;
            Set<UUID> currentKeys = messagesConsumptionStatuses.keySet();

            List<Integer> counters;
            // For each Token, update the list with new sent messages count tracker
            for(UUID token : currentKeys){
                counters = messagesConsumptionStatuses.get(token);
                // If concurrent access is allowed to the following block,
                // it is possible that the new counter might be same for
                // both as per the counters' same size accessed at the same time
                // and therefore new counter for the same UUID.
                synchronized (this){
                    // Get the new counter value for these new messages
                    i = counters.get(counters.size() - 1) + 1;
                    // Make a list for this counter(corresponding to the new messages) with "nCopies"
                    counters.add(i);
                }
            }
        }

        public void addNewSentMessage(OTPMessage message, Contact associatedContact){
            synchronized (this){
                sentMessagesAndContacts.put(message, associatedContact);
            }
            updateMessageConsumptionTrackingCounter();
        }

        /**
         * It is possible that the list may not reflect the true number of
         * all the messages that have been sent. This is likely to happen
         * if messages are being sent a continuous manner.
         *
         * @return An unmodifiable snapshot(list) of the messages that have been sent.
         */
        public final Map<OTPMessage, Contact> getSentMessagesAndContacts() {
            Map<OTPMessage, Contact> messageListToBeSent;
            synchronized (this){
                messageListToBeSent = Collections.unmodifiableMap(sentMessagesAndContacts);
            }
            return messageListToBeSent;
        }

        /**
         * @param token : The token provided by the messages receiver
         * @return Unmodifiable List of messages that have been sent in a "single"
         * message sending event. This single event may provide sent messages
         * of several individual sent message events. It depends on when this method is
         * called.
         */
        public Map<OTPMessage, Contact> getSentMessages(UUID token){
            Map<OTPMessage, Contact> messagesToBeConsumed;
            if(!messagesConsumptionStatuses.containsKey(token)){
                // It is possible that before entering this method, we had 3 messages sent
                // and at this point 3 messages are supposed to be consumed as per the event
                // but the sent messages list gets updated and 3 more are added. Then, in the
                // below block, 6 messages will be prepared as one event. This makes sense
                // because even after receiving the event, the receiver may not choose to call
                // this method right away. Imagine, 100 messages have been sent in the meantime.
                // By our strategy, he or she would be receiving 100 of them!!
                // That's as much as update the receiver is getting as it is possible. If it had been
                // an approach were every single message should be delivered as one message event only.
                // Now, I am not saying that receiving 100 messages is "bad" per se. Because, some other
                // use case may require strict monitoring of every event as it happened.
                // This also means that if 100 messages are sent as single events then there might
                // be 90 messages read at once but there will still be technically 100 events to consume.
                // If these events are all thrown then 98 of them shall return an empty list. Woah!
                // This can only be controlled by the channeler of these events by filtering them.
                synchronized (this){
                    messagesConsumptionStatuses.put(token, new ArrayList<Integer>(Collections.nCopies(sentMessagesAndContacts.size(), 0)));
                    // Also update the current consumption status
                    currentConsumptionStatus = new LinkedHashMap<>();
                    currentConsumptionStatus.put(token, 0);
                    // Initially, return whatever has been sent
                    Map<OTPMessage, Contact> messageListToBeSent;
                    messageListToBeSent = Collections.unmodifiableMap(sentMessagesAndContacts);
                    return messageListToBeSent;
                }
            }
            else{
                messagesToBeConsumed = new LinkedHashMap<>();
                Collection<Integer> consumptionCounters = messagesConsumptionStatuses.get(token);
                // Check the current consumption status and update this status
                // to a newer one. Return all the messages with the newer consumption
                // status
                int consumptionIndex = currentConsumptionStatus.get(token);
                // Search for all messages we have for consumptionIndex + 1

                Set<Map.Entry<OTPMessage, Contact>> sentMessagesAndContactsSet = sentMessagesAndContacts.entrySet();
                Iterator<Map.Entry<OTPMessage, Contact>> iter = sentMessagesAndContactsSet.iterator();
                int i =0;
                for(Integer counter : consumptionCounters){
                    if(counter > consumptionIndex + 1){
                        break;
                    }
                    // Get the message at this index
                    Map.Entry<OTPMessage, Contact> entry = iter.next();

                    if(counter == consumptionIndex + 1){

                        OTPMessage message = entry.getKey();
                        Contact c = entry.getValue();

                        i++;
                        // Add this message to a list of sent messages
                        messagesToBeConsumed.put(message, c);
                    }
                }
                // Update the current consumption index
                currentConsumptionStatus.put(token, consumptionIndex + 1);
            }
            return messagesToBeConsumed;
        }

        public void removeObserver(UUID token){
            if(!messagesConsumptionStatuses.containsKey(token)){
                // Make sure no more messages are added for this observer during
                // removal.
                synchronized (this){
                    messagesConsumptionStatuses.remove(token);
                    currentConsumptionStatus.remove(token);
                }
            }
        }

    }

}
