package com.cappu.launcherwin.phoneutils;


import android.net.Uri;

import android.provider.BaseColumns;

 

public interface SMS extends BaseColumns

{

    public static final Uri CONTENT_URI = Uri.parse("content://sms");

    public static final String FILTER  = "!imichat";

   

    public static final String TYPE = "type";

    public static final String THREAD_ID = "thread_id";

    public static final String ADDRESS = "address";

    public static final String PERSON_ID = "person";

    public static final String DATE = "date";

    public static final String READ = "read";

    public static final String BODY = "body";

    public static final String PROTOCOL = "protocol";

 

    public static final int MESSAGE_TYPE_ALL    = 0;

    public static final int MESSAGE_TYPE_INBOX  = 1;

    public static final int MESSAGE_TYPE_SENT   = 2;

    public static final int MESSAGE_TYPE_DRAFT  = 3;

    public static final int MESSAGE_TYPE_OUTBOX = 4;

    public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages

    public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later

   

    public static final int PROTOCOL_SMS = 0;//SMS_PROTO

    public static final int PROTOCOL_MMS = 1;//MMS_PROTO

}