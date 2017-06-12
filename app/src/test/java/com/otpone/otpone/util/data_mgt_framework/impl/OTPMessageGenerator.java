package com.otpone.otpone.util.data_mgt_framework.impl;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by DJ on 6/11/2017.
 */

public class OTPMessageGenerator implements MultiGenerator<Pair<OTPMessage, Contact>> {

    int position;
    List<Contact> contacts;
    String from;

    public OTPMessageGenerator(@NonNull List<Contact> contacts, @NonNull String from) {

        if(contacts == null || contacts.isEmpty()){
            throw new IllegalStateException("Contacts can't be null or empty.");
        }

        if(from == null || from.isEmpty()){
            throw new IllegalStateException("Sender phone no can't be null or empty");
        }

        this.contacts = contacts;
        this.from = from;
    }

    @Override
    public Pair<OTPMessage, Contact> next() {

        Pair<OTPMessage, Contact> pair;
        OTPMessage msg;
        Contact c;
        try {
            msg = new OTPMessage(from, (c = contacts.get(position % contacts.size())).getPhoneNo(), null);
            pair = new Pair<>(msg, c);
        } catch (OTPMessage.InvalidPhoneNoException e) {
            e.printStackTrace();
            // Rethrow it.
            throw new RuntimeException(e);
        }
        position++;
        return pair;
    }

    @Override
    public List<Pair<OTPMessage, Contact>> next(int count) {

        List<Pair<OTPMessage, Contact>> messagesToBeReturned = new ArrayList<>(count);

        Pair<OTPMessage, Contact> pair;
        OTPMessage msg;
        Contact c;

        int i = position;
        int j = position + count;

        // Get count no of elements form current position.
        while(i < j){

            try {
                msg = new OTPMessage(from, (c = contacts.get(position % contacts.size())).getPhoneNo(), null);
                pair = new Pair<>(msg, c);
                messagesToBeReturned.add(pair);
            } catch (OTPMessage.InvalidPhoneNoException e) {
                e.printStackTrace();
                // Rethrow it.
                throw new RuntimeException(e);
            }

            i++;
            position++;
        }

        return messagesToBeReturned;
    }
}
