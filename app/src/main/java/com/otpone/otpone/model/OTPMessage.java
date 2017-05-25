package com.otpone.otpone.model;

/**
 * Created by DJ on 5/16/2017.
 */


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.otpone.otpone.model.util.Generator;
import com.otpone.otpone.model.util.SimpleSixDigitRandomIntGenerator;

import java.sql.Timestamp;

/**
 * This class represents an OTP message to be sent.
 */
public class OTPMessage implements Parcelable, Comparable<OTPMessage> {

    private static final String TAG = OTPMessage.class.getSimpleName();

    /**
     * The default Template text for OTP.
     */
    public static final String OTP_TEMPLATE_TEXT_DEFAULT = "Hi. Your OTP is: ";

    /**
     * This constant indicates an invalid Timestamp value.
     */
    public static final long INVALID_TIME = -1L;

    /**
     * The message body.
     */
    private String msgBody;

    /**
     * The Mobile No. of the Sender of message. Must be 10 digits numeral (without
     * the country code)
     */
    private String from;
    /**
     * The Mobile No. of the Receiver of message. Must be 10 digits numeral (without
     * the country code)
     */
    private String to;

    /**
     * A Template text for OTP.
     */
    private String templateOTP;

    /**
     * A Generator is required for a Random 6 digit
     * number generation for composing the OTP OTPMessage.
     * In case, one is not provided, a default one  is used(without  a Seed) -
     * {@link com.otpone.otpone.model.util.SimpleSixDigitRandomIntGenerator}
     */
    private Generator generator;

    /**
     * The timestamp holds meaning only when the message has been sent
     * to indicate the "timestamp" of it.
     */
    private Timestamp messageTimestamp;


    // GETTERS, SETTERS & CTORS-------------------------------------------------------------------------------------------

    /**
     * A OTPMessage must have a Sender and a Receiver to be valid.
     * @param from : The Mobile No of the Sender.
     * @param to : The Mobile No of the Receiver.
     *
     * @throws InvalidPhoneNoException : If the Mobile No is not a 10 digit numeral.
     */
    public OTPMessage(@NonNull String from, @NonNull String to) throws InvalidPhoneNoException{
        this.from = from;
        this.to = to;
        validateInput();
        templateOTP = OTP_TEMPLATE_TEXT_DEFAULT;
        msgBody = templateOTP + getNextRandomNumber();
    }

    /**
     * @param from : The Mobile No of the Sender.
     * @param to : The Mobile No of the Receiver.
     * @param templateOTP : The template of the OTP message.
     * @throws InvalidPhoneNoException
     */
    public OTPMessage(@NonNull String from, @NonNull String to, String templateOTP) throws InvalidPhoneNoException {
        this(from, to);
        this.templateOTP = templateOTP;
        msgBody = templateOTP + getNextRandomNumber();
    }

    @SuppressWarnings("unchecked")
    private int getNextRandomNumber(){
        if(generator == null){
            generator = new SimpleSixDigitRandomIntGenerator();
        }
        return (int)generator.next(null);
    }

    public String getMsgBody() {
        return msgBody;
    }

    /**
     * You can pass the OTPMessage Body entirely yourself too. It is
     * however recommended to set either the Random Number Generator or
     * use the default one({@link com.otpone.otpone.model.util.SimpleSixDigitRandomIntGenerator})
     * and OTPMessage Body shall be auto-generated.
     *
     * @param msgBody The message body
     */
    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(@NonNull String from) throws InvalidPhoneNoException {
        if(isMobileNo(from)){
            Log.d(TAG, "Validation of Sender Mobile No. successful.");
        }
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(@NonNull String to) throws InvalidPhoneNoException{
        if(isMobileNo(to)){
            Log.d(TAG, "Validation of Receiver Mobile No. successful.");
        }
        this.to = to;
    }

    public String getTemplateOTP() {
        return templateOTP;
    }

    public void setTemplateOTP(String templateOTP) {
        this.templateOTP = templateOTP;
    }

    public Generator getRandomNumberGenerator() {
        return generator;
    }

    public void setRandomNumberGenerator(Generator generator) {
        this.generator = generator;
    }

    public Timestamp getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(Timestamp messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    // INPUT VALIDATION-------------------------------------------------------------------------------------------------

    /**
     * Validate the compulsory input.(Currently the mobile
     * numbers of Sender and Receiver)
     */
    private void validateInput() throws InvalidPhoneNoException{
        if(isMobileNo(this.from) && isMobileNo(this.to)){
            // Validation successful
            Log.d(TAG, "Validation of Input successful.");
        }
    }

    // TODO: Find the specific warning for a non-used return value of a method call.
    // (And No, it is not "unused"). Replace "all" with the correct one.
    @SuppressWarnings("all")
    private boolean isMobileNo(String no) throws InvalidPhoneNoException{
        if(no == null){
            Log.e(TAG, "Validation of Mobile No. failed!!!");
            return false;
        }
        if(no.length() == 10){
            // Try parsing the number as a long
            try{
                Long.parseLong(no);
            }
            catch(NumberFormatException nfe){
                Log.e(TAG, "Validation of Mobile No. failed!!!");
                throw new InvalidPhoneNoException("The number of digits are 10" +
                        "in the Mobile No but it is not properly formatted.", nfe);
            }
            Log.d(TAG, "Validation of Mobile No. successful.");
            return true;
        }
        Log.e(TAG, "Validation of Mobile No. failed!!!");
        return false;
    }


    // COMPARABLE IMPL.--------------------------------------------------------------------------------------------------

    @Override
    public int compareTo(@NonNull OTPMessage o) {
        return o.getMessageTimestamp().compareTo(this.messageTimestamp);
    }

    // ------------------------------------------------------------------------------------------------------------------
    // InvalidPhoneNoException-------------------------------------------------------------------------------------------

    /**
     * Checked Exception in case a Phone No(Phone No as well as Mobile No) is invalid.
     */
    public static class InvalidPhoneNoException extends Exception{

        public static final String STANDARD_MESSAGE = "The Phone No. is Invalid.";

        public InvalidPhoneNoException(String message) {
            super(message);
        }

        public InvalidPhoneNoException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidPhoneNoException(Throwable cause) {
            super(cause);
        }
    }

    // PARCELABLE IMPL.----------------------------------------------------------------------------------------------------

    protected OTPMessage(Parcel in) {
        msgBody = in.readString();
        from = in.readString();
        to = in.readString();
        templateOTP = in.readString();
        messageTimestamp = new Timestamp(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgBody);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(templateOTP);
        dest.writeLong(messageTimestamp == null ? INVALID_TIME : messageTimestamp.getTime());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<OTPMessage> CREATOR = new Parcelable.Creator<OTPMessage>() {
        @Override
        public OTPMessage createFromParcel(Parcel in) {
            return new OTPMessage(in);
        }

        @Override
        public OTPMessage[] newArray(int size) {
            return new OTPMessage[size];
        }
    };
}
