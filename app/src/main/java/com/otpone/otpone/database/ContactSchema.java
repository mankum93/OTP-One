package com.otpone.otpone.database;

/**
 * Created by DJ on 5/17/2017.
 */

public class ContactSchema {

    public static final class ContactTable{

        public static final String NAME = "contact_info";

        public static final class cols{

            public static final String CONTACT_PHONE_NO = "phoneNo";
            public static final String CONTACT_EMAIL_ID = "emailId";
        }

        public static final class ContactNameTable{

            public static final String NAME = "contact_name";

            public static final class cols{
                // Foreign Key: Phone No
                public static final String FIRST_NAME = "firstName";
                public static final String MIDDLE_NAME = "middleName";
                public static final String LAST_NAME = "lastName";
            }
        }

        public static final class ContactAddressTable{

            public static final String NAME = "contact_address";

            public static final class cols{
                // Foreign Key: Phone No
                public static final String FIRST_LINE = "firstLine";
                public static final String SECOND_LINE = "secondLine";
                public static final String CITY = "city";
                public static final String POSTAL_CODE = "postalCode";
            }
        }
    }

    public static final class MessagesRecordTable{

        /**
         * This prefix will be appended to the phone number so that
         * each table represents the record of messages sent for a
         * contact.
         */
        public static final String NAME_PREFIX = "message_record_";

        public static final class cols{
            public static final String MESSAGE_FROM = "messageFrom";
            public static final String MESSAGE_BODY = "messageBody";
            public static final String MESSAGE_TIMESTAMP = "messageTimestamp";
        }
    }
}
