package com.otpone.otpone.database;

/**
 * Created by DJ on 5/17/2017.
 */

public class ContactSchema {

    public static final class ContactTable{

        public static final String NAME = "contact_info";

        public static final class cols{

            public static final String CONTACT_PHONE_NO = "phoneNo";

            public static final String FIRST_NAME = "firstName";
            public static final String MIDDLE_NAME = "middleName";
            public static final String LAST_NAME = "lastName";

            public static final String CONTACT_EMAIL_ID = "emailId";
        }

        public static final class ContactAddressTable{

            public static final String NAME = "contact_address";

            public static final class cols{
                // Foreign KeyMaker: Phone No
                public static final String FIRST_LINE = "firstLine";
                public static final String SECOND_LINE = "secondLine";
                public static final String CITY = "city";
                public static final String POSTAL_CODE = "postalCode";
            }
        }
    }

    public static final class MessagesRecordsTable {

        public static final String NAME = "messages_records_table";

        public static final class cols{
            public static final String MESSAGE_FROM = "messageFrom";
            public static final String MESSAGE_TO = "messageTo";
            public static final String MESSAGE_BODY = "messageBody";
            public static final String MESSAGE_TIMESTAMP = "messageTimestamp";
        }
    }
}
