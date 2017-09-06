/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.contacts.list;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.list.ContactsRequest;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.DropMenu.DropDownMenu;
import com.mediatek.contacts.util.Log;

// add by y.haiyang for i99 (start)
import java.util.List;
import java.util.ArrayList;
import android.widget.ImageButton;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import com.cappu.widget.MarqueeTextView;


import com.mediatek.contacts.list.AbstractPickerFragment.OnI99SelectedListener;
// add by y.haiyang for i99 (end)
/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting multiple contacts.
 */

public class ContactListMultiChoiceActivity extends ContactsActivity implements
        View.OnCreateContextMenuListener, OnQueryTextListener, OnClickListener, OnCloseListener,
        OnFocusChangeListener {
    private static final String TAG = "ContactListMultiChoiceActivity";

    private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;
    public static final int CONTACTGROUPLISTACTIVITY_RESULT_CODE = 1;

    private static final String KEY_ACTION_CODE = "actionCode";
    private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

    public static final String RESTRICT_LIST = "restrictlist";

    private ContactsIntentResolverEx mIntentResolverEx;
    protected AbstractPickerFragment mListFragment;

    private int mActionCode = -1;

    private ContactsRequest mRequest;
    private SearchView mSearchView;

    // the dropdown menu with "Select all" and "Deselect all"
    private DropDownMenu mSelectionMenu;
    private boolean mIsSelectedAll = true;
    private boolean mIsSelectedNone = true;
    // if Search Mode now, decide the menu display or not.
    private boolean mIsSearchMode = false;

    // for CT NEW FEATURE
    private int mNumberBalance = 100;

    // add by y.haiyang for i99 (start)
    private static final boolean I99 = true;
    private static final String EMPTY = "";
    private String mSearchString = EMPTY;
    private View mSearchContent;
    private EditText mI99Search;
    private Button mSurNamesBu;
    private Dialog mOptionDialog;
    private PopupWindow mSurNamePop;
    private TextView mSurNameFilter;
    private ImageButton mSurNameButton;
    private ListView mSurNameList;
    private GridView mSurNameGrid;
    private View mSurNameTitle;
    I99ClickListener mI99ClickListener;

    private TopBar mTopBar;
    private String soss_choose_contacts = null;//yuan tong qin add 

    ArrayList<FilterNodes> mSurNameFilters = new ArrayList<FilterNodes>();
    // add by y.haiyang for i99 (end)

    private enum SelectionMode {
        SearchMode, ListMode
    };

    public ContactListMultiChoiceActivity() {
        Log.i(TAG, "[ContactListMultiChoiceActivity]new.");
        mIntentResolverEx = new ContactsIntentResolverEx(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof AbstractPickerFragment) {
            mListFragment = (AbstractPickerFragment) fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.i(TAG,"[onCreate]startPermissionActivity,return.");
            return;
        }
        // for ct new feature
        Intent mmsIntent = this.getIntent();
        Log.i(TAG, "[onCreate]...");
        if (mmsIntent != null) {
            mNumberBalance = mmsIntent.getIntExtra("NUMBER_BALANCE", 100);
            Log.i(TAG, "[onCreate]mNumberBalance from intent = " + mNumberBalance);

        }
        soss_choose_contacts = mmsIntent.getStringExtra("soss_choose_contacts");//yuan tong qin add
       
        if (savedState != null) {
            mActionCode = savedState.getInt(KEY_ACTION_CODE);
            mNumberBalance = savedState.getInt("NUMBER_BALANCE");
            Log.i(TAG, "[onCreate]mNumberBalance from savedState = " + mNumberBalance);

        }

        // Extract relevant information from the intent
        mRequest = mIntentResolverEx.resolveIntent(getIntent());
        if (!mRequest.isValid()) {
            Log.w(TAG, "[onCreate]Request is invalid!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // add by y.haiyang for i99 (start)
        if(I99){
            setContentView(R.layout.care_contact_picker);
            getActionBar().hide();

            mI99ClickListener = new I99ClickListener();
            mSearchContent = findViewById(R.id.search_content);
            mI99Search = (EditText)findViewById(R.id.i99_search);
            mSurNamesBu = (Button)findViewById(R.id.i99_surnames);
            mSurNamesBu.setOnClickListener(mI99ClickListener);

            mI99Search.addTextChangedListener(mI99TextWatcher);

            mTopBar = (TopBar)findViewById(R.id.topbar);
            mTopBar.setOnTopBarListener(mTopBarListener);

        }else{
            setContentView(R.layout.contact_picker);
        }
        // add by y.haiyang for i99 (end)
        configureListFragment();

        // Disable Search View in listview
        if (mSearchView != null) {
            mSearchView.setVisibility(View.GONE);
        }

        showActionBar(SelectionMode.ListMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[onDestroy]");
        if(mI99Receiver!=null){
        	unregisterReceiver(mI99Receiver);//yuan tong qin add 
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "[onSaveInstanceState]mActionCode = " + mActionCode + ",mNumberBalance = "
                + mNumberBalance);
        outState.putInt(KEY_ACTION_CODE, mActionCode);
        // for ct new feature
        outState.putInt("NUMBER_BALANCE", mNumberBalance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add by y.haiyang for i99 (start)
        if(I99){
            return true;
        }
        // add by y.haiyang for i99 (end)
        super.onCreateOptionsMenu(menu);

       /* MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mtk_list_multichoice, menu);

        MenuItem optionItem = menu.findItem(R.id.search_menu_item);
        optionItem.setTitle(R.string.menu_search);*/

        return true;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "[onClick]v= " + v);
        final int resId = v.getId();
        switch (resId) {
        case R.id.search_menu_item:
            Log.i(TAG, "[onClick]resId = search_menu_item ");
            mListFragment.updateSelectedItemsView();
            showActionBar(SelectionMode.SearchMode);
            closeOptionsMenu();
            break;

        case R.id.menu_option:
            Log.i(TAG, "[onClick]resId = menu_option ");
            if (mListFragment instanceof MultiDuplicationPickerFragment) {
                Log.d(TAG, "[onClick]Send result for copy action");
                setResult(ContactImportExportActivity.RESULT_CODE);
            }
            if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
                PhoneAndEmailsPickerFragment fragment =
                        (PhoneAndEmailsPickerFragment) mListFragment;
                fragment.setNumberBalance(mNumberBalance);
                fragment.onOptionAction();
            } else {
                mListFragment.onOptionAction();
            }
            break;

        case R.id.select_items:
            Log.i(TAG, "[onClick]resId = select_items ");
            // if the Window of this Activity hasn't been created,
            // don't show Popup. because there is no any window to attach .
            if (getWindow() == null) {
                Log.w(TAG, "[onClick]current Activity dinsow is null");
                return;
            }
            if (mSelectionMenu == null || !mSelectionMenu.isShown()) {
                View parent = (View) v.getParent();
                mSelectionMenu = updateSelectionMenu(parent);
                mSelectionMenu.show();
            } else {
                Log.w(TAG, "[onClick]mSelectionMenu is already showing, ignore this click");
            }
            break;

        default:
            break;
        }
        return;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // add by y.haiyang for i99 (start)
        if(I99){
            return true;
        }
        // add by y.haiyang for i99 (end)
        int itemId = item.getItemId();
        Log.i(TAG, "[onMenuItemSelected]itemId = " + itemId);
        // if click the search menu, into the SearchMode and disable the search
        // menu
        if (itemId == R.id.search_menu_item) {
            mListFragment.updateSelectedItemsView();
            mIsSelectedNone = mListFragment.isSelectedNone();
            showActionBar(SelectionMode.SearchMode);
            item.setVisible(false);
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Creates the fragment based on the current request.
     */
    private void configureListFragment() {
        if (mActionCode == mRequest.getActionCode()) {
            Log.w(TAG, "[configureListFragment]return ,mActionCode = " + mActionCode);
            return;
        }

        Bundle bundle = new Bundle();
        mActionCode = mRequest.getActionCode();
        Log.i(TAG, "[configureListFragment] action code is " + mActionCode);

        switch (mActionCode) {
        case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS://Mms,Rcse,Email
            mListFragment = new MultiBasePickerFragment();
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_VCARD_PICKER://Email
            mListFragment = new MultiVCardPickerFragment();
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:
            mListFragment = new MultiDuplicationPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_EMAILS:
            mListFragment = new EmailsPickerFragment();
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONES:
            mListFragment = new PhoneNumbersPickerFragment();
            //M:Op01 Rcs get intent data for filter @{
            ExtensionManager.getInstance().getRcsExtension().
                    getIntentData(getIntent(), mListFragment);
            /** @} */
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_DATAS:
            mListFragment = new DataItemsPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            break;

        case ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS:
                // add by y.haiyang for i99 (start)
                mTopBar.setText(R.string.menu_delete_contact);
                // add by y.haiyang for i99 (end)
            mListFragment = new MultiDeletionPickerFragment();
            break;
			
            //yuan tong qin add 
            case ContactsRequestAction.ACTION_SOSS_CHOOSE_CONTACTS :
            	mListFragment = new PhoneAndEmailsPickerFragment();
//            	mListFragment.setNumberBalan
            	break;
            //yuan tong qin add end

        case ContactsRequestAction.ACTION_GROUP_MOVE_MULTIPLE_CONTACTS:
            mListFragment = new MultiGroupPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS:
            mListFragment = new PhoneAndEmailsPickerFragment();
            break;

        case ContactsRequestAction.ACTION_SHARE_MULTIPLE_CONTACTS:
            mListFragment = new MultiSharePickerFragment();
            break;

        case ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS:
            mListFragment = new MultiGroupAddPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONE_IMS_SIP_CALLS:
            mListFragment = new ConferenceCallsPickerFragment();
            bundle.putParcelable(ConferenceCallsPickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            break;

        default:
            throw new IllegalStateException("Invalid action code: " + mActionCode);
        }
		//wangcunxi add begin care_os
        mListFragment.setI99SelectedListener(onSelectedListener);// add for i99
		//wangcunxi add end
        mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        mListFragment.setQueryString(mRequest.getQueryString(), false);
        mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);
        mListFragment.setVisibleScrollbarEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    public void startActivityAndForwardResult(final Intent intent) {
        Log.i(TAG, "[startActivityAndForwardResult]intent = " + intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        // Forward extras to the new activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mListFragment.startSearch(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onClose() {
        if (mSearchView == null) {
            return false;
        }
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        showActionBar(SelectionMode.ListMode);
        mListFragment.updateSelectedItemsView();
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.search_view) {
            if (hasFocus) {
                showInputMethod(mSearchView.findFocus());
            }
        }
    }

    private void showInputMethod(View view) {
        final InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(view, 0)) {
                Log.w(TAG, "Failed to show soft input method.");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "[onActivityResult]requestCode = " + requestCode + ",resultCode = "
                + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBACTIVITY_ADD_TO_EXISTING_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    startActivity(data);
                }
                finish();
            }
        }

        if (resultCode == ContactImportExportActivity.RESULT_CODE) {
            finish();
        }

        if (resultCode == CONTACTGROUPLISTACTIVITY_RESULT_CODE) {
            long[] ids = data.getLongArrayExtra("checkedids");
            if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
                PhoneAndEmailsPickerFragment fragment =
                        (PhoneAndEmailsPickerFragment) mListFragment;
                fragment.markItemsAsSelectedForCheckedGroups(ids);
            }
            // M:OP01 RCS will mark item for selected group in phone numbers
            // list@{
            ExtensionManager.getInstance().getRcsExtension().getGroupListResult(mListFragment, ids);
            /** @} */
        }

    }

    public void onBackPressed() {
        Log.i(TAG, "[onBackPressed]");
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
        if (mSearchView != null && !mSearchView.isFocused()) {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            showActionBar(SelectionMode.ListMode);
            mListFragment.updateSelectedItemsView();
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "[onConfigurationChanged]" + newConfig);
        super.onConfigurationChanged(newConfig);
        // do nothing
    }

    private void showActionBar(SelectionMode mode) {
        Log.d(TAG, "[showActionBar]mode = " + mode);
        ActionBar actionBar = getActionBar();
        switch (mode) {
        case SearchMode:
            mIsSearchMode = true;
            invalidateOptionsMenu();
            final View searchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.mtk_multichoice_custom_action_bar, null);
            // in SearchMode,disable the doneMenu and selectView.
            Button selectView = (Button) searchViewContainer.findViewById(R.id.select_items);
            selectView.setVisibility(View.GONE);

            mSearchView = (SearchView) searchViewContainer.findViewById(R.id.search_view);
            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setQueryHint(getString(R.string.hint_findContacts));
            mSearchView.setIconified(false);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setOnQueryTextFocusChangeListener(this);

            // when no Query String,do not display the "X"
            mSearchView.onActionViewExpanded();

            actionBar.setCustomView(searchViewContainer, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            // display the "OK" button.
            Button optionView = (Button) searchViewContainer.findViewById(R.id.menu_option);
            optionView.setTypeface(Typeface.DEFAULT_BOLD);
            if (mIsSelectedNone) {
                // if there is no item selected, the "OK" button is disable.
                optionView.setEnabled(false);
                optionView.setTextColor(Color.LTGRAY);
            } else {
                optionView.setEnabled(true);
                optionView.setTextColor(Color.WHITE);
            }
            optionView.setOnClickListener(this);
            break;

        case ListMode:
            mIsSearchMode = false;
            invalidateOptionsMenu();
            // Inflate a custom action bar that contains the "done" button for
            // multi-choice
            LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.mtk_multichoice_custom_action_bar,
                    null);
            // in the listMode,disable the SearchView
            mSearchView = (SearchView) customActionBarView.findViewById(R.id.search_view);
            mSearchView.setVisibility(View.GONE);

            // set dropDown menu on selectItems.
            Button selectItems = (Button) customActionBarView.findViewById(R.id.select_items);
            selectItems.setOnClickListener(this);

            Button menuOption = (Button) customActionBarView.findViewById(R.id.menu_option);
            menuOption.setTypeface(Typeface.DEFAULT_BOLD);
            String optionText = menuOption.getText().toString();
            menuOption.setOnClickListener(this);

            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
            // in onBackPressed() used. If mSearchView is null,return prePage.
            mSearchView = null;
            break;

        default:
            break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "[onOptionsItemSelected]");
        // add by y.haiyang for i99 (start)
        if(I99){
            return true;
        }
        // add by y.haiyang for i99 (end)
        if (item.getItemId() == android.R.id.home) {
            hideSoftKeyboard(mSearchView);
            // Fix CR:ALPS01945610
            if (isResumed()) {
                onBackPressed();
            }
            return true;
        }
        if (item.getItemId() == R.id.groups) {
            startActivityForResult(new Intent(ContactListMultiChoiceActivity.this,
                    ContactGroupListActivity.class), CONTACTGROUPLISTACTIVITY_RESULT_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * add dropDown menu on the selectItems.The menu is "Select all" or
     * "Deselect all"
     *
     * @param customActionBarView
     * @return The updated DropDownMenu
     */
    private DropDownMenu updateSelectionMenu(View customActionBarView) {
        DropMenu dropMenu = new DropMenu(this);
        // new and add a menu.
        DropDownMenu selectionMenu = dropMenu.addDropDownMenu(
                (Button) customActionBarView.findViewById(R.id.select_items), R.menu.mtk_selection);

        Button selectView = (Button) customActionBarView.findViewById(R.id.select_items);
        // when click the selectView button, display the dropDown menu.
        selectView.setOnClickListener(this);
        MenuItem item = selectionMenu.findItem(R.id.action_select_all);

        // get mIsSelectedAll from fragment.
        mListFragment.updateSelectedItemsView();
        mIsSelectedAll = mListFragment.isSelectedAll();
        // if select all items, the menu is "Deselect all"; else the menu is
        // "Select all".
        if (mIsSelectedAll) {
            // dropDown menu title is "Deselect all".
            item.setTitle(R.string.menu_select_none);
            // click the menu, deselect all items
            dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    showActionBar(SelectionMode.ListMode);
                    // clear select all items
                    mListFragment.onClearSelect();
                    return false;
                }
            });
        } else {
            // dropDown Menu title is "Select all"
            item.setTitle(R.string.menu_select_all);
            // click the menu, select all items.
            dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    showActionBar(SelectionMode.ListMode);
                    // select all of itmes
                    mListFragment.onSelectAll();
                    return false;
                }
            });
        }
        return selectionMenu;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // add by y.haiyang for i99 (start)
        if(I99){
            return true;
        }
        // add by y.haiyang for i99 (end)

        ExtensionManager.getInstance().getOp01Extension().
                addGroupMenu(this, menu, mListFragment);

  /*
        MenuItem menuItem = menu.findItem(R.id.search_menu_item);
        if (mIsSearchMode) {
            // if SearchMode, search Menu is disable.
            menuItem.setVisible(false);
            return false;
        } else {
            // if ListMode, search Menu is display.
            menuItem.setVisible(true);
            if (mListFragment instanceof MultiPhoneAndEmailsPickerFragment) {
                MenuItem groupsItem = menu.findItem(R.id.groups);
                groupsItem.setVisible(true);
            }
            //M:OP01 RCS will add menu item in list@{
            ExtensionManager.getInstance().getRcsExtension().
                    addListMenuOptions(this, menu, menuItem, mListFragment);
            return super.onPrepareOptionsMenu(menu);
        }

        */
        //M:OP01 RCS will add menu item in list@{
        ExtensionManager.getInstance().getRcsExtension().
                addListMenuOptions(this, menu, null, mListFragment);
        /** @} */
        return true;
    }

    private void hideSoftKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // add by y.haiyang for i99 (start)
    public class I99ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.i99_surnames:
                    mSurNamePop = createSurNamePop();
                    final int width = mSurNamesBu.getWidth() + 100;
                    final int topbarH = getResources().getDimensionPixelSize(R.dimen.care_topbar_height);
                    final int featureH = getResources().getDimensionPixelSize(R.dimen.care_feature_height);
                    final int screenH = I99Utils.getScreenSize(ContactListMultiChoiceActivity.this)[1];
                    final int statusBarH = I99Utils.getStatusBarH(ContactListMultiChoiceActivity.this);
                    final int height =screenH - statusBarH - topbarH - featureH;
                    mSurNamePop.setWidth(width);
                    mSurNamePop.setHeight(height);
                    if(mSurNamePop.isShowing()){
                        mSearchString=EMPTY;
                        showSurList();
                        mSurNamePop.dismiss();
                    }else{
                        mSurNamePop.showAsDropDown(mSearchContent,I99Utils.getScreenSize(ContactListMultiChoiceActivity.this)[0] - width, 0);
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
            mListFragment.startSearch(s.toString());
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
    //private class SurNameFilterCallBack implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
            String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                    + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + Contacts.DISPLAY_NAME + " != '' ))";
            String[] projection = { ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY };

            return new CursorLoader(ContactListMultiChoiceActivity.this, Contacts.CONTENT_URI,
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
            SurnamesListAdapter mSurListAdapter = new SurnamesListAdapter(ContactListMultiChoiceActivity.this,mSurNameFilters);
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
            SurnamesGridAdapter mGridAdapter = new SurnamesGridAdapter(ContactListMultiChoiceActivity.this, filter.getChildren());
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
            ContactListMultiChoiceActivity.this.updateFilter();
        }

    };

    private void showOptionDialog(){
        mListFragment.updateSelectedItemsView();
        mIsSelectedAll = mListFragment.isSelectedAll();

        mOptionDialog = new Dialog(this, R.style.I99DialogStyle);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.i99_multi_choice_option_dialog, null);
        TextView title = (TextView)view.findViewById(R.id.title);
        Button select = (Button)view.findViewById(R.id.select);
        select.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mIsSelectedAll){
                    mListFragment.onClearSelect();
                }else{
                    mListFragment.onSelectAll();
                }

                if(mOptionDialog != null && mOptionDialog.isShowing()){
                    mOptionDialog.dismiss();
                    mOptionDialog = null ;
                }
            }
        });

        if(mIsSelectedAll){
           select.setText(R.string.menu_select_none);
        }else{
           select.setText(R.string.menu_select_all);
        }

        title.setTextSize(I99Font.TITLE);
        select.setTextSize(I99Font.TITLE);
        android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = I99Utils.getScreenSize(this)[0];
        mOptionDialog.addContentView(view, lp);
        mOptionDialog.show();
        Window window = mOptionDialog.getWindow();
        lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.CarePopupAnimation);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        showOptionDialog();
        return false;
    }

    private OnI99SelectedListener onSelectedListener = new OnI99SelectedListener(){
        @Override
        public void onSelectedChanged(int count){
            TextView sub = mTopBar.getSubTitle();
            if(sub != null && count == 0){
                sub.setVisibility(View.GONE);
            } else if(count != 0){
                mTopBar.setSubText(getString(R.string.care_selected_item_count, count));
                sub = mTopBar.getSubTitle();
                sub.setVisibility(View.VISIBLE);
            }
        }
    };
    private String format(int resid , int count){
        return getResources().getQuantityString(resid,count,count).toString();
    }

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
        imm.hideSoftInputFromWindow(ContactListMultiChoiceActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private onTopBarListener mTopBarListener = new onTopBarListener(){

        public void onLeftClick(View v){
            setResult(RESULT_CANCELED);
            ContactListMultiChoiceActivity.this.finish();
        }
        public void onRightClick(View v){

            mListFragment.updateSelectedItemsView();
            mIsSelectedNone = mListFragment.isSelectedNone();

            if(mIsSelectedNone){
                Toast.makeText(v.getContext(), R.string.multichoice_no_select_alert,
                                 Toast.LENGTH_SHORT).show();
                return;
            }

            /*if (mListFragment instanceof ContactsDuplicationFragment) {//dengying del
                Log.d(TAG, "Send result for copy action");
                setResult(ContactImportExportActivity.RESULT_CODE);
            }*/
            if(soss_choose_contacts != null && soss_choose_contacts.equals("soss")){
            	 if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
                     PhoneAndEmailsPickerFragment fragment = (PhoneAndEmailsPickerFragment) mListFragment;
                     fragment.setNumberBalance(mNumberBalance);
                  }
            }
            mListFragment.onOptionAction();
        }
        public void onTitleClick(View v){

        }
    };
    // add by y.haiyang for i99 (end)

}
