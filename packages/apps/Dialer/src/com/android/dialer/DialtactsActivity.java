/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.dialer;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.provider.CallLog.Calls;
import android.speech.RecognizerIntent;
import android.support.v4.view.ViewPager;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.contacts.common.activity.TransactionSafeActivity;
import com.android.contacts.common.dialog.ClearFrequentsDialog;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.interactions.TouchPointManager;
import com.android.contacts.common.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.contacts.commonbind.analytics.AnalyticsUtil;
import com.android.dialer.calllog.CallLogActivity;
import com.android.dialer.calllog.CallLogFragment;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.DialpadFragment;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.dialpad.SmartDialPrefix;
import com.android.dialer.interactions.PhoneNumberInteraction;
import com.android.dialer.list.DragDropController;
import com.android.dialer.list.ListsFragment;
import com.android.dialer.list.OnDragDropListener;
import com.android.dialer.list.OnListFragmentScrolledListener;
import com.android.dialer.list.PhoneFavoriteSquareTileView;
import com.android.dialer.list.RegularSearchFragment;
import com.android.dialer.list.SearchFragment;
import com.android.dialer.list.SmartDialSearchFragment;
import com.android.dialer.list.SpeedDialFragment;
import com.android.dialer.settings.DialerSettingsActivity;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.widget.ActionBarController;
import com.android.dialer.widget.SearchEditTextLayout;
import com.android.dialer.widget.SearchEditTextLayout.Callback;
import com.android.dialerbind.DatabaseHelperManager;
import com.android.ims.ImsManager;
import com.android.phone.common.animation.AnimUtils;
import com.android.phone.common.animation.AnimationListenerAdapter;
import com.mediatek.contacts.util.ContactsIntent;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.CallAccountSelectionNotificationUtil;
import com.mediatek.dialer.util.DialerFeatureOptions;

import com.mediatek.dialer.util.DialerVolteUtils;
import junit.framework.Assert;

//add by yuantongqin for i99 (start)
import android.app.Dialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;
import com.android.dialer.care_os.I99Font;
import com.android.dialer.care_os.I99Utils;
import android.app.ActionBar.LayoutParams;
import android.content.pm.ActivityInfo;
import android.widget.Toast;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v13.app.FragmentPagerAdapter;
import android.app.FragmentManager;
import android.view.ViewGroup;
//add by yuantongqin for i99 (end)

import java.util.ArrayList;
import java.util.List;

/**
 * The dialer tab's title is 'phone', a more common name (see strings.xml).
 */
