/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.text.TextUtils;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.list.ContactEntryListFragment;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.list.ContactPickerFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.common.list.DirectoryListLoader;
import com.android.contacts.list.EmailAddressPickerFragment;
import com.android.contacts.list.JoinContactListFragment;
import com.android.contacts.list.LegacyPhoneNumberPickerFragment;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.list.OnEmailAddressPickerActionListener;
import com.android.contacts.list.UiIntentActions;
import com.android.contacts.common.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.list.OnPostalAddressPickerActionListener;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.list.PostalAddressPickerFragment;
import com.google.common.collect.Sets;
import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.util.Log;

import java.util.Set;

// add by y.haiyang for i99 (start)
import java.util.List;
import java.util.ArrayList;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.CursorLoader;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.view.inputmethod.InputMethodManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.widget.PopupWindow;

import com.cappu.contacts.I99Configure;
import com.cappu.contacts.I99Font;
import com.cappu.contacts.SurnamesListAdapter;
import com.cappu.contacts.SurnamesGridAdapter;
import com.cappu.contacts.SurnamesCallBack;
import com.cappu.contacts.SurnamesCallBack;
import com.cappu.contacts.util.FilterNodes;
import com.cappu.contacts.util.I99Utils;
// add by y.haiyang for i99 (end)
/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting one.
 */
