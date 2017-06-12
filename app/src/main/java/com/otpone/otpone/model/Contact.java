package com.otpone.otpone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DJ on 5/16/2017.
 */

/**
 * This class represents the Contact of a Person.
 */
public class Contact implements Parcelable {

    // NOTE: Because only India is supported right now, we have a single constant.
    // If need be, more constants can be appended here to accommodate any future
    // requirement.
    public static final String COUNTRY_CODE_INDIA = "+91";

    /**
     * Name of a Contact/Person
     */
    @SerializedName("name")
    private Name contactName;

    // Phone No can be modeled as a separate class too with attributes like
    // country code or area code, etc. Right now, the implementation is for
    // India only and the country code is fixed, i.e, +91
    /**
     * Phone No of Contact/Person
     */
    @SerializedName("phone_no")
    private String phoneNo;

    /**
     * Email ID of the Contact/Person
     */
    @SerializedName("email")
    private String emailId;

    /**
     * Address of a Contact/Person
     */
    @SerializedName("address")
    private Address address;

    // GETTERS, SETTERS & CTORS----------------------------------------------------------------------------------------

    /**
     * Name and Phone No form a set of compulsory attributes for
     * a contact to be a contact.
     *
     * @param contactName : The name of the contact
     * @param phoneNo : Phone No of the contact
     */
    public Contact(@NonNull Name contactName, @NonNull String phoneNo) {
        this.contactName = contactName;
        this.phoneNo = phoneNo;
        validateInput();
    }

    /**
     * This constructor is for constructing instance through Builder.
     *
     * @param contactName : The name of the contact
     * @param phoneNo : Phone No of the contact
     * @param emailId : Email Address of the contact
     * @param address : Home/Correspondence address of contact
     */
    protected Contact(@NonNull Name contactName, @NonNull String phoneNo, String emailId, Address address) {
        this.contactName = contactName;
        this.phoneNo = phoneNo;
        this.emailId = emailId;
        this.address = address;
        validateInput();
    }

    /**
     * Input validation for compulsory fields only
     */
    private void validateInput(){
        if(this.contactName == null){
            throw new IllegalArgumentException("Contact Name cannot be null.");
        }
        if(phoneNo == null || phoneNo.isEmpty()){
            throw new IllegalArgumentException("Phone No cannot be null or empty.");
        }
    }

