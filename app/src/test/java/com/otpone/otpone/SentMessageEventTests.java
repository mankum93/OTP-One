package com.otpone.otpone;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;
import com.otpone.otpone.util.data_mgt_framework.impl.MultiGenerator;
import com.otpone.otpone.util.data_mgt_framework.impl.OTPMessageGenerator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created by DJ on 6/11/2017.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(android.util.Log.class)
public class SentMessageEventTests {

    private static Logger logger = Logger.getLogger(SentMessageEventTests.class.getSimpleName());

    private SendOTPActivity.SentMessageEvent sentMessageEvent;
    private ArrayList<Contact> dummyContactsData;

    private final Gson gson = new Gson();

    private final String DUMMY_CONTACTS_FILE_NAME = "app/src/main/assets/ContactsTestData.json";

    public final String REGISTERED_SENDER_PHONE_NO = "9717552439";

    private final Semaphore semaphore = new Semaphore(0);

    @Before
    public void setUp() throws Exception {

        // Init the unit to be tested.
        sentMessageEvent = new SendOTPActivity.SentMessageEvent();

        // Load the dummy data.
        Contact[] contacts = gson.fromJson(
                new JsonReader(
                        new BufferedReader(
                                new InputStreamReader(new FileInputStream(DUMMY_CONTACTS_FILE_NAME)))), Contact[].class);
        dummyContactsData = new ArrayList<Contact>(Arrays.asList(contacts));
    }

    @Test
    @Ignore
    public void shouldConsumeTheProducedSentMessageEventsAllTogether(){

        PowerMockito.mockStatic(Log.class);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5, Executors.defaultThreadFactory());
        Random rand = new Random(5);

        // Lets produce some events concurrently

        // Instantiate a message generator
        OTPMessageGenerator otpMessageGenerator = new OTPMessageGenerator(dummyContactsData, REGISTERED_SENDER_PHONE_NO);

        // Randomly generate number of messages to be consumed count.
        for(int i = 0; i < 5; i++){
            SentMessagesProducerTask task = new SentMessagesProducerTask(rand.nextInt(5) + 1, otpMessageGenerator, sentMessageEvent);
            try {
                executor.submit(task).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        // Now, lets try to consume. Lets try with 3 consumers.
        List<SentMessagesConsumerTask> consumers = new ArrayList<>(3);

        for(int i = 0; i < 3; i++){
            consumers.add(new SentMessagesConsumerTask(sentMessageEvent));
        }

        // First, execute tasks directly with executor service.
        for(SentMessagesConsumerTask task : consumers){
            executor.scheduleAtFixedRate(task, 5000, 500, TimeUnit.MILLISECONDS);
        }

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    //@Ignore
    public void shouldConsumeTheProducedSentMessageEventsInASynchronousCoordinatedWay(){

        PowerMockito.mockStatic(Log.class);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5, Executors.defaultThreadFactory());
        Random rand = new Random(5);

        // Lets produce some events concurrently

        // Instantiate a message generator
        OTPMessageGenerator otpMessageGenerator = new OTPMessageGenerator(dummyContactsData, REGISTERED_SENDER_PHONE_NO);

        List<SentMessagesConsumerTask> consumers = new ArrayList<>(3);

        for(int i = 0; i < 3; i++){
            consumers.add(new SentMessagesConsumerTask(sentMessageEvent));
        }

        // Randomly generate number of messages to be consumed count.
        for(SentMessagesConsumerTask task1 : consumers){

            for(int i = 0; i < 5; i++) {
                SentMessagesProducerTask task = new SentMessagesProducerTask(rand.nextInt(5) + 1, otpMessageGenerator, sentMessageEvent);
                try {
                    executor.submit(task).get();

                    executor.schedule(task1, 500, TimeUnit.MILLISECONDS).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class SentMessagesProducerTask implements Runnable{

        private int numberOfMessages;
        private MultiGenerator<Pair<OTPMessage, Contact>> otpMessageMultiGenerator;
        private SendOTPActivity.SentMessageEvent sentMessageEvent;

        public SentMessagesProducerTask(int numberOfMessages, MultiGenerator<Pair<OTPMessage, Contact>> otpMessageMultiGenerator, SendOTPActivity.SentMessageEvent sentMessageEvent) {
            this.numberOfMessages = numberOfMessages;
            this.otpMessageMultiGenerator = otpMessageMultiGenerator;
            this.sentMessageEvent = sentMessageEvent;
        }

        @Override
        public void run() {
            // Schedule production of Messages and Contacts.
            sentMessageEvent.addNewSentMessages(otpMessageMultiGenerator.next(numberOfMessages));
        }

        private Map<OTPMessage, Contact> toContactsAndMessages(List<Pair<OTPMessage, Contact>> contactAndMessagePairs){

            Map<OTPMessage, Contact> map = new LinkedHashMap<>();

            for(Pair<OTPMessage, Contact> pair : contactAndMessagePairs){
                map.put(pair.first, pair.second);
            }

            return map;
        }
    }



    private static class SentMessagesConsumerTask implements Runnable{


        private UUID consumerId;
        private SendOTPActivity.SentMessageEvent sentMessageEvent;

        public SentMessagesConsumerTask(@NonNull SendOTPActivity.SentMessageEvent sentMessageEvent) {
            this.consumerId = UUID.randomUUID();
            this.sentMessageEvent = sentMessageEvent;
        }

        public SentMessagesConsumerTask(UUID consumerId, @NonNull SendOTPActivity.SentMessageEvent sentMessageEvent) {
            this.consumerId = consumerId;
            this.sentMessageEvent = sentMessageEvent;
        }

        @Override
        public void run() {
            List<Pair<OTPMessage, Contact>> vals = sentMessageEvent.getSentMessages(consumerId);
            logger.info("Consumed messages by Consumer: " + consumerId.toString() + "\n" + vals.toString());
        }
    }
}