public class ContactSelectionActivity extends ContactsActivity
        implements View.OnCreateContextMenuListener, OnQueryTextListener, OnClickListener,
                OnCloseListener, OnFocusChangeListener {
    private static final String TAG = "ContactSelectionActivity";

    private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;

    private static final String KEY_ACTION_CODE = "actionCode";
    private static final String KEY_SEARCH_MODE = "searchMode";
    private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

    private ContactsIntentResolver mIntentResolver;
    protected ContactEntryListFragment<?> mListFragment;

    private int mActionCode = -1;
    private boolean mIsSearchMode;
    private boolean mIsSearchSupported;

    private ContactsRequest mRequest;
    private SearchView mSearchView;
    private View mSearchViewContainer;


    /** M: New Feature */
    private String mFromWhereActivity = "";
    // add by y.haiyang for i99 (start)
    private static final boolean I99 = I99Configure.USED_I99;
    private static final String EMPTY = "";
    private String mSearchString = EMPTY;
    private EditText mI99Search;
    private Button mSurNamesBu;
    private PopupWindow mSurNamePop;
    private TextView mSurNameFilter;
    private ImageButton mSurNameButton;
    private ListView mSurNameList;
    private GridView mSurNameGrid;
    private View mSurNameTitle;
    private View mSearchContent;
    private TopBar mTopBar;
    I99ClickListener mI99ClickListener;

    ArrayList<FilterNodes> mSurNameFilters = new ArrayList<FilterNodes>();
    // add by y.haiyang for i99 (end)

    public ContactSelectionActivity() {
        mIntentResolver = new ContactsIntentResolver(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactEntryListFragment<?>) {
            mListFragment = (ContactEntryListFragment<?>) fragment;
            setupActionListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            return;
        }

        if (savedState != null) {
            mActionCode = savedState.getInt(KEY_ACTION_CODE);
            mIsSearchMode = savedState.getBoolean(KEY_SEARCH_MODE);
        }

        // Extract relevant information from the intent
        mRequest = mIntentResolver.resolveIntent(getIntent());
        if (!mRequest.isValid()) {
            Log.w(TAG, "[onCreate] mRequest is Invalid,finish activity...mRequest:"
                    + mRequest);
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        configureActivityTitle();

        // add by y.haiyang for i99 (start)
        if(I99){
            setContentView(R.layout.care_contact_picker);
            getActionBar().hide();
            mI99ClickListener = new I99ClickListener();

            mI99Search = (EditText)findViewById(R.id.i99_search);
            mI99Search.addTextChangedListener(mI99TextWatcher);

            mSurNamesBu = (Button)findViewById(R.id.i99_surnames);
            mSurNamesBu.setOnClickListener(mI99ClickListener);

            mSearchContent = findViewById(R.id.search_content);
            mTopBar = (TopBar)findViewById(R.id.topbar);
            mTopBar.setText(R.string.contactPickerActivityTitle);
            mTopBar.setRightVisibilty(View.GONE);
            mTopBar.setLeftVisibilty(View.GONE);
        }else{
            setContentView(R.layout.contact_picker);
        }
        // add by y.haiyang for i99 (end)

        if (mActionCode != mRequest.getActionCode()) {
            mActionCode = mRequest.getActionCode();
            configureListFragment();
        }

        prepareSearchViewAndActionBar();
    }

    private void prepareSearchViewAndActionBar() {
        final ActionBar actionBar = getActionBar();
        mSearchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
                .inflate(R.layout.custom_action_bar, null);
        mSearchView = (SearchView) mSearchViewContainer.findViewById(R.id.search_view);

        // Postal address pickers (and legacy pickers) don't support search, so just show
        // "HomeAsUp" button and title.
        if (mRequest.getActionCode() == ContactsRequest.ACTION_PICK_POSTAL ||
                mRequest.isLegacyCompatibilityMode()) {
            mSearchView.setVisibility(View.GONE);
            if (actionBar != null) {
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
            }
            mIsSearchSupported = false;
            configureSearchMode();
            return;
        }

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // In order to make the SearchView look like "shown via search menu", we need to
        // manually setup its state. See also DialtactsActivity.java and ActionBarAdapter.java.
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setQueryHint(getString(R.string.hint_findContacts));
        mSearchView.setIconified(false);
        mSearchView.setFocusable(true);

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnQueryTextFocusChangeListener(this);

        actionBar.setCustomView(mSearchViewContainer,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        actionBar.setDisplayShowCustomEnabled(true);

        mIsSearchSupported = true;
        configureSearchMode();
    }

    private void configureSearchMode() {
        final ActionBar actionBar = getActionBar();
        if (mIsSearchMode) {
            actionBar.setDisplayShowTitleEnabled(false);
            mSearchViewContainer.setVisibility(View.VISIBLE);
            mSearchView.requestFocus();
        } else {
            actionBar.setDisplayShowTitleEnabled(true);
            mSearchViewContainer.setVisibility(View.GONE);
            mSearchView.setQuery(null, true);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Go back to previous screen, intending "cancel"
                setResult(RESULT_CANCELED);
                /// M: Bug fix ALPS02013610. Need add isResumed() judgement.
                if (isResumed()) {
                    onBackPressed();
                }
                return true;
            case R.id.menu_search:
                mIsSearchMode = !mIsSearchMode;
                configureSearchMode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ACTION_CODE, mActionCode);
        outState.putBoolean(KEY_SEARCH_MODE, mIsSearchMode);
    }

    private void configureActivityTitle() {
        if (!TextUtils.isEmpty(mRequest.getActivityTitle())) {
            setTitle(mRequest.getActivityTitle());
            Log.w(TAG,
                    "[configureActivityTitle] mRequest.getActivityTile != null,return.mRequest:"
                            + mRequest);
            return;
        }

        int actionCode = mRequest.getActionCode();
        Log.d(TAG, "[configureActivityTitle] actionCode:" + actionCode);
        switch (actionCode) {
            case ContactsRequest.ACTION_INSERT_OR_EDIT_CONTACT: {
                setTitle(R.string.contactInsertOrEditActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_CONTACT: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_OR_CREATE_CONTACT: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CONTACT: {
                setTitle(R.string.shortcutActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_PHONE: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_EMAIL: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CALL: {
                setTitle(R.string.callShortcutActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_SMS: {
                setTitle(R.string.messageShortcutActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_POSTAL: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_JOIN: {
                setTitle(R.string.titleJoinContactDataWith);
                break;
            }
        }
    }

    /**
     * Creates the fragment based on the current request.
     */
    public void configureListFragment() {
        Log.d(TAG, "[configureListFragment]mActionCode is" + mActionCode);
        switch (mActionCode) {
            case ContactsRequest.ACTION_INSERT_OR_EDIT_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setEditMode(true);
                fragment.setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_NONE);
                fragment.setCreateContactEnabled(!mRequest.isSearchMode());
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_DEFAULT:
            case ContactsRequest.ACTION_PICK_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setIncludeProfile(mRequest.shouldIncludeProfile());
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_OR_CREATE_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setCreateContactEnabled(!mRequest.isSearchMode());
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setShortcutRequested(true);
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_PHONE: {//dengying@20160601
            	  Log.e("dengyingContact", "ContactSelectionActivity.java configureListFragment ACTION_PICK_PHONE");
            	  
                PhoneNumberPickerFragment fragment = getPhoneNumberPickerFragment(mRequest);
                //CallableUri's default value is false
                //If it set to true, query uri will be Callable.CONTENT_URI
                boolean isCallableUri = getIntent().getBooleanExtra("isCallableUri", false);
                fragment.setUseCallableUri(isCallableUri);
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_EMAIL: {
                mListFragment = new EmailAddressPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CALL: {
                PhoneNumberPickerFragment fragment = getPhoneNumberPickerFragment(mRequest);
                fragment.setShortcutAction(Intent.ACTION_CALL);

                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_SMS: {
                PhoneNumberPickerFragment fragment = getPhoneNumberPickerFragment(mRequest);
                fragment.setShortcutAction(Intent.ACTION_SENDTO);

                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_POSTAL: {
                PostalAddressPickerFragment fragment = new PostalAddressPickerFragment();

                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_JOIN: {
                JoinContactListFragment joinFragment = new JoinContactListFragment();
                joinFragment.setTargetContactId(getTargetContactId());
                mListFragment = joinFragment;
                break;
            }

            default:
                throw new IllegalStateException("Invalid action code: " + mActionCode);
        }

        /** M: */
        ActivitiesUtils.setPickerFragmentAccountType(this, mListFragment);


        // Setting compatibility is no longer needed for PhoneNumberPickerFragment since that logic
        // has been separated into LegacyPhoneNumberPickerFragment.  But we still need to set
        // compatibility for other fragments.
        mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    private PhoneNumberPickerFragment getPhoneNumberPickerFragment(ContactsRequest request) {
        if (mRequest.isLegacyCompatibilityMode()) {
            return new LegacyPhoneNumberPickerFragment();
        } else {
            return new PhoneNumberPickerFragment();
        }
    }

    public void setupActionListener() {
        if (mListFragment instanceof ContactPickerFragment) {
            ((ContactPickerFragment) mListFragment).setOnContactPickerActionListener(
                    new ContactPickerActionListener());
        } else if (mListFragment instanceof PhoneNumberPickerFragment) {
            ((PhoneNumberPickerFragment) mListFragment).setOnPhoneNumberPickerActionListener(
                    new PhoneNumberPickerActionListener());
        } else if (mListFragment instanceof PostalAddressPickerFragment) {
            ((PostalAddressPickerFragment) mListFragment).setOnPostalAddressPickerActionListener(
                    new PostalAddressPickerActionListener());
        } else if (mListFragment instanceof EmailAddressPickerFragment) {
            ((EmailAddressPickerFragment) mListFragment).setOnEmailAddressPickerActionListener(
                    new EmailAddressPickerActionListener());
        } else if (mListFragment instanceof JoinContactListFragment) {
            ((JoinContactListFragment) mListFragment).setOnContactPickerActionListener(
                    new JoinContactActionListener());
        } else {
            throw new IllegalStateException("Unsupported list fragment type: " + mListFragment);
        }
    }

    private final class ContactPickerActionListener implements OnContactPickerActionListener {
        @Override
        public void onCreateNewContactAction() {
            startCreateNewContactActivity();
        }

        @Override
        public void onEditContactAction(Uri contactLookupUri) {
            Bundle extras = getIntent().getExtras();
            if (launchAddToContactDialog(extras)) {
                // Show a confirmation dialog to add the value(s) to the existing contact.
                Intent intent = new Intent(ContactSelectionActivity.this,
                        ConfirmAddDetailActivity.class);
                intent.setData(contactLookupUri);
                if (extras != null) {
                    // First remove name key if present because the dialog does not support name
                    // editing. This is fine because the user wants to add information to an
                    // existing contact, who should already have a name and we wouldn't want to
                    // override the name.
                    extras.remove(Insert.NAME);
                    intent.putExtras(extras);
                }

                // Wait for the activity result because we want to keep the picker open (in case the
                // user cancels adding the info to a contact and wants to pick someone else).
                startActivityForResult(intent, SUBACTIVITY_ADD_TO_EXISTING_CONTACT);
            } else {
                // Otherwise launch the full contact editor.
                /// M: it should add isEditingUserProfile flag
                startActivityAndForwardResult(EditorIntents.createEditContactIntent(
                        contactLookupUri, /* materialPalette =*/ null, /* photoId =*/ -1,
                        /* nameId =*/ -1, /* isEditingUserProfile =*/ false));
            }
        }

        @Override
        public void onPickContactAction(Uri contactUri) {
            returnPickerResult(contactUri);
        }

        @Override
        public void onShortcutIntentCreated(Intent intent) {
            returnPickerResult(intent);
        }

        /**
         * Returns true if is a single email or single phone number provided in the {@link Intent}
         * extras bundle so that a pop-up confirmation dialog can be used to add the data to
         * a contact. Otherwise return false if there are other intent extras that require launching
         * the full contact editor. Ignore extras with the key {@link Insert.NAME} because names
         * are a special case and we typically don't want to replace the name of an existing
         * contact.
         */
        private boolean launchAddToContactDialog(Bundle extras) {
            if (extras == null) {
                Log.w(TAG, "[launchAddToContactDialog] extras is null");
                return false;
            }

            // Copy extras because the set may be modified in the next step
            Set<String> intentExtraKeys = Sets.newHashSet();
            intentExtraKeys.addAll(extras.keySet());

            // Ignore name key because this is an existing contact.
            if (intentExtraKeys.contains(Insert.NAME)) {
                intentExtraKeys.remove(Insert.NAME);
            }

            int numIntentExtraKeys = intentExtraKeys.size();
            if (numIntentExtraKeys == 2) {
                boolean hasPhone = intentExtraKeys.contains(Insert.PHONE) &&
                        intentExtraKeys.contains(Insert.PHONE_TYPE);
                boolean hasEmail = intentExtraKeys.contains(Insert.EMAIL) &&
                        intentExtraKeys.contains(Insert.EMAIL_TYPE);
                return hasPhone || hasEmail;
            } else if (numIntentExtraKeys == 1) {
                return intentExtraKeys.contains(Insert.PHONE) ||
                        intentExtraKeys.contains(Insert.EMAIL);
            }
            // Having 0 or more than 2 intent extra keys means that we should launch
            // the full contact editor to properly handle the intent extras.
            return false;
        }
    }

    private final class PhoneNumberPickerActionListener implements
            OnPhoneNumberPickerActionListener {
        @Override
        public void onPickPhoneNumberAction(Uri dataUri) {
            returnPickerResult(dataUri);
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber) {
            Log.w(TAG, "Unsupported call.");
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber, boolean isVideoCall) {
            Log.w(TAG, "Unsupported call.");
        }

        @Override
        public void onShortcutIntentCreated(Intent intent) {
            returnPickerResult(intent);
        }

        public void onHomeInActionBarSelected() {
            ContactSelectionActivity.this.onBackPressed();
        }
    }

    private final class JoinContactActionListener implements OnContactPickerActionListener {
        @Override
        public void onPickContactAction(Uri contactUri) {
            Intent intent = new Intent(null, contactUri);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onShortcutIntentCreated(Intent intent) {
        }

        @Override
        public void onCreateNewContactAction() {
        }

        @Override
        public void onEditContactAction(Uri contactLookupUri) {
        }
    }

    private final class PostalAddressPickerActionListener implements
            OnPostalAddressPickerActionListener {
        @Override
        public void onPickPostalAddressAction(Uri dataUri) {
            returnPickerResult(dataUri);
        }
    }

    private final class EmailAddressPickerActionListener implements
            OnEmailAddressPickerActionListener {
        @Override
        public void onPickEmailAddressAction(Uri dataUri) {
            returnPickerResult(dataUri);
        }
    }

    public void startActivityAndForwardResult(final Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        /** M: New Feature @{ */
        intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
        /** @} */
        // Forward extras to the new activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "startActivity() failed: " + e);
            Toast.makeText(ContactSelectionActivity.this, R.string.missing_app,
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mListFragment.setQueryString(newText, true);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()) {
            case R.id.search_view: {
                if (hasFocus) {
                    showInputMethod(mSearchView.findFocus());
                }
            }
        }
    }

    public void returnPickerResult(Uri data) {
        Intent intent = new Intent();
        intent.setData(data);
        returnPickerResult(intent);
    }

    public void returnPickerResult(Intent intent) {
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_action_button: {
                startCreateNewContactActivity();
                break;
            }
        }
    }

    private long getTargetContactId() {
        Intent intent = getIntent();
        final long targetContactId = intent.getLongExtra(
                UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, -1);
        if (targetContactId == -1) {
            Log.e(TAG, "Intent " + intent.getAction() + " is missing required extra: "
                    + UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY);
            setResult(RESULT_CANCELED);
            finish();
            return -1;
        }
        return targetContactId;
    }

    private void startCreateNewContactActivity() {
    	
    	Log.d("dengyingContact", "startCreateNewContactActivity");
    	
        Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
        /// M: Add account type for handling special case for add new contactor
        intent.putExtra(ContactsSettingsUtils.ACCOUNT_TYPE,
                        getIntent().getIntExtra(ContactsSettingsUtils.ACCOUNT_TYPE,
                                                ContactsSettingsUtils.ALL_TYPE_ACCOUNT));
        startActivityAndForwardResult(intent);
    }

    private void showInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(view, 0)) {
                Log.w(TAG, "Failed to show soft input method.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "[onActivityResult] requestCode:" + requestCode + ",resultCode:"
                + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBACTIVITY_ADD_TO_EXISTING_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    ImplicitIntentsUtil.startActivityInAppIfPossible(this, data);
                }
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        /// M:Fix ALPS01777704,dismiss searchItem when mSearchView set gone
        // change for ALPS02364621, mSearchView would not been inited at onCreate if permission
        // check fail. @{
        if (mSearchView != null) {
            searchItem.setVisible(!mIsSearchMode && mIsSearchSupported
                    && mSearchView.getVisibility() != View.GONE);
            Log.d(TAG, "searchMode:" + mIsSearchMode + ",mSearchView Visib:"
                            + mSearchView.getVisibility());
        } else {
            Log.d(TAG, "mSearchView has not been inited ");
        }
        /// @}
        return true;
    }

    @Override
    public void onBackPressed() {
	    // add by y.haiyang for i99 (start)
	    if(mSurNamePop != null && mSurNamePop.isShowing()){
            mSearchString=EMPTY;
            showSurList();
            mSurNamePop.dismiss();
            return;
        }
		
        final String search = mI99Search.getText().toString().trim();
        if(!TextUtils.isEmpty(search)){
            mI99Search.setText(EMPTY);
            return;
        }
	    // add by y.haiyang for i99 (end)
		
        if (mIsSearchMode) {
            mIsSearchMode = false;
            configureSearchMode();
        } else {
            super.onBackPressed();
        }
		
		
    }
    // add by y.haiyang for i99 (start)
    public class I99ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.i99_surnames:
                    mSurNamePop = createSurNamePop();
                    int width = mSurNamesBu.getWidth() + 100;
                    mSurNamePop.setWidth(width);
                    if(mSurNamePop.isShowing()){
                        mSearchString=EMPTY;
                        showSurList();
                        mSurNamePop.dismiss();
                    }else{
                        mSurNamePop.showAsDropDown(mSearchContent, I99Utils.getScreenSize(ContactSelectionActivity.this)[0] - width, 0);
                        hideKeyBoard();
                    }
                break;

                case R.id.surname_delete:
                    SurnamesGridAdapter adapter = (SurnamesGridAdapter)mSurNameGrid.getAdapter();
                    if(!adapter.setBackData()){
                        showSurList();
                    }
                    if(!TextUtils.isEmpty(mSearchString)){
                        mSearchString = mSearchString.substring(0 ,mSearchString.length() -1);
                        updateFilter();
                    }
                break;
            }
        }
    }

    TextWatcher mI99TextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
             mListFragment.setQueryString(s.toString(), true);
        }
    };

    @Override
    protected void onResume() {
        if(I99){
            createSurNamePop();
            getLoaderManager().initLoader(0,null,mSurNameFilterCallBack);

            IntentFilter filter = new IntentFilter();
            filter.addAction("com.cappu.action.KEYBOARD_HIDE_SHOW");
            registerReceiver(mI99Receiver, filter);
        }
        super.onResume();
    }
    /*@Override
    public void onBackPressed() {
        if(mSurNamePop != null && mSurNamePop.isShowing()){
            mSearchString=EMPTY;
            showSurList();
            mSurNamePop.dismiss();
            return;
        }
        final String search = mI99Search.getText().toString().trim();
        if(!TextUtils.isEmpty(search)){
            mI99Search.setText(EMPTY);
            return;
        }
        super.onBackPressed();
    }*/
    public void showSurList(){
        mSurNameList.setVisibility(View.VISIBLE);
        mSurNameGrid.setVisibility(View.GONE);
        mSurNameTitle.setVisibility(View.GONE);
        mSurNameGrid.setAdapter(null);
    }

    public void updateFilter(){
        mSurNameFilter.setText(EMPTY);
        mSurNameFilter.setText(mSearchString);
        mI99Search.setText(EMPTY);
        mI99Search.setText(mSearchString);
    }

    private PopupWindow createSurNamePop(){
        if(mSurNamePop != null ){
            return mSurNamePop;
        }
        View view = getLayoutInflater().inflate(R.layout.i99_pop_surname_layout, null);
        mSurNameTitle= view.findViewById(R.id.surname_title);
        mSurNameFilter = (TextView)view.findViewById(R.id.surname_filter);
        mSurNameList = (ListView)view.findViewById(R.id.surname_list);
        mSurNameGrid = (GridView)view.findViewById(R.id.surname_grid);
        mSurNameButton = (ImageButton)view.findViewById(R.id.surname_delete);
        mSurNameButton.setOnClickListener(mI99ClickListener);
        mSurNamePop = new PopupWindow(view, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mSurNamePop.setAnimationStyle(R.style.I99SurnameAnim);
        mSurNamePop.setOutsideTouchable(false);
        return mSurNamePop;
    }

    private LoaderCallbacks<Cursor> mSurNameFilterCallBack = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
            String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                    + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + Contacts.DISPLAY_NAME + " != '' ))";
            String[] projection = { ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY };

            return new CursorLoader(ContactSelectionActivity.this, Contacts.CONTENT_URI,
                    projection, select, null, "sort_key COLLATE LOCALIZED asc");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            if (cursor == null) {
                Log.i(TAG, "cursor is null ");
                return;
            }
            mSurNameFilters.clear();
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                FilterNodes filter = null;
                String name = cursor.getString(1).replaceAll(" ", "");
                String sortKey = cursor.getString(2);
                String letter = sortKey.substring(0, 1);
                String _letter = null;

                if (mSurNameFilters.size() != 0) {
                    filter = mSurNameFilters.get(mSurNameFilters.size() - 1);
                    _letter = filter.getData();
                }

                if (!letter.equals(_letter)) {
                    FilterNodes root = new FilterNodes();
                    root.setData(letter);
                    mSurNameFilters.add(root);
                    filter = root;
                }

                for (int j = 0; j < name.length(); j++) {
                    String dataString = String.valueOf(name.charAt(j));
                    filter = addChild(filter, dataString);
                }

                cursor.moveToNext();
            }
            SurnamesListAdapter mSurListAdapter = new SurnamesListAdapter(ContactSelectionActivity.this,mSurNameFilters);
            mSurNameList.setAdapter(mSurListAdapter);
            mSurListAdapter.setCallBack(mSurListCallBack);

        }

        /**
         * Add Child for This Tree
         *
         * @param parent
         * @param data
         * @return
         */
        public FilterNodes addChild(FilterNodes parent, String data) {
            List<FilterNodes> childs = parent.getChildren();
            // _child  this is old boy
            // child   this is new boy
            FilterNodes child = new FilterNodes();

            child.setData(data);
            if (childs == null || childs.size() == 0) {
                parent.addChildren(child);
            } else {
                FilterNodes _child = childs.get(childs.size() - 1);
                String _data = _child.getData();
                if (data.equals(_data)) {
                    return _child;
                } else {
                    parent.addChildren(child);
                }
            }
            return child;

        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            // TODO Auto-generated method stub

        }

        private void debug() {
            for (int i = 0; i < mSurNameFilters.size(); i++) {
                printTree(mSurNameFilters.get(i));
            }
        }

        private void printTree(FilterNodes tree) {
            List<FilterNodes> childs = tree.getChildren();
            if (childs == null) {
                return;
            }
            for (int i = 0; i < childs.size(); i++) {
                printTree(childs.get(i));
            }
        }

    };

    SurnamesCallBack mSurListCallBack = new SurnamesCallBack(){
        @Override
        public void showSurGrid(FilterNodes filter ,boolean add){
            mSurNameList.setVisibility(View.GONE);
            mSurNameGrid.setVisibility(View.VISIBLE);
            mSurNameTitle.setVisibility(View.VISIBLE);
            SurnamesGridAdapter mGridAdapter = new SurnamesGridAdapter(ContactSelectionActivity.this, filter.getChildren());
            mSurNameGrid.setAdapter(mGridAdapter);
            mGridAdapter.setCallBack(mSurListCallBack);
            if(add){
                mSearchString=I99Utils.plusString(mSearchString,filter.getData());
                mSurNameFilter.setText(EMPTY);
                mSurNameFilter.setText(mSearchString);
                mI99Search.setText(EMPTY);
                mI99Search.setText(mSearchString);
            }
        }

        @Override
        public void updateFilter(FilterNodes filter){
            mSearchString=I99Utils.plusString(mSearchString,filter.getData());
            ContactSelectionActivity.this.updateFilter();
        }

    };

    private BroadcastReceiver mI99Receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent data) {
            // TODO Auto-generated method stub
            final boolean state = data.getBooleanExtra("KEYBOARD_STATE", false);
            if(state == true){
                if(mSurNamePop != null && mSurNamePop.isShowing()){
                    mSearchString=EMPTY;
                    showSurList();
                    mSurNamePop.dismiss();
                }
            }
        }
    };

    private void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ContactSelectionActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    // add by y.haiyang for i99 (end)
}