    public Name getName() {
        return contactName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        if(phoneNo == null || phoneNo.isEmpty()){
            throw new IllegalArgumentException("Phone No cannot be null or empty.");
        }
        this.phoneNo = phoneNo;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    //PARCELABLE IMPL.-------------------------------------------------------------------------------------------------

    protected Contact(Parcel in) {
        contactName = (Name) in.readValue(Name.class.getClassLoader());
        phoneNo = in.readString();
        emailId = in.readString();
        address = (Address) in.readValue(Address.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(contactName);
        dest.writeString(phoneNo);
        dest.writeString(emailId);
        dest.writeValue(address);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    // BUILDER PATTERN-------------------------------------------------------------------------------------------------

    public static class Builder {
        private Contact.Name contactName;
        private String phoneNo;
        private String emailId;
        private Address address;

        private Builder(Name contactName, String phoneNo) {
            this.contactName = contactName;
            this.phoneNo = phoneNo;
        }

        public static Builder newBuilder(@NonNull Name contactName, @NonNull String phoneNo){
            return new Builder(contactName, phoneNo);
        }

        public Builder setEmailId(String emailId) {
            this.emailId = emailId;
            return this;
        }

        public Builder setAddress(Address address) {
            this.address = address;
            return this;
        }

        public Contact createContact() {
            return new Contact(contactName, phoneNo, emailId, address);
        }
    }


    // UTILITY METHODS-------------------------------------------------------------------------------------------------

    public static String toFormattedPhoneNo(String phoneNo){
        return COUNTRY_CODE_INDIA + phoneNo;
    }

    public static String toFormattedAndStrippedPhoneNo(String phoneNo){
        return COUNTRY_CODE_INDIA.substring(1) + phoneNo;
    }


    // NAME CLASS------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * A class to represent the Name of a person/contact. A person's full name
     * is made of implements Parcelable {First Name, Middle Name, Last Name}
     */
    public static class Name implements Parcelable {

        /**
         * First Name of a Contact/Person
         */
        @SerializedName("first_name")
        private String firstName;
        /**
         * Middle Name of a Contact/Person
         */
        @SerializedName("middle_name")
        private String middleName;
        /**
         * Last Name of a Contact/Person
         */
        @SerializedName("last_name")
        private String lastName;

        /**
         * @return The {First Name Last Name} representation. Ex: Alex Buckley, Matt Trant, etc.
         */
        @Override
        public String toString() {
            return firstName + " " + lastName;
        }

        // GETTERS, SETTERS & CTORS--------------------------------------------------------------------------------

        /**
         * A valid name has to have a valid First Name.
         * @param firstName : The First Name of a Contact/Person
         */
        public Name(@NonNull String firstName) {
            this.firstName = firstName;
            validateInput();
        }

        public Name(@NonNull String firstName, String middleName, String lastName) {
            this(firstName, lastName);
            this.middleName = middleName;
        }

        public Name(@NonNull String firstName, String lastName) {
            this(firstName);
            this.lastName = lastName;
        }

        private void validateInput(){
            if(this.firstName == null || this.firstName.isEmpty()){
                throw new IllegalArgumentException("First Name of a person cannot be null or empty.");
            }
        }

        public String getFirstName() {
            return firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }


        // PARCELABLE IMPL.-----------------------------------------------------------------------------------------------

        protected Name(Parcel in ) {
            firstName = in .readString();
            middleName = in .readString();
            lastName = in .readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(firstName);
            dest.writeString(middleName);
            dest.writeString(lastName);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Name> CREATOR = new Parcelable.Creator<Name> () {
            @Override
            public Name createFromParcel(Parcel in ) {
                return new Name(in);
            }

            @Override
            public Name[] newArray(int size) {
                return new Name[size];
            }
        };

    }


    // ADDRESS CLASS---------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * This class represent the Address of a Contact/Person. The Address is constituted by
     * {First line, Second line, City, Postal code}
     */
    public static class Address implements Parcelable {

        /**
         * First line of the Contact/Person's address.
         */
        @SerializedName("first_line")
        private String firstLine;
        /**
         * Second line of the Contact/Person's address.
         */
        @SerializedName("second_line")
        private String secondLine;

        // TODO: To validate the city name, we require a list of all cities in India.
        // Retrieve that list and make constants out of it.
        /**
         * The city of the Contact/Person
         */
        @SerializedName("city")
        private String city;

        /**
         * The postal code/ pin code of the Contact/Person
         */
        @SerializedName("postal_code")
        private int postalCode;

        /**
         * @return Returns the Address in the format of,<p>
         * First Line,<p>
         * Second Line,<p>
         * City, Pin Code.
         */
        @Override
        public String toString() {
            // NOTE: Even if we don't use a StringBuilder, modern compiler will
            // perform optimizations on concatenation(using a StringBuilder or
            // a StringBuffer). This is the reason, Intellij might recommend
            // converting from a manual StringBuilder to simply String. I have used a
            // StringBuilder because of more readability of resulting code.
            return new StringBuilder(firstLine).append(", ")
                    .append("\n")
                    .append(secondLine == null || secondLine.isEmpty() ? "" : secondLine + ", " + "\n")
                    .append(city).append(postalCode == 0 ? "" : ", ")
                    .append(postalCode).append(".")
                    .toString();
        }

        // GETTERS, SETTERS & CTORS--------------------------------------------------------------------------------

        /**
         * An address must have a first line and city.
         *
         * @param firstLine : First line of Address
         * @param city : City part of An Address
         */
        public Address(@NonNull String firstLine, @NonNull String city) {
            this.firstLine = firstLine;
            this.city = city;
            validateInput();
        }

        /**
         * This constructor is for constructing instance through Builder.
         *
         * @param firstLine : First line of Address
         * @param secondLine : Second line of Address
         * @param city : City part of An Address
         * @param postalCode : The postal code/ pin code of the Contact/Person
         */
        protected Address(String firstLine, String secondLine, String city, int postalCode) {
            this.firstLine = firstLine;
            this.city = city;
            validateInput();
            this.secondLine = secondLine;
            this.postalCode = postalCode;
        }

        private void validateInput(){
            if(this.firstLine == null || this.firstLine.isEmpty()){
                throw new IllegalArgumentException("An Address must have a non-empty first line.");
            }
            if(city == null || city.isEmpty()){
                throw new IllegalArgumentException("A City cannot be null or empty for an Address.");
            }
        }

        public String getFirstLine() {
            return firstLine;
        }

        public void setFirstLine(String firstLine) {
            if(this.firstLine == null || this.firstLine.isEmpty()){
                throw new IllegalArgumentException("An Address must have a non-empty first line.");
            }
            this.firstLine = firstLine;
        }

        public String getSecondLine() {
            return secondLine;
        }

        public void setSecondLine(String secondLine) {
            this.secondLine = secondLine;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            if(city == null || city.isEmpty()){
                throw new IllegalArgumentException("A City cannot be null or empty for an Address.");
            }
            this.city = city;
        }

        public int getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(int postalCode) {
            this.postalCode = postalCode;
        }

        // ADDRESS BUILDER-------------------------------------------------------------------------------------------

        /**
         * Builder Pattern for an Address.
         */
        public static class Builder {

            private String firstLine;
            private String secondLine;
            private String city;
            private int postalCode;

            private Builder(String firstLine, String city) {
                this.firstLine = firstLine;
                this.city = city;
            }

            public static Builder newBuilder(@NonNull String firstLine, @NonNull String city){
                return new Builder(firstLine, city);
            }

            public Builder setSecondLine(String secondLine) {
                this.secondLine = secondLine;
                return this;
            }

            public Builder setPostalCode(int postalCode) {
                this.postalCode = postalCode;
                return this;
            }

            public Address createAddress() {
                return new Address(firstLine, secondLine, city, postalCode);
            }
        }

        // PARCELABLE IMPL.----------------------------------------------------------------------------------------------

        protected Address(Parcel in) {
            firstLine = in.readString();
            secondLine = in.readString();
            city = in.readString();
            postalCode = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(firstLine);
            dest.writeString(secondLine);
            dest.writeString(city);
            dest.writeInt(postalCode);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
            @Override
            public Address createFromParcel(Parcel in) {
                return new Address(in);
            }

            @Override
            public Address[] newArray(int size) {
                return new Address[size];
            }
        };
    }
}