public class DialtactsActivity extends TransactionSafeActivity implements View.OnClickListener,
        DialpadFragment.OnDialpadQueryChangedListener,
        OnListFragmentScrolledListener,
        CallLogFragment.HostInterface,
        DialpadFragment.HostInterface,
        ListsFragment.HostInterface,
        SpeedDialFragment.HostInterface,
        SearchFragment.HostInterface,
        OnDragDropListener,
        OnPhoneNumberPickerActionListener,
        PopupMenu.OnMenuItemClickListener,
        ViewPager.OnPageChangeListener,
        ActionBarController.ActivityUi {
    private static final String TAG = "DialtactsActivity";

    /// M: For the purpose of debugging in eng load
    public static final boolean DEBUG = Build.TYPE.equals("eng");

    public static final String SHARED_PREFS_NAME = "com.android.dialer_preferences";

    /** @see #getCallOrigin() */
    private static final String CALL_ORIGIN_DIALTACTS =
            "com.android.dialer.DialtactsActivity";

    private static final String KEY_IN_REGULAR_SEARCH_UI = "in_regular_search_ui";
    private static final String KEY_IN_DIALPAD_SEARCH_UI = "in_dialpad_search_ui";
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_IS_DIALPAD_SHOWN = "is_dialpad_shown";
    /// M: Save and restore the mPendingSearchViewQuery
    private static final String KEY_PENDING_SEARCH_QUERY = "pending_search_query";

    private static final String TAG_DIALPAD_FRAGMENT = "dialpad";
    private static final String TAG_REGULAR_SEARCH_FRAGMENT = "search";
    private static final String TAG_SMARTDIAL_SEARCH_FRAGMENT = "smartdial";
    private static final String TAG_FAVORITES_FRAGMENT = "favorites";

    /**
     * Just for backward compatibility. Should behave as same as {@link Intent#ACTION_DIAL}.
     */
    private static final String ACTION_TOUCH_DIALER = "com.android.phone.action.TOUCH_DIALER";
    public static final String EXTRA_SHOW_TAB = "EXTRA_SHOW_TAB";

    private static final int ACTIVITY_REQUEST_CODE_VOICE_SEARCH = 1;
    /// M: Add for import/export function
    private static final int IMPORT_EXPORT_REQUEST_CODE = 2;

    private static final int FAB_SCALE_IN_DELAY_MS = 300;

    private FrameLayout mParentLayout;

    /**
     * Fragment containing the dialpad that slides into view
     */
    protected DialpadFragment mDialpadFragment;

    /**
     * Fragment for searching phone numbers using the alphanumeric keyboard.
     */
    private RegularSearchFragment mRegularSearchFragment;

    /**
     * Fragment for searching phone numbers using the dialpad.
     */
    private SmartDialSearchFragment mSmartDialSearchFragment;

    /**
     * Animation that slides in.
     */
    private Animation mSlideIn;

    /**
     * Animation that slides out.
     */
    private Animation mSlideOut;

    AnimationListenerAdapter mSlideInListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            maybeEnterSearchUi();
        }
    };

    /**
     * Listener for after slide out animation completes on dialer fragment.
     */
    AnimationListenerAdapter mSlideOutListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            commitDialpadFragmentHide();
        }
    };

    /**
     * Fragment containing the speed dial list, recents list, and all contacts list.
     */
    private ListsFragment mListsFragment;

    /**
     * Tracks whether onSaveInstanceState has been called. If true, no fragment transactions can
     * be commited.
     */
    private boolean mStateSaved;
    private boolean mIsRestarting;
    private boolean mInDialpadSearch;
    private boolean mInRegularSearch;
    private boolean mClearSearchOnPause;
    private boolean mIsDialpadShown;
    private boolean mShowDialpadOnResume;

    /**
     * Whether or not the device is in landscape orientation.
     */
    private boolean mIsLandscape;

    /**
     * True if the dialpad is only temporarily showing due to being in call
     */
    private boolean mInCallDialpadUp;

    /**
     * True when this activity has been launched for the first time.
     */
    private boolean mFirstLaunch;

    /**
     * Search query to be applied to the SearchView in the ActionBar once
     * onCreateOptionsMenu has been called.
     */
    private String mPendingSearchViewQuery;

    private PopupMenu mOverflowMenu;
    private EditText mSearchView;
    private View mVoiceSearchButton;

    private String mSearchQuery;

    private DialerDatabaseHelper mDialerDatabaseHelper;
    private DragDropController mDragDropController;
    private ActionBarController mActionBarController;

    private FloatingActionButtonController mFloatingActionButtonController;

    private int mActionBarHeight;

    /**
     * The text returned from a voice search query.  Set in {@link #onActivityResult} and used in
     * {@link #onResume()} to populate the search box.
     */
    private String mVoiceSearchQuery;
    
 // add by yuantonqgin for i99 (start)
    private Dialog  mOptionDialog;
    private ViewPager mViewPager;
    List<android.app.Fragment> mfrag=new ArrayList<android.app.Fragment>();
    // add by yuantonqgin for i99 (end)

    protected class OptionsPopupMenu extends PopupMenu {
        public OptionsPopupMenu(Context context, View anchor) {
            super(context, anchor, Gravity.END);
        }

        @Override
        public void show() {
            final boolean hasContactsPermission =
                    PermissionsUtil.hasContactsPermissions(DialtactsActivity.this);
            final Menu menu = getMenu();
            final MenuItem clearFrequents = menu.findItem(R.id.menu_clear_frequents);
            clearFrequents.setVisible(mListsFragment != null &&
                    mListsFragment.getSpeedDialFragment() != null &&
                    mListsFragment.getSpeedDialFragment().hasFrequents() && hasContactsPermission);

            menu.findItem(R.id.menu_import_export).setVisible(hasContactsPermission);
            menu.findItem(R.id.menu_add_contact).setVisible(hasContactsPermission);

            menu.findItem(R.id.menu_history).setVisible(
                    PermissionsUtil.hasPhonePermissions(DialtactsActivity.this));
            super.show();
        }
    }

    /**
     * Listener that listens to drag events and sends their x and y coordinates to a
     * {@link DragDropController}.
     */
    private class LayoutOnDragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                mDragDropController.handleDragHovered(v, (int) event.getX(), (int) event.getY());
            }
            return true;
        }
    }

    /**
     * Listener used to send search queries to the phone search fragment.
     */
    private final TextWatcher mPhoneSearchQueryTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String newText = s.toString();
            if (newText.equals(mSearchQuery)) {
                // If the query hasn't changed (perhaps due to activity being destroyed
                // and restored, or user launching the same DIAL intent twice), then there is
                // no need to do anything here.
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "onTextChange for mSearchView called with new query: " + newText);
                Log.d(TAG, "Previous Query: " + mSearchQuery);
            }
            mSearchQuery = newText;

            // Show search fragment only when the query string is changed to non-empty text.
            if (!TextUtils.isEmpty(newText)) {
                // Call enterSearchUi only if we are switching search modes, or showing a search
                // fragment for the first time.
                final boolean sameSearchMode = (mIsDialpadShown && mInDialpadSearch) ||
                        (!mIsDialpadShown && mInRegularSearch);
                if (!sameSearchMode) {
                    enterSearchUi(mIsDialpadShown, mSearchQuery, true /* animate */);
                }
            }

            if (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()) {
                mSmartDialSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
            } else if (mRegularSearchFragment != null && mRegularSearchFragment.isVisible()) {
                mRegularSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    /**
     * Open the search UI when the user clicks on the search box.
     */
    private final View.OnClickListener mSearchViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isInSearchUi()) {
                mActionBarController.onSearchBoxTapped();
                enterSearchUi(false /* smartDialSearch */, mSearchView.getText().toString(),
                        true /* animate */);
            }
        }
    };

    /**
     * Handles the user closing the soft keyboard.
     */
    private final View.OnKeyListener mSearchEditTextLayoutListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (TextUtils.isEmpty(mSearchView.getText().toString())) {
                    // If the search term is empty, close the search UI.
                    maybeExitSearchUi();
                    /// M: end the back key dispatch to avoid activity onBackPressed is called.
                    return true;
                } else {
                    // If the search term is not empty, show the dialpad fab.
                    showFabInSearchUi();
                }
            }
            return false;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            TouchPointManager.getInstance().setPoint((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace.beginSection(TAG + " onCreate");
        super.onCreate(savedInstanceState);

        mFirstLaunch = true;

        final Resources resources = getResources();
        mActionBarHeight = resources.getDimensionPixelSize(R.dimen.action_bar_height_large);

        Trace.beginSection(TAG + " setContentView");
        setContentView(R.layout.dialtacts_activity);
        Trace.endSection();
        getWindow().setBackgroundDrawable(null);

        Trace.beginSection(TAG + " setup Views");
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.search_edittext);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(null);
		//yuan tong qin add start
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏 
        actionBar.hide();
        //yuan tong qin add end

        SearchEditTextLayout searchEditTextLayout =
                (SearchEditTextLayout) actionBar.getCustomView().findViewById(R.id.search_view_container);
        searchEditTextLayout.setPreImeKeyListener(mSearchEditTextLayoutListener);

        mActionBarController = new ActionBarController(this, searchEditTextLayout);

        mSearchView = (EditText) searchEditTextLayout.findViewById(R.id.search_view);
        mSearchView.addTextChangedListener(mPhoneSearchQueryTextListener);
        mVoiceSearchButton = searchEditTextLayout.findViewById(R.id.voice_search_button);
        searchEditTextLayout.findViewById(R.id.search_magnifying_glass)
                .setOnClickListener(mSearchViewOnClickListener);
        searchEditTextLayout.findViewById(R.id.search_box_start_search)
                .setOnClickListener(mSearchViewOnClickListener);
        searchEditTextLayout.setOnClickListener(mSearchViewOnClickListener);
        searchEditTextLayout.setCallback(new SearchEditTextLayout.Callback() {
            @Override
            public void onBackButtonClicked() {
                onBackPressed();
            }

            @Override
            public void onSearchViewClicked() {
                // Hide FAB, as the keyboard is shown.
                mFloatingActionButtonController.scaleOut();
            }
        });

        mIsLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        final View floatingActionButtonContainer = findViewById(
                R.id.floating_action_button_container);
        ImageButton floatingActionButton = (ImageButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(this);
        mFloatingActionButtonController = new FloatingActionButtonController(this,
                floatingActionButtonContainer, floatingActionButton);

        ImageButton optionsMenuButton =
                (ImageButton) searchEditTextLayout.findViewById(R.id.dialtacts_options_menu_button);
        optionsMenuButton.setOnClickListener(this);
        mOverflowMenu = buildOptionsMenu(searchEditTextLayout);
        optionsMenuButton.setOnTouchListener(mOverflowMenu.getDragToOpenListener());

        // Add the favorites fragment but only if savedInstanceState is null. Otherwise the
        // fragment manager is responsible for recreating it.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.dialtacts_frame, new ListsFragment(), TAG_FAVORITES_FRAGMENT)
                    .commit();
        } else {
            mSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY);
            mInRegularSearch = savedInstanceState.getBoolean(KEY_IN_REGULAR_SEARCH_UI);
            mInDialpadSearch = savedInstanceState.getBoolean(KEY_IN_DIALPAD_SEARCH_UI);
            mFirstLaunch = savedInstanceState.getBoolean(KEY_FIRST_LAUNCH);
            mShowDialpadOnResume = savedInstanceState.getBoolean(KEY_IS_DIALPAD_SHOWN);
            /// M: Save and restore the mPendingSearchViewQuery
            mPendingSearchViewQuery = savedInstanceState.getString(KEY_PENDING_SEARCH_QUERY);
            mActionBarController.restoreInstanceState(savedInstanceState);
        }

        final boolean isLayoutRtl = DialerUtils.isRtl();
        if (mIsLandscape) {
            mSlideIn = AnimationUtils.loadAnimation(this,
                    isLayoutRtl ? R.anim.dialpad_slide_in_left : R.anim.dialpad_slide_in_right);
            mSlideOut = AnimationUtils.loadAnimation(this,
                    isLayoutRtl ? R.anim.dialpad_slide_out_left : R.anim.dialpad_slide_out_right);
        } else {
            mSlideIn = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_in_bottom);
            mSlideOut = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_out_bottom);
        }

        mSlideIn.setInterpolator(AnimUtils.EASE_IN);
        mSlideOut.setInterpolator(AnimUtils.EASE_OUT);

        mSlideIn.setAnimationListener(mSlideInListener);
        mSlideOut.setAnimationListener(mSlideOutListener);

        mParentLayout = (FrameLayout) findViewById(R.id.dialtacts_mainlayout);
        mParentLayout.setOnDragListener(new LayoutOnDragListener());
        floatingActionButtonContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final ViewTreeObserver observer =
                                floatingActionButtonContainer.getViewTreeObserver();
                        if (!observer.isAlive()) {
                            return;
                        }
                        observer.removeOnGlobalLayoutListener(this);
                        int screenWidth = mParentLayout.getWidth();
                        mFloatingActionButtonController.setScreenWidth(screenWidth);
                        mFloatingActionButtonController.align(
                                getFabAlignment(), false /* animate */);
                    }
                });

        Trace.endSection();

        Trace.beginSection(TAG + " initialize smart dialing");

       /// M: [MTK Dialer Search] @{
        if (!DialerFeatureOptions.isDialerSearchEnabled()) {
            mDialerDatabaseHelper = DatabaseHelperManager.getDatabaseHelper(this);
            SmartDialPrefix.initializeNanpSettings(this);
        }
        /// @}

        //yuan tong qin add start
        mViewPager = (ViewPager) findViewById(R.id.dialtacts_call_log_pager);
        addData();//添加数据
        mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
        //yuan tong qin add end
        Trace.endSection();
        Trace.endSection();
    }

    //yuan tong qin add start 
    
    public void addData(){
    	mfrag.add(new DialpadFragment());
    }
    
    private class  ViewPagerAdapter extends FragmentPagerAdapter{
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	
//            return new DialpadFragment();
        	return mfrag.get(position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            // The parent's setPrimaryItem() also calls setMenuVisibility(), so we want to know
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            // modify by y.haiyang for i99(start)
            /**return TAB_INDEX_COUNT;*/
            return  1;
            // modfiy by y.haiyang for i99(end)
        }
    }
    
    //yuan tong qin add end

    @Override
    protected void onResume() {
        Trace.beginSection(TAG + " onResume");
        super.onResume();

        mStateSaved = false;
        if (mFirstLaunch) {
            displayFragment(getIntent());
        } else if (!phoneIsInUse() && mInCallDialpadUp) {
//        	hideDialpadFragment(false, true);//yuan tong qin del
            mInCallDialpadUp = false;
        } else if (mShowDialpadOnResume) {
//            showDialpadFragment(false);//yuan tong qin del
            mShowDialpadOnResume = false;
        }

        // If there was a voice query result returned in the {@link #onActivityResult} callback, it
        // will have been stashed in mVoiceSearchQuery since the search results fragment cannot be
        // shown until onResume has completed.  Active the search UI and set the search term now.
        if (!TextUtils.isEmpty(mVoiceSearchQuery)) {
            mActionBarController.onSearchBoxTapped();
            mSearchView.setText(mVoiceSearchQuery);
            mVoiceSearchQuery = null;
        }

        mFirstLaunch = false;

        if (mIsRestarting) {
            // This is only called when the activity goes from resumed -> paused -> resumed, so it
            // will not cause an extra view to be sent out on rotation
           //yuan tong qin del start
        	/* if (mIsDialpadShown) {
                AnalyticsUtil.sendScreenView(mDialpadFragment, this);
            }*/
        	
        	//yuan tong qin del end 
            mIsRestarting = false;
        }

        prepareVoiceSearchButton();

        /// M: [MTK Dialer Search] @{
        if (!DialerFeatureOptions.isDialerSearchEnabled()) {
            mDialerDatabaseHelper.startSmartDialUpdateThread();
        }
        /// @}

        mFloatingActionButtonController.align(getFabAlignment(), false /* animate */);

        /// M: [Call Account Notification] Show the call account selection notification
        CallAccountSelectionNotificationUtil.getInstance(this).showNotification(true, this);

        if (getIntent().hasExtra(EXTRA_SHOW_TAB)) {
            int index = getIntent().getIntExtra(EXTRA_SHOW_TAB, ListsFragment.TAB_INDEX_SPEED_DIAL);
            if (index < mListsFragment.getTabCount()) {
                mListsFragment.showTab(index);
            }
        } else if (Calls.CONTENT_TYPE.equals(getIntent().getType())) {
            mListsFragment.showTab(ListsFragment.TAB_INDEX_RECENTS);
        }

        Trace.endSection();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mIsRestarting = true;
    }

    @Override
    protected void onPause() {
        if (mClearSearchOnPause) {
            hideDialpadAndSearchUi();
            mClearSearchOnPause = false;
        }
        /// M: [Call Account Notification] Hide the call account selection notification
        CallAccountSelectionNotificationUtil.getInstance(this).showNotification(false, this);
		//yuan tong qin del
        // if (mSlideOut.hasStarted() && !mSlideOut.hasEnded()) {
        //    commitDialpadFragmentHide();
        // }
        /// @}
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_QUERY, mSearchQuery);
        outState.putBoolean(KEY_IN_REGULAR_SEARCH_UI, mInRegularSearch);
        outState.putBoolean(KEY_IN_DIALPAD_SEARCH_UI, mInDialpadSearch);
        outState.putBoolean(KEY_FIRST_LAUNCH, mFirstLaunch);
        outState.putBoolean(KEY_IS_DIALPAD_SHOWN, mIsDialpadShown);
        /// M: Save and restore the mPendingSearchViewQuery
        outState.putString(KEY_PENDING_SEARCH_QUERY, mPendingSearchViewQuery);
        mActionBarController.saveInstanceState(outState);
        mStateSaved = true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof DialpadFragment) {
            mDialpadFragment = (DialpadFragment) fragment;
            if (!mIsDialpadShown && !mShowDialpadOnResume) {
                final FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                transaction.hide(mDialpadFragment);//yuan tong qin del
                transaction.commit();
            }
        } else if (fragment instanceof SmartDialSearchFragment) {
            mSmartDialSearchFragment = (SmartDialSearchFragment) fragment;
            mSmartDialSearchFragment.setOnPhoneNumberPickerActionListener(this);
            /// M: Exist the case that fragment has not be attached, but
            // mAddToContactNumber is needed.
            if (mDialpadFragment != null && mDialpadFragment.getDigitsWidget() != null
                    && !TextUtils.isEmpty(mDialpadFragment.getDigitsWidget().getText())) {
                mSmartDialSearchFragment.setAddToContactNumber(mDialpadFragment
                        .getDigitsWidget().getText().toString());
            }
        } else if (fragment instanceof SearchFragment) {
            mRegularSearchFragment = (RegularSearchFragment) fragment;
            mRegularSearchFragment.setOnPhoneNumberPickerActionListener(this);
        } else if (fragment instanceof ListsFragment) {
            mListsFragment = (ListsFragment) fragment;
            mListsFragment.addOnPageChangeListener(this);
        }
    }

    protected void handleMenuSettings() {
        final Intent intent = new Intent(this, DialerSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_action_button:
                if (mListsFragment.getCurrentTabIndex()
                        == ListsFragment.TAB_INDEX_ALL_CONTACTS && !mInRegularSearch) {
                    DialerUtils.startActivityWithErrorToast(
                            this,
                            IntentUtil.getNewContactIntent(),
                            R.string.add_contact_not_available);
                } else if (!mIsDialpadShown) {
                    mInCallDialpadUp = false;
                    showDialpadFragment(true);
                }
                break;
            case R.id.voice_search_button:
                try {
                    startActivityForResult(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
                            ACTIVITY_REQUEST_CODE_VOICE_SEARCH);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(DialtactsActivity.this, R.string.voice_search_not_available,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.dialtacts_options_menu_button:
                mOverflowMenu.show();
                break;
            default: {
                Log.wtf(TAG, "Unexpected onClick event from " + view);
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history:
                // Use explicit CallLogActivity intent instead of ACTION_VIEW +
                // CONTENT_TYPE, so that we always open our call log from our dialer
                final Intent intent = new Intent(this, CallLogActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_add_contact:
                DialerUtils.startActivityWithErrorToast(
                        this,
                        IntentUtil.getNewContactIntent(),
                        R.string.add_contact_not_available);
                break;
            case R.id.menu_import_export:
                // We hard-code the "contactsAreAvailable" argument because doing it properly would
                // involve querying a {@link ProviderStatusLoader}, which we don't want to do right
                // now in Dialtacts for (potential) performance reasons. Compare with how it is
                // done in {@link PeopleActivity}.
                /**
                 * M: When it is A1 project,use Google import/export function or
                 * use MTK. @{
                 */
                if (DialerFeatureOptions.isA1ProjectEnabled()) {
                    ImportExportDialogFragment.show(getFragmentManager(), true,
                            DialtactsActivity.class);
                } else {
                    final Intent importIntent = new Intent(
                            ContactsIntent.LIST.ACTION_IMPORTEXPORT_CONTACTS);
                    importIntent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,
                            DialtactsActivity.class.getName());
                    try {
                        startActivityForResult(importIntent, IMPORT_EXPORT_REQUEST_CODE);
                    } catch (ActivityNotFoundException ex) {
                        ImportExportDialogFragment.show(getFragmentManager(), true,
                                DialtactsActivity.class);
                    }
                }
                /** @} */
                return true;
            case R.id.menu_clear_frequents:
                ClearFrequentsDialog.show(getFragmentManager());
                return true;
            case R.id.menu_call_settings:
                handleMenuSettings();
                return true;
            /** M: [VoLTE ConfCall] handle conference call menu. @{ */
            case R.id.menu_volte_conf_call:
                DialerVolteUtils.handleMenuVolteConfCall(this);
                return true;
            /** @} */
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_CODE_VOICE_SEARCH) {
            if (resultCode == RESULT_OK) {
                final ArrayList<String> matches = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if (matches.size() > 0) {
                    final String match = matches.get(0);
                    mVoiceSearchQuery = match;
                } else {
                    Log.e(TAG, "Voice search - nothing heard");
                }
            } else {
                Log.e(TAG, "Voice search failed");
            }
        }
        /** M: [VoLTE ConfCall] Handle the volte conference call. @{ */
        else if (requestCode == DialerVolteUtils.ACTIVITY_REQUEST_CODE_PICK_PHONE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                DialerVolteUtils.launchVolteConfCall(this, data);
            } else {
                Log.d(TAG, "No contacts picked, Volte conference call cancelled.");
            }
        }
        /** @} */
        /** M: [Import/Export] Handle the import/export activity result. @{ */
        else if (requestCode == IMPORT_EXPORT_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Import/Export activity create failed! ");
            } else {
                Log.d(TAG, "Import/Export activity create successfully! ");
            }
        }
        /** @} */

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initiates a fragment transaction to show the dialpad fragment. Animations and other visual
     * updates are handled by a callback which is invoked after the dialpad fragment is shown.
     * @see #onDialpadShown
     */
    private void showDialpadFragment(boolean animate) {
        if (mIsDialpadShown || mStateSaved) {
            return;
        }
        mIsDialpadShown = true;

        mListsFragment.setUserVisibleHint(false);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (mDialpadFragment == null) {
            mDialpadFragment = new DialpadFragment();
            ft.add(R.id.dialtacts_container, mDialpadFragment, TAG_DIALPAD_FRAGMENT);
        } else {
            ft.show(mDialpadFragment);
        }

        mDialpadFragment.setAnimate(animate);
        AnalyticsUtil.sendScreenView(mDialpadFragment);
        ft.commit();

        if (animate) {
            mFloatingActionButtonController.scaleOut();
        } else {
            mFloatingActionButtonController.setVisible(false);
            maybeEnterSearchUi();
        }
        mActionBarController.onDialpadUp();

        mListsFragment.getView().animate().alpha(0).withLayer();
    }

    /**
     * Callback from child DialpadFragment when the dialpad is shown.
     */
    public void onDialpadShown() {
        Assert.assertNotNull(mDialpadFragment);
        if (mDialpadFragment.getAnimate()) {
            mDialpadFragment.getView().startAnimation(mSlideIn);
        } else {
            mDialpadFragment.setYFraction(0);
        }

        updateSearchFragmentPosition();
    }

    /**
     * Initiates animations and other visual updates to hide the dialpad. The fragment is hidden in
     * a callback after the hide animation ends.
     * @see #commitDialpadFragmentHide
     */
    public void hideDialpadFragment(boolean animate, boolean clearDialpad) {
        if (mDialpadFragment == null || mDialpadFragment.getView() == null) {
            return;
        }
        if (clearDialpad) {
            mDialpadFragment.clearDialpad();
        }
        if (!mIsDialpadShown) {
            return;
        }
        mIsDialpadShown = false;
        mDialpadFragment.setAnimate(animate);
        mListsFragment.setUserVisibleHint(true);
        mListsFragment.sendScreenViewForCurrentPosition();

        updateSearchFragmentPosition();

        mFloatingActionButtonController.align(getFabAlignment(), animate);
        if (animate) {
            mDialpadFragment.getView().startAnimation(mSlideOut);
        } else {
            commitDialpadFragmentHide();
        }

        mActionBarController.onDialpadDown();

        if (isInSearchUi()) {
            if (TextUtils.isEmpty(mSearchQuery)) {
                exitSearchUi();
            }
        }

        /// @}
    }

    /**
     * Finishes hiding the dialpad fragment after any animations are completed.
     */
    private void commitDialpadFragmentHide() {
        if (!mStateSaved && mDialpadFragment != null && !mDialpadFragment.isHidden()) {
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
       //     ft.hide(mDialpadFragment);//yuan tong qin del 
            ft.commit();
        }
        mFloatingActionButtonController.scaleIn(AnimUtils.NO_DELAY);
    }

    private void updateSearchFragmentPosition() {
        SearchFragment fragment = null;
        if (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()) {
            fragment = mSmartDialSearchFragment;
        } else if (mRegularSearchFragment != null && mRegularSearchFragment.isVisible()) {
            fragment = mRegularSearchFragment;
        }
        if (fragment != null && fragment.isVisible()) {
            fragment.updatePosition(true /* animate */);
        }
    }

    @Override
    public boolean isInSearchUi() {
        return mInDialpadSearch || mInRegularSearch;
    }

    @Override
    public boolean hasSearchQuery() {
        return !TextUtils.isEmpty(mSearchQuery);
    }

    @Override
    public boolean shouldShowActionBar() {
        return mListsFragment.shouldShowActionBar();
    }

    private void setNotInSearchUi() {
        mInDialpadSearch = false;
        mInRegularSearch = false;
    }

    private void hideDialpadAndSearchUi() {
        if (mIsDialpadShown) {
            hideDialpadFragment(false, true);
        } else {
            exitSearchUi();
        }
    }

    private void prepareVoiceSearchButton() {
        final Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        /**
         * M: [ALPS02227737] set value for view to record the voice search
         * button status @{
         */
        boolean canBeHandled = canIntentBeHandled(voiceIntent);
        SearchEditTextLayout searchBox = (SearchEditTextLayout) getActionBar().getCustomView();
        if (searchBox != null) {
            searchBox.setCanHandleSpeech(canBeHandled);
        }
        /** @} */
        //yuan tong qin modify start
//        if (canBeHandled) {
//            mVoiceSearchButton.setVisibility(View.VISIBLE);
//            mVoiceSearchButton.setOnClickListener(this);
//        } else {
            mVoiceSearchButton.setVisibility(View.GONE);
//        }
            
            //yuan tong qin modify end 
    }

    protected OptionsPopupMenu buildOptionsMenu(View invoker) {
        /** M: [VoLTE ConfCall] Show conference call menu for volte. @{ */
        final OptionsPopupMenu popupMenu = new OptionsPopupMenu(this, invoker) {
            @Override
            public void show() {
                boolean visible = DialerVolteUtils
                        .isVolteConfCallEnable(DialtactsActivity.this);
                getMenu().findItem(R.id.menu_volte_conf_call).setVisible(visible);
                super.show();
            }
        };
        /** @} */
        popupMenu.inflate(R.menu.dialtacts_options);

        /// M: add for plug-in. @{
        final Menu menu = popupMenu.getMenu();
        ExtensionManager.getInstance().getDialPadExtension().buildOptionsMenu(this, menu);
        /// @}

        popupMenu.setOnMenuItemClickListener(this);
        return popupMenu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** M: Modify to set the pending search query only when dialpad is visible. @{ */
        if (mPendingSearchViewQuery != null
                && mDialpadFragment != null && mDialpadFragment.isVisible()) {
            mSearchView.setText(mPendingSearchViewQuery);
            mPendingSearchViewQuery = null;
        }
        /** @} */
        if (mActionBarController != null) {
            mActionBarController.restoreActionBarOffset();
        }
        return false;
    }

    /**
     * Returns true if the intent is due to hitting the green send key (hardware call button:
     * KEYCODE_CALL) while in a call.
     *
     * @param intent the intent that launched this activity
     * @return true if the intent is due to hitting the green send key while in a call
     */
    private boolean isSendKeyWhileInCall(Intent intent) {
        // If there is a call in progress and the user launched the dialer by hitting the call
        // button, go straight to the in-call screen.
        final boolean callKey = Intent.ACTION_CALL_BUTTON.equals(intent.getAction());

        if (callKey) {
            getTelecomManager().showInCallScreen(false);
            return true;
        }

        return false;
    }

    /**
     * Sets the current tab based on the intent's request type
     *
     * @param intent Intent that contains information about which tab should be selected
     */
    private void displayFragment(Intent intent) {
        // If we got here by hitting send and we're in call forward along to the in-call activity
        if (isSendKeyWhileInCall(intent)) {
            finish();
            return;
        }

        final boolean phoneIsInUse = phoneIsInUse();
        if (phoneIsInUse || (intent.getData() !=  null && isDialIntent(intent))) {
            showDialpadFragment(false);
            mDialpadFragment.setStartedFromNewIntent(true);
            if (phoneIsInUse && !mDialpadFragment.isVisible()) {
                mInCallDialpadUp = true;
            }
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        setIntent(newIntent);

        mStateSaved = false;
        displayFragment(newIntent);

        invalidateOptionsMenu();
    }

    /** Returns true if the given intent contains a phone number to populate the dialer with */
    private boolean isDialIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || ACTION_TOUCH_DIALER.equals(action)) {
            return true;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            final Uri data = intent.getData();
            if (data != null && PhoneAccount.SCHEME_TEL.equals(data.getScheme())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an appropriate call origin for this Activity. May return null when no call origin
     * should be used (e.g. when some 3rd party application launched the screen. Call origin is
     * for remembering the tab in which the user made a phone call, so the external app's DIAL
     * request should not be counted.)
     */
    public String getCallOrigin() {
        return !isDialIntent(getIntent()) ? CALL_ORIGIN_DIALTACTS : null;
    }

    /**
     * Shows the search fragment
     */
    private void enterSearchUi(boolean smartDialSearch, String query, boolean animate) {
        if (mStateSaved || getFragmentManager().isDestroyed()) {
            // Weird race condition where fragment is doing work after the activity is destroyed
            // due to talkback being on (b/10209937). Just return since we can't do any
            // constructive here.
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "Entering search UI - smart dial " + smartDialSearch);
        }

        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mInDialpadSearch && mSmartDialSearchFragment != null) {
            transaction.remove(mSmartDialSearchFragment);
        } else if (mInRegularSearch && mRegularSearchFragment != null) {
            transaction.remove(mRegularSearchFragment);
        }

        final String tag;
        if (smartDialSearch) {
            tag = TAG_SMARTDIAL_SEARCH_FRAGMENT;
        } else {
            tag = TAG_REGULAR_SEARCH_FRAGMENT;
        }
        mInDialpadSearch = smartDialSearch;
        mInRegularSearch = !smartDialSearch;

        mFloatingActionButtonController.scaleOut();

        SearchFragment fragment = (SearchFragment) getFragmentManager().findFragmentByTag(tag);
        if (animate) {
            transaction.setCustomAnimations(android.R.animator.fade_in, 0);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        }

        /// M: If switch to a new fragment, it need to set query string to this
        // fragment, otherwise the query result would show nothing. @{
        boolean needToSetQuery = false;
        if (fragment == null) {
            needToSetQuery = true;
            if (smartDialSearch) {
                fragment = new SmartDialSearchFragment();
            } else {
                fragment = new RegularSearchFragment();
                fragment.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Show the FAB when the user touches the lists fragment and the soft
                        // keyboard is hidden.
                        showFabInSearchUi();
                        return false;
                    }
                });
            }
            transaction.add(R.id.dialtacts_frame, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        // DialtactsActivity will provide the options menu
        fragment.setHasOptionsMenu(false);
        fragment.setShowEmptyListForNullQuery(true);
        if (!smartDialSearch || needToSetQuery) {
            fragment.setQueryString(query, false /* delaySelection */);
        }
        // @}
        transaction.commit();

        if (animate) {
            mListsFragment.getView().animate().alpha(0).withLayer();
        }
        mListsFragment.setUserVisibleHint(false);
    }

    /**
     * Hides the search fragment
     */
    private void exitSearchUi() {
        // See related bug in enterSearchUI();
        if (getFragmentManager().isDestroyed() || mStateSaved) {
            return;
        }

        mSearchView.setText(null);

        if (mDialpadFragment != null) {
            mDialpadFragment.clearDialpad();
        }

        setNotInSearchUi();

        // Restore the FAB for the lists fragment.
        if (getFabAlignment() != FloatingActionButtonController.ALIGN_END) {
            mFloatingActionButtonController.setVisible(false);
        }
        mFloatingActionButtonController.scaleIn(FAB_SCALE_IN_DELAY_MS);
        onPageScrolled(mListsFragment.getCurrentTabIndex(), 0 /* offset */, 0 /* pixelOffset */);
        onPageSelected(mListsFragment.getCurrentTabIndex());

        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mSmartDialSearchFragment != null) {
            transaction.remove(mSmartDialSearchFragment);
        }
        if (mRegularSearchFragment != null) {
            transaction.remove(mRegularSearchFragment);
        }
        transaction.commit();

        mListsFragment.getView().animate().alpha(1).withLayer();

        if (mDialpadFragment == null || !mDialpadFragment.isVisible()) {
            // If the dialpad fragment wasn't previously visible, then send a screen view because
            // we are exiting regular search. Otherwise, the screen view will be sent by
            // {@link #hideDialpadFragment}.
            mListsFragment.sendScreenViewForCurrentPosition();
            mListsFragment.setUserVisibleHint(true);
        }

        mActionBarController.onSearchUiExited();
    }

    @Override
    public void onBackPressed() {
        if (mStateSaved) {
            return;
        }

        if (false) {//modified by Yar @20170802
            if (TextUtils.isEmpty(mSearchQuery) ||
                    (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()
                            && mSmartDialSearchFragment.getAdapter().getCount() == 0)) {
                exitSearchUi();
            }
            hideDialpadFragment(true, false);
        } else if (isInSearchUi()) {
            exitSearchUi();
            DialerUtils.hideInputMethod(mParentLayout);
        } else {
            super.onBackPressed();
        }
    }

    private void maybeEnterSearchUi() {
        if (!isInSearchUi()) {
            enterSearchUi(true /* isSmartDial */, mSearchQuery, false);
        }
    }

    /**
     * @return True if the search UI was exited, false otherwise
     */
    private boolean maybeExitSearchUi() {
        if (isInSearchUi() && TextUtils.isEmpty(mSearchQuery)) {
            exitSearchUi();
            DialerUtils.hideInputMethod(mParentLayout);
            return true;
        }
        return false;
    }

    private void showFabInSearchUi() {
        mFloatingActionButtonController.changeIcon(
                getResources().getDrawable(R.drawable.fab_ic_dial),
                getResources().getString(R.string.action_menu_dialpad_button));
        mFloatingActionButtonController.align(getFabAlignment(), false /* animate */);
        mFloatingActionButtonController.scaleIn(FAB_SCALE_IN_DELAY_MS);
    }

    @Override
    public void onDialpadQueryChanged(String query) {
        if (mSmartDialSearchFragment != null) {
            mSmartDialSearchFragment.setAddToContactNumber(query);
        }
        final String normalizedQuery = SmartDialNameMatcher.normalizeNumber(query,
                /* M: [MTK Dialer Search] use mtk enhance dialpad map */
                DialerFeatureOptions.isDialerSearchEnabled() ?
                        SmartDialNameMatcher.SMART_DIALPAD_MAP
                        : SmartDialNameMatcher.LATIN_SMART_DIAL_MAP);

        if (!TextUtils.equals(mSearchView.getText(), normalizedQuery)) {
            if (DEBUG) {
                Log.d(TAG, "onDialpadQueryChanged - new query: " + query);
            }
            if (mDialpadFragment == null || !mDialpadFragment.isVisible()) {
                // This callback can happen if the dialpad fragment is recreated because of
                // activity destruction. In that case, don't update the search view because
                // that would bring the user back to the search fragment regardless of the
                // previous state of the application. Instead, just return here and let the
                // fragment manager correctly figure out whatever fragment was last displayed.
                if (!TextUtils.isEmpty(normalizedQuery)) {
                    mPendingSearchViewQuery = normalizedQuery;
                }
                return;
            }
            mSearchView.setText(normalizedQuery);
        }

        try {
            if (mDialpadFragment != null && mDialpadFragment.isVisible()) {
                mDialpadFragment.process_quote_emergency_unquote(normalizedQuery);
            }
        } catch (Exception ignored) {
            // Skip any exceptions for this piece of code
        }
    }

    @Override
    public boolean onDialpadSpacerTouchWithEmptyQuery() {
        if (mInDialpadSearch && mSmartDialSearchFragment != null
                && !mSmartDialSearchFragment.isShowingPermissionRequest()) {
            hideDialpadFragment(true /* animate */, true /* clearDialpad */);
            return true;
        }
        return false;
    }

    @Override
    public void onListFragmentScrollStateChange(int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            hideDialpadFragment(true, false);
            DialerUtils.hideInputMethod(mParentLayout);
        }
    }

    @Override
    public void onListFragmentScroll(int firstVisibleItem, int visibleItemCount,
                                     int totalItemCount) {
        // TODO: No-op for now. This should eventually show/hide the actionBar based on
        // interactions with the ListsFragments.
    }

    private boolean phoneIsInUse() {
        return getTelecomManager().isInCall();
    }

    private boolean canIntentBeHandled(Intent intent) {
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && resolveInfo.size() > 0;
    }

    /**
     * Called when the user has long-pressed a contact tile to start a drag operation.
     */
    @Override
    public void onDragStarted(int x, int y, PhoneFavoriteSquareTileView view) {
        mListsFragment.showRemoveView(true);
    }

    @Override
    public void onDragHovered(int x, int y, PhoneFavoriteSquareTileView view) {
    }

    /**
     * Called when the user has released a contact tile after long-pressing it.
     */
    @Override
    public void onDragFinished(int x, int y) {
        mListsFragment.showRemoveView(false);
    }

    @Override
    public void onDroppedOnRemove() {}

    /**
     * Allows the SpeedDialFragment to attach the drag controller to mRemoveViewContainer
     * once it has been attached to the activity.
     */
    @Override
    public void setDragDropController(DragDropController dragController) {
        mDragDropController = dragController;
        mListsFragment.getRemoveView().setDragDropController(dragController);
    }

    /**
     * Implemented to satisfy {@link SpeedDialFragment.HostInterface}
     */
    @Override
    public void showAllContactsTab() {
        if (mListsFragment != null) {
            mListsFragment.showTab(ListsFragment.TAB_INDEX_ALL_CONTACTS);
        }
    }

    /**
     * Implemented to satisfy {@link CallLogFragment.HostInterface}
     */
    @Override
    public void showDialpad() {
        showDialpadFragment(true);
    }

    @Override
    public void onPickPhoneNumberAction(Uri dataUri) {
        // Specify call-origin so that users will see the previous tab instead of
        // CallLog screen (search UI will be automatically exited).
        PhoneNumberInteraction.startInteractionForPhoneCall(
                DialtactsActivity.this, dataUri, getCallOrigin());
        mClearSearchOnPause = true;
    }

    @Override
    public void onCallNumberDirectly(String phoneNumber) {
        onCallNumberDirectly(phoneNumber, false /* isVideoCall */);
    }

    @Override
    public void onCallNumberDirectly(String phoneNumber, boolean isVideoCall) {
        if (phoneNumber == null) {
            // Invalid phone number, but let the call go through so that InCallUI can show
            // an error message.
            phoneNumber = "";
        }
        Intent intent = isVideoCall ?
                IntentUtil.getVideoCallIntent(phoneNumber, getCallOrigin()) :
                IntentUtil.getCallIntent(phoneNumber, getCallOrigin());
        DialerUtils.startActivityWithErrorToast(this, intent);
        mClearSearchOnPause = true;
    }

    @Override
    public void onShortcutIntentCreated(Intent intent) {
        Log.w(TAG, "Unsupported intent has come (" + intent + "). Ignoring.");
    }

    @Override
    public void onHomeInActionBarSelected() {
        exitSearchUi();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int tabIndex = mListsFragment.getCurrentTabIndex();

        // Scroll the button from center to end when moving from the Speed Dial to Recents tab.
        // In RTL, scroll when the current tab is Recents instead of Speed Dial, because the order
        // of the tabs is reversed and the ViewPager returns the left tab position during scroll.
        boolean isRtl = DialerUtils.isRtl();
        if (!isRtl && tabIndex == ListsFragment.TAB_INDEX_SPEED_DIAL && !mIsLandscape) {
            mFloatingActionButtonController.onPageScrolled(positionOffset);
        } else if (isRtl && tabIndex == ListsFragment.TAB_INDEX_RECENTS && !mIsLandscape) {
            mFloatingActionButtonController.onPageScrolled(1 - positionOffset);
        } else if (tabIndex != ListsFragment.TAB_INDEX_SPEED_DIAL) {
            mFloatingActionButtonController.onPageScrolled(1);
        }
    }

    @Override
    public void onPageSelected(int position) {
        int tabIndex = mListsFragment.getCurrentTabIndex();
        if (tabIndex == ListsFragment.TAB_INDEX_ALL_CONTACTS) {
            mFloatingActionButtonController.changeIcon(
                    getResources().getDrawable(R.drawable.ic_person_add_24dp),
                    getResources().getString(R.string.search_shortcut_create_new_contact));
        } else {
            mFloatingActionButtonController.changeIcon(
                    getResources().getDrawable(R.drawable.fab_ic_dial),
                    getResources().getString(R.string.action_menu_dialpad_button));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private TelecomManager getTelecomManager() {
        return (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
    }

    @Override
    public boolean isActionBarShowing() {
        return mActionBarController.isActionBarShowing();
    }

    @Override
    public ActionBarController getActionBarController() {
        return mActionBarController;
    }

    @Override
    public boolean isDialpadShown() {
        return mIsDialpadShown;
    }

    @Override
    public int getDialpadHeight() {
        if (mDialpadFragment != null) {
            return mDialpadFragment.getDialpadHeight();
        }
        return 0;
    }

    @Override
    public int getActionBarHideOffset() {
        return getActionBar().getHideOffset();
    }

    @Override
    public void setActionBarHideOffset(int offset) {
        getActionBar().setHideOffset(offset);
    }

    @Override
    public int getActionBarHeight() {
        return mActionBarHeight;
    }

    private int getFabAlignment() {
        if (!mIsLandscape && !isInSearchUi() &&
                mListsFragment.getCurrentTabIndex() == ListsFragment.TAB_INDEX_SPEED_DIAL) {
            return FloatingActionButtonController.ALIGN_MIDDLE;
        }
        return FloatingActionButtonController.ALIGN_END;
    }

    /**
     * M: Set to clear dialpad and exit search ui while activity on pause
     * @param clearSearch If true clear dialpad and exit search ui while activity on pause
     */
    public void setClearSearchOnPause(boolean clearSearch) {
        mClearSearchOnPause = clearSearch;
    }
    
    //  yuan tong qin add start
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(mOptionDialog != null && mOptionDialog.isShowing()){
            mOptionDialog.dismiss();
            mOptionDialog = null ;
        }else{
            showOptionDialog();
        }
        return super.onMenuOpened(featureId, menu);
    }
    
 
    //菜单显示的布局文件
    private void showOptionDialog(){
        mOptionDialog = new Dialog(this, R.style.I99DialogStyle);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.i99_dialtact_option_dialog, null);
        TextView title = (TextView)view.findViewById(R.id.title);
        Button callSettings = (Button)view.findViewById(R.id.call_settings);
        callSettings.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
            	//设置的跳转
            	  Intent intent = new Intent(DialtactsActivity.this, DialerSettingsActivity.class);
                  startActivity(intent);
//                startActivity(DialtactsActivity.getCallSettingsIntent());
                if(mOptionDialog != null){
                    mOptionDialog.dismiss();
                    mOptionDialog = null ;
                }
            }
        });

        title.setTextSize(I99Font.TITLE);
        callSettings.setTextSize(I99Font.TITLE);
        android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT);
        lp.width = I99Utils.getScreenSize(this)[0];//屏幕的宽和高
        mOptionDialog.addContentView(view, lp);
        mOptionDialog.show();
        Window window = mOptionDialog.getWindow();
        lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.I99DialogAnim);
        Log.i("test", "==出来了没有==");
    }
    // yuan tong qin add end
}