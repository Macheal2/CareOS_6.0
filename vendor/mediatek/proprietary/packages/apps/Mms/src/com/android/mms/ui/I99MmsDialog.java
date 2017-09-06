package com.android.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.mms.R;

import com.cappu.app.CareDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.util.Log;
import com.android.mms.ui.I99DialogListener;
import com.android.mms.ui.MessageItem;
import android.text.TextUtils;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.util.Linkify;
import com.android.mms.ui.MessageUtils;
import android.net.Uri;
import android.text.style.URLSpan;
import java.util.HashSet;
import com.android.mms.ui.ConversationList;
import android.content.Intent;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.util.DisplayMetrics;
import android.content.ContentUris;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import com.android.mms.data.Contact;

public class I99MmsDialog extends CareDialog implements OnItemClickListener{

    private Context mContext;
    private ListView mListView;
    private List<Map<String, String>> mMenuList;
    private I99DialogListener mI99DialogListener;
    private MessageItem mMessageItem;
    private Intent mIntent;
    private String mNumber;
    private String[] mMenuItems; 	
    View mContentView;
	
    public I99MmsDialog(Context context,int theme, I99DialogListener i99DialogListener, MessageItem messageItem) {
	    super(context, theme);
	    // TODO Auto-generated constructor stub
	    this.mContext = context;
	    this.mI99DialogListener = i99DialogListener;
	    this.mMessageItem = messageItem;
	    this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
	    super.onCreate(savedInstanceState);
	    LayoutInflater inflater = getLayoutInflater();
	    mContentView = inflater.inflate(R.layout.i99_style_dialog, null);

	    this.setContentView(mContentView);

	    mMenuItems = new String[]{
	        mContext.getString(R.string.ipmsg_copy),
	        mContext.getString(R.string.menu_forward),
	        mContext.getString(R.string.ipmsg_delete),
	        mContext.getString(R.string.copy_to_sdcard),
	        mContext.getString(R.string.view_message_details),
	        i99AddContact(mMessageItem)
        };	

	    mListView = (ListView) mContentView.findViewById(R.id.i99_style_list_items);
		
	    mMenuList = new ArrayList<Map<String, String>>();
	    setListItems();
    }	
	
    

    public void setListItems(){			
	    for (int i = 0; i < mMenuItems.length; i++) {
	        Map<String, String> menuMap = new HashMap<String, String>();
	        menuMap.put("menuItem", mMenuItems[i]);	    
	        mMenuList.add(menuMap);
	    }
	    if(null == mMenuItems[mMenuItems.length - 1]){
		    mMenuList.remove(mMenuItems.length - 1);
	    }
			
	    SimpleAdapter adapter = new SimpleAdapter(mContext,
	          mMenuList, R.layout.i99_theme_dialog_menu_list,
	         new String[] { "menuItem" },
	         new int[] { R.id.i99_dialog_menu_list_item});
	    mListView.setAdapter(adapter);
	    mListView.setOnItemClickListener(this);
    }	

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
		long arg3) {
	    // TODO Auto-generated method stub
	    mI99DialogListener.getI99DialogListItemId(position, mMessageItem, mNumber);	
            this.dismiss();
    }	

    public String i99AddContact(MessageItem msgItem){
	    String addContactString = null;

	    if (TextUtils.isEmpty(msgItem.mBody)) {
                return null;
            }
            SpannableString msg = new SpannableString(msgItem.mBody);
            Linkify.addLinks(msg, Linkify.ALL);
            ArrayList<String> uris =
                MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
            /// M: Code analyze 022, Add bookmark. Clear the List.@{
            //mURLs.clear();
            /// @}
            // Remove any dupes so they don't get added to the menu multiple times
            HashSet<String> collapsedUris = new HashSet<String>();
            for (String uri : uris) {
                collapsedUris.add(uri.toLowerCase());
            }
            for (String uriString : collapsedUris) {
                String prefix = null;
                int sep = uriString.indexOf(":");
                if (sep >= 0) {
                    prefix = uriString.substring(0, sep);
                    /// M: Code analyze 022, Add bookmark. @{
                    if ("mailto".equalsIgnoreCase(prefix) || "tel".equalsIgnoreCase(prefix)) {
                        uriString = uriString.substring(sep + 1);
                    }
                    /// @}
                }
                Uri contactUri = null;
                boolean knownPrefix = true;
                if ("mailto".equalsIgnoreCase(prefix))  {
                    contactUri = getContactUriForEmail(uriString);
                } else if ("tel".equalsIgnoreCase(prefix)) {
                    contactUri = getContactUriForPhoneNumber(uriString);
                } else {
                    knownPrefix = false;
                    //mURLs.add(uriString);
                }
                if (knownPrefix && contactUri == null) {
                    mIntent = ConversationList.createAddContactIntent(uriString);
                    addContactString = mContext.getString(R.string.menu_add_address_to_contacts, uriString);
	            mNumber = uriString;
	        }
                //}
	    }
	    return addContactString;
     }

    private Uri getContactUriForEmail(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
            Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
        new String[] { Email.CONTACT_ID, Contacts.DISPLAY_NAME }, null, null, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(1);
                        if (!TextUtils.isEmpty(name)) {
                            return ContentUris.withAppendedId(Contacts.CONTENT_URI, cursor.getLong(0));
                        }
                    }
                } finally {
                cursor.close();
            }
        }
        return null;
    }

     private Uri getContactUriForPhoneNumber(String phoneNumber) {
          Contact contact = Contact.get(phoneNumber, true);
          if (contact.existsInDatabase()) {
              return contact.getUri();
          }
          return null;   
     }
     
    
}
