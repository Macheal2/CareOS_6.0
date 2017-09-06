/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.contacts.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor.SaveMode;//yuan tong qin add
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.AccountsListAdapter;
import com.android.contacts.common.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.detail.PhotoSelectionHandler;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.UiClosables;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.util.Log;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

//yuan tong qin add  begin
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.content.DialogInterface;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.cappu.app.CareDialog;
import com.cappu.contacts.I99ContactHeaderActivity;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;
import android.view.inputmethod.InputMethodManager;
//yuan tong qin add  end 

/**
 * Contact editor with all fields displayed.
 */
public class ContactEditorFragment extends ContactEditorBaseFragment implements
        RawContactReadOnlyEditorView.Listener {
    private static final String TAG = "ContactEditorFragment";

    private static final String KEY_EXPANDED_EDITORS = "expandedEditors";

    private static final String KEY_RAW_CONTACT_ID_REQUESTING_PHOTO = "photorequester";
    private static final String KEY_CURRENT_PHOTO_URI = "currentphotouri";

    // Used to store which raw contact editors have been expanded. Keyed on raw contact ids.
    private HashMap<Long, Boolean> mExpandedEditors = new HashMap<Long, Boolean>();

    /**
     * The raw contact for which we started "take photo" or "choose photo from gallery" most
     * recently.  Used to restore {@link #mCurrentPhotoHandler} after orientation change.
     */
    private long mRawContactIdRequestingPhoto;

    /**
     * The {@link PhotoHandler} for the photo editor for the {@link #mRawContactIdRequestingPhoto}
     * raw contact.
     *
     * A {@link PhotoHandler} is created for each photo editor in {@link #bindPhotoHandler}, but
     * the only "active" one should get the activity result.  This member represents the active
     * one.
     */
    private PhotoHandler mCurrentPhotoHandler;
    private Uri mCurrentPhotoUri;

    // add by y.haiyang for i99 (start)
    private static final boolean I99 = true;
    public static final int REQUEST_CODE_I99_PHOTO = 9001;
    public static final String I99_ACTION_CAMERA = "cappu.action.camera";
    public static final String I99_ACTION_GALLERY = "cappu.action.gallery";
    public static final String I99_ACTION_NOTMAL = "cappu.action.normal";
    private boolean isGoPhoto = false;
    private TopBar mTopBar = null;
    // add by y.haiyang for i99 (end)
    public ContactEditorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        Log.i(TAG, "[onCreateView].");
        final View view = inflater.inflate(R.layout.contact_editor_fragment, container, false);

        mContent = (LinearLayout) view.findViewById(R.id.editors);

		//wangcunxi modify begin Care_os
        mTopBar = (TopBar)view.findViewById(R.id.editor_top_bar);
        mTopBar.setOnTopBarListener(mTopBarListener);
        android.graphics.drawable.Drawable  currentIcon = getResources().getDrawable(R.drawable.i99_icon_ok);  
        mTopBar.setRightDrawable(currentIcon);
        // setHasOptionsMenu(true);
        //wangcunxi modify end


        return view;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.i(TAG, "[onCreate].");
        if (savedState != null) {
            mExpandedEditors = (HashMap<Long, Boolean>)
                    savedState.getSerializable(KEY_EXPANDED_EDITORS);
            mRawContactIdRequestingPhoto = savedState.getLong(
                    KEY_RAW_CONTACT_ID_REQUESTING_PHOTO);
            mCurrentPhotoUri = savedState.getParcelable(KEY_CURRENT_PHOTO_URI);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "[onStop]");

        // add by y.haiyang for i99 (start)
        if(I99 && isGoPhoto){
            isGoPhoto = false;
            return;
        }
        // add by y.haiyang for i99 (end)

        // If anything was left unsaved, save it now and return to the compact editor.
        if (!getActivity().isChangingConfigurations() && mStatus == Status.EDITING) {
            /// M: add for AAS
            if (ExtensionManager.getInstance().getAasExtension().shouldStopSave(
                            mSubsciberAccount.isIccAccountType(mState))) {
                Log.w(TAG, "[onStop],AAS plugin,return.");
                return;
            }
            Log.d(TAG, "[onStop],change SaveMode = 4");
			
            //save(SaveMode.COMPACT, /* backPressed =*/ false); //yuan tong qin add 
        }
    }

    @Override
    public void onExternalEditorRequest(AccountWithDataSet account, Uri uri) {
        if (mListener != null) {
            mListener.onCustomEditContactActivityRequested(account, uri, null, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "[onOptionsItemSelected],change SaveMode = 4");
            return save(SaveMode.COMPACT, /* backPressed =*/ true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditorExpansionChanged() {
        Log.d(TAG, "[onEditorExpansionChanged]");
        updatedExpandedEditorsMap();
    }

    /**
     * Removes a current editor ({@link #mState}) and rebinds new editor for a new account.
     * Some of old data are reused with new restriction enforced by the new account.
     *
     * @param oldState Old data being edited.
     * @param oldAccount Old account associated with oldState.
     * @param newAccount New account to be used.
     */
    private void rebindEditorsForNewContact(
            RawContactDelta oldState, AccountWithDataSet oldAccount,
            AccountWithDataSet newAccount) {
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        AccountType oldAccountType = accountTypes.getAccountTypeForAccount(oldAccount);
        AccountType newAccountType = accountTypes.getAccountTypeForAccount(newAccount);

        if (newAccountType.getCreateContactActivityClassName() != null) {
            Log.w(TAG, "external activity called in rebind situation");
            if (mListener != null) {
                mListener.onCustomCreateContactActivityRequested(newAccount, mIntentExtras);
            }
        } else {
            mExistingContactDataReady = false;
            mNewContactDataReady = false;
            mState = new RawContactDeltaList();
            setStateForNewContact(newAccount, newAccountType, oldState, oldAccountType);
            if (mIsEdit) {
                /// M: need use isEditingUserProfile() instead of mIsUserProfile
                setStateForExistingContact(
                        mReadOnlyDisplayName, isEditingUserProfile(), mRawContacts);
            }
        }
    }

    @Override
    protected void setGroupMetaData() {
        if (mGroupMetaData == null) {
            return;
        }
        int editorCount = mContent.getChildCount();
        for (int i = 0; i < editorCount; i++) {
            BaseRawContactEditorView editor = (BaseRawContactEditorView) mContent.getChildAt(i);
            editor.setGroupMetaData(mGroupMetaData);
        }
    }

    @Override
    protected void bindEditors() {
        Log.d(TAG, "[bindEditors]");
        // bindEditors() can only bind views if there is data in mState, so immediately return
        // if mState is null
        if (mState.isEmpty()) {
            Log.w(TAG, "[bindEditors]mState is empty,return.");
            return;
        }

        // Check if delta list is ready.  Delta list is populated from existing data and when
        // editing an read-only contact, it's also populated with newly created data for the
        // blank form.  When the data is not ready, skip. This method will be called multiple times.
        if ((mIsEdit && !mExistingContactDataReady) || (mHasNewContact && !mNewContactDataReady)) {
            Log.w(TAG, "[bindEditors], delta list is not ready,return.");
            return;
        }

        // Sort the editors
        Collections.sort(mState, mComparator);

        // Remove any existing editors and rebuild any visible
        mContent.removeAllViews();

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        int numRawContacts = mState.size();

        for (int i = 0; i < numRawContacts; i++) {
            // TODO ensure proper ordering of entities in the list
            final RawContactDelta rawContactDelta = mState.get(i);
            if (!rawContactDelta.isVisible()) continue;

            final AccountType type = rawContactDelta.getAccountType(accountTypes);
			final String accountType = type.accountType;//wangcunxi add
            final long rawContactId = rawContactDelta.getRawContactId();

            final BaseRawContactEditorView editor;
            if (!type.areContactsWritable()) {
                editor = (BaseRawContactEditorView) inflater.inflate(
                        R.layout.raw_contact_readonly_editor_view, mContent, false);
            } else {
                //wangcunxi modify begin Care_os
                //editor = (RawContactEditorView) inflater.inflate(R.layout.raw_contact_editor_view,
                //        mContent, false);
                editor = (RawContactEditorView) inflater.inflate(R.layout.care_raw_contact_editor_view,
                        mContent, false);
                //wangcunxi modify end
            }
            /// M: If sim type, disable photo editor's triangle affordance.
            mSubsciberAccount.disableTriangleAffordance(editor, mState);

            editor.setListener(this);
            final List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(mContext)
                    .getAccounts(true);
            /// M: need use isEditingUserProfile() instead of mNewLocalProfile
            if (mHasNewContact && !isEditingUserProfile() && accounts.size() > 1) {
                addAccountSwitcher(mState.get(0), editor);
            } else {
                /// M: Bug fix, need dismiss the last account switcher pop up.
                mSubsciberAccount.dismissAccountSwitcherPopup();
            }

            editor.setEnabled(isEnabled());

            if (mExpandedEditors.containsKey(rawContactId)) {
                editor.setCollapsed(mExpandedEditors.get(rawContactId));
            } else {
                // By default, only the first editor will be expanded.
                editor.setCollapsed(i != 0);
            }

            /** M: AAS&SNE ensure phone kind updated and exists @{ */
            ExtensionManager.getInstance().getAasExtension().ensurePhoneKindForEditor(type,
                    mSubsciberAccount.getSubId(), rawContactDelta);
            ExtensionManager.getInstance().getSneExtension().onEditorBindEditors(rawContactDelta,
                    type, mSubsciberAccount.getSubId());
            /** @} */

            mContent.addView(editor);

            editor.setState(rawContactDelta, type, mViewIdGenerator, isEditingUserProfile());
            editor.setCollapsible(numRawContacts > 1);

            // Set up the photo handler.
            bindPhotoHandler(editor, type, mState);

            // If a new photo was chosen but not yet saved, we need to update the UI to
            // reflect this.
            final Uri photoUri = updatedPhotoUriForRawContact(rawContactId);
            if (photoUri != null) editor.setFullSizedPhoto(photoUri);

            //wangcunxi add begin Care_os(SIM Contacts dont display photo)
            if(AccountTypeUtils.isAccountTypeIccCard(accountType)){
                editor.setHasPhotoEditor(false);
            }
            //wangcunxi add end
            if (editor instanceof RawContactEditorView) {
                final Activity activity = getActivity();
                final RawContactEditorView rawContactEditor = (RawContactEditorView) editor;
                final ValuesDelta nameValuesDelta = rawContactEditor.getNameEditor().getValues();
                final EditorListener structuredNameListener = new EditorListener() {

                    @Override
                    public void onRequest(int request) {
                        // Make sure the activity is running
                        if (activity.isFinishing()) {
                            return;
                        }
                        if (!isEditingUserProfile()) {
                            if (request == EditorListener.FIELD_CHANGED) {
                                if (!nameValuesDelta.isSuperPrimary()) {
                                    unsetSuperPrimaryForAllNameEditors();
                                    nameValuesDelta.setSuperPrimary(true);
                                }
                                acquireAggregationSuggestions(activity,
                                        rawContactEditor.getNameEditor().getRawContactId(),
                                        rawContactEditor.getNameEditor().getValues());
                            } else if (request == EditorListener.FIELD_TURNED_EMPTY) {
                                if (nameValuesDelta.isSuperPrimary()) {
                                    nameValuesDelta.setSuperPrimary(false);
                                }
                            }
                        }
                    }

                    @Override
                    public void onDeleteRequested(Editor removedEditor) {
                    }
                };

                final StructuredNameEditorView nameEditor = rawContactEditor.getNameEditor();
                nameEditor.setEditorListener(structuredNameListener);
                if (TextUtils.isEmpty(nameEditor.getDisplayName()) &&
                        !TextUtils.isEmpty(mReadOnlyDisplayName)) {
                    nameEditor.setDisplayName(mReadOnlyDisplayName);
                    mReadOnlyNameEditorView = nameEditor;
                }

                rawContactEditor.setAutoAddToDefaultGroup(mAutoAddToDefaultGroup);

                if (isAggregationSuggestionRawContactId(rawContactId)) {
                    acquireAggregationSuggestions(activity,
                            rawContactEditor.getNameEditor().getRawContactId(),
                            rawContactEditor.getNameEditor().getValues());
                }
            }
        }

        setGroupMetaData();

        // Show editor now that we've loaded state
        mContent.setVisibility(View.VISIBLE);

        // Refresh Action Bar as the visibility of the join command
        // Activity can be null if we have been detached from the Activity
        invalidateOptionsMenu();

        updatedExpandedEditorsMap();
    }

    private void unsetSuperPrimaryForAllNameEditors() {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View view = mContent.getChildAt(i);
            if (view instanceof RawContactEditorView) {
                final RawContactEditorView rawContactEditorView = (RawContactEditorView) view;
                final StructuredNameEditorView nameEditorView =
                        rawContactEditorView.getNameEditor();
                if (nameEditorView != null) {
                    final ValuesDelta valuesDelta = nameEditorView.getValues();
                    if (valuesDelta != null) {
                        valuesDelta.setSuperPrimary(false);
                    }
                }
            }
        }
    }

    @Override
    public String getDisplayName() {
        // Return the super primary name if it is non-empty
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View view = mContent.getChildAt(i);
            if (view instanceof RawContactEditorView) {
                final RawContactEditorView rawContactEditorView = (RawContactEditorView) view;
                final StructuredNameEditorView nameEditorView =
                        rawContactEditorView.getNameEditor();
                if (nameEditorView != null) {
                    final String displayName = nameEditorView.getDisplayName();
                    if (!TextUtils.isEmpty(displayName)) {
                        return displayName;
                    }
                }
            }
        }
        // Return the first non-empty name
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View view = mContent.getChildAt(i);
            if (view instanceof RawContactEditorView) {
                final RawContactEditorView rawContactEditorView = (RawContactEditorView) view;
                final StructuredNameEditorView nameEditorView =
                        rawContactEditorView.getNameEditor();
                if (nameEditorView != null) {
                    final String displayName = nameEditorView.getDisplayName();
                    if (!TextUtils.isEmpty(displayName)) {
                        return displayName;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getPhoneticName() {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View view = mContent.getChildAt(i);
            if (view instanceof RawContactEditorView) {
                final RawContactEditorView rawContactEditorView = (RawContactEditorView) view;
                final PhoneticNameEditorView phoneticNameEditorView =
                        (PhoneticNameEditorView) rawContactEditorView.getPhoneticNameEditor();
                if (phoneticNameEditorView != null) {
                    final String phoneticName = phoneticNameEditorView.getPhoneticName();
                    if (!TextUtils.isEmpty(phoneticName)) {
                        return phoneticName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Update the values in {@link #mExpandedEditors}.
     */
    private void updatedExpandedEditorsMap() {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View childView = mContent.getChildAt(i);
            if (childView instanceof BaseRawContactEditorView) {
                BaseRawContactEditorView childEditor = (BaseRawContactEditorView) childView;
                mExpandedEditors.put(childEditor.getRawContactId(), childEditor.isCollapsed());
            }
        }
    }

    /**
     * If we've stashed a temporary file containing a contact's new photo, return its URI.
     * @param rawContactId identifies the raw-contact whose Bitmap we'll try to return.
     * @return Uru of photo for specified raw-contact, or null
     */
    private Uri updatedPhotoUriForRawContact(long rawContactId) {
        return (Uri) mUpdatedPhotos.get(String.valueOf(rawContactId));
    }

    private void bindPhotoHandler(BaseRawContactEditorView editor, AccountType type,
            RawContactDeltaList state) {
        final int mode;
        final boolean showIsPrimaryOption;
        if (type.areContactsWritable()) {
            if (editor.hasSetPhoto()) {
                mode = PhotoActionPopup.Modes.WRITE_ABLE_PHOTO;
                showIsPrimaryOption = hasMoreThanOnePhoto();
            } else {
                mode = PhotoActionPopup.Modes.NO_PHOTO;
                showIsPrimaryOption = false;
            }
        } else if (editor.hasSetPhoto() && hasMoreThanOnePhoto()) {
            mode = PhotoActionPopup.Modes.READ_ONLY_PHOTO;
            showIsPrimaryOption = true;
        } else {
            // Read-only and either no photo or the only photo ==> no options
            editor.getPhotoEditor().setEditorListener(null);
            editor.getPhotoEditor().setShowPrimary(false);
            return;
        }
        final PhotoHandler photoHandler = new PhotoHandler(mContext, editor, mode, state);
        editor.getPhotoEditor().setEditorListener(
                (PhotoHandler.PhotoEditorListener) photoHandler.getListener());
        editor.getPhotoEditor().setShowPrimary(showIsPrimaryOption);

        // Note a newly created raw contact gets some random negative ID, so any value is valid
        // here. (i.e. don't check against -1 or anything.)
        if (mRawContactIdRequestingPhoto == editor.getRawContactId()) {
            mCurrentPhotoHandler = photoHandler;
        }
    }

    private void addAccountSwitcher(
            final RawContactDelta currentState, BaseRawContactEditorView editor) {
        /// M: Change AccountSwitcher: add for ICCAccountType. @{
        final AccountWithDataSet currentAccount;
        if (mSubsciberAccount.isIccAccountType(mState)) {
            currentAccount = new AccountWithDataSetEx(currentState.getAccountName(),
                    currentState.getAccountType(), currentState.getDataSet(),
                    mSubsciberAccount.getSubId());
        } else {
            currentAccount = new AccountWithDataSet(currentState.getAccountName(),
                    currentState.getAccountType(), currentState.getDataSet());
        }
        // @}
        final View accountView = editor.findViewById(R.id.account);
        final View anchorView = editor.findViewById(R.id.account_selector_container);
        if (accountView == null) {
            Log.w(TAG, "[addAccountSwitcher]accountView is null,return!");
            return;
        }
        anchorView.setVisibility(View.VISIBLE);
        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListPopupWindow popup = new ListPopupWindow(mContext, null);
                final AccountsListAdapter adapter =
                        new AccountsListAdapter(mContext,
                        AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, currentAccount);
                popup.setWidth(anchorView.getWidth());
                popup.setAnchorView(anchorView);
                popup.setAdapter(adapter);
                popup.setModal(true);
                popup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
                        UiClosables.closeQuietly(popup);
                        AccountWithDataSet newAccount = adapter.getItem(position);
                        if (!newAccount.equals(currentAccount)) {
                            /// M: Change feature: AccountSwitcher. @{
                            // If the new account is sim account, set the sim info firstly.
                            // Or need to clear sim info firstly.
                            if (mSubsciberAccount.setAccountSimInfo(currentState, newAccount,
                                    mCurrentPhotoHandler, mContext)) {
                                return;
                            }
                            // @}
                            mNewContactAccountChanged = true;
                            rebindEditorsForNewContact(currentState, currentAccount, newAccount);
                        }
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    protected boolean doSaveAction(int saveMode, boolean backPressed) {
        Log.d(TAG, "[doSaveAction]start ContactSaveService,saveMode = " + saveMode
                + ",backPressed = " + backPressed);
        // Save contact and reload the compact editor after saving.
        // Note, the full resolution photos Bundle must be passed to the ContactSaveService
        // and then passed along in the result Intent in order for the compact editor to
        // receive it, instead of mUpdatedPhotos being accessed directly in onSaveCompleted,
        // because we clear mUpdatedPhotos after starting the save service below.
        Intent intent = ContactSaveService.createSaveContactIntent(mContext, mState,
                SAVE_MODE_EXTRA_KEY, saveMode, isEditingUserProfile(),
                ((Activity) mContext).getClass(), ContactEditorActivity.ACTION_SAVE_COMPLETED,
                mUpdatedPhotos, backPressed);
        mContext.startService(intent);

        // Don't try to save the same photos twice.
        mUpdatedPhotos = new Bundle();

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_EXPANDED_EDITORS, mExpandedEditors);
        outState.putLong(KEY_RAW_CONTACT_ID_REQUESTING_PHOTO, mRawContactIdRequestingPhoto);
        outState.putParcelable(KEY_CURRENT_PHOTO_URI, mCurrentPhotoUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mStatus == Status.SUB_ACTIVITY) {
            Log.d(TAG, "[onActivityResult]mStatus changed as Status.EDITING,ori is SUB_ACTIVITY");
            mStatus = Status.EDITING;
        }
        // add by y.haiyang for i99 (start)
        if(I99 && resultCode ==  Activity.RESULT_OK && requestCode == REQUEST_CODE_I99_PHOTO){
            return;
        }
        // add by y.haiyang for i99 (end)
        // See if the photo selection handler handles this result.
        if (mCurrentPhotoHandler != null && mCurrentPhotoHandler.handlePhotoActivityResult(
                requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void joinAggregate(final long contactId) {
        Log.d(TAG, "[joinAggregate] start ContactSaveService,contactId = " + contactId);
        final Intent intent = ContactSaveService.createJoinContactsIntent(
                mContext, mContactIdForJoin, contactId, ContactEditorActivity.class,
                ContactEditorActivity.ACTION_JOIN_COMPLETED);
        mContext.startService(intent);
    }

    /**
     * Sets the photo stored in mPhoto and writes it to the RawContact with the given id
     */
    //wangcunxi modify begin Care_os
    //private void setPhoto(long rawContact, Bitmap photo, Uri photoUri) {
    private void setPhoto(long rawContact, Bitmap photo, Uri photoUri,String photoName) {

    //wangcunxi modify end
        BaseRawContactEditorView requestingEditor = getRawContactEditorView(rawContact);

        if (photo == null || photo.getHeight() <= 0 || photo.getWidth() <= 0) {
            // This is unexpected.
            Log.w(TAG, "Invalid bitmap passed to setPhoto()");
            return;
        }

        if (requestingEditor != null) {
            requestingEditor.setPhotoEntry(photo);
            // Immediately set all other photos as non-primary. Otherwise the UI can display
            // multiple photos as "Primary photo".
            for (int i = 0; i < mContent.getChildCount(); i++) {
                final View childView = mContent.getChildAt(i);
                if (childView instanceof BaseRawContactEditorView
                        && childView != requestingEditor) {
                    final BaseRawContactEditorView rawContactEditor
                            = (BaseRawContactEditorView) childView;
                    rawContactEditor.getPhotoEditor().setSuperPrimary(false);
                }
            }
        } else {
            Log.w(TAG, "The contact that requested the photo is no longer present.");
        }

        // For inserts where the raw contact ID is a negative number, we must clear any previously
        // saved full resolution photos under negative raw contact IDs so that the compact editor
        // will use the newly selected photo, instead of an old one.
        if (isInsert(getActivity().getIntent()) && rawContact < 0) {
            removeNewRawContactPhotos();
        }
        mUpdatedPhotos.putParcelable(String.valueOf(rawContact), photoUri);
        //wangcunxi add begin Care_os
        mUpdatedPhotos.putString("photo_key", photoName);
        //wangcunxi add end
    }

    /**
     * Finds raw contact editor view for the given rawContactId.
     */
    @Override
    protected View getAggregationAnchorView(long rawContactId) {
        BaseRawContactEditorView editorView = getRawContactEditorView(rawContactId);
        return editorView == null ? null : editorView.findViewById(R.id.anchor_view);
    }

    public BaseRawContactEditorView getRawContactEditorView(long rawContactId) {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View childView = mContent.getChildAt(i);
            if (childView instanceof BaseRawContactEditorView) {
                final BaseRawContactEditorView editor = (BaseRawContactEditorView) childView;
                if (editor.getRawContactId() == rawContactId) {
                    return editor;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if there is currently more than one photo on screen.
     */
    private boolean hasMoreThanOnePhoto() {
        int countWithPicture = 0;
        final int numEntities = mState.size();
        for (int i = 0; i < numEntities; i++) {
            final RawContactDelta entity = mState.get(i);
            if (entity.isVisible()) {
                final ValuesDelta primary = entity.getPrimaryEntry(Photo.CONTENT_ITEM_TYPE);
                if (primary != null && primary.getPhoto() != null) {
                    countWithPicture++;
                } else {
                    final long rawContactId = entity.getRawContactId();
                    final Uri uri = mUpdatedPhotos.getParcelable(String.valueOf(rawContactId));
                    if (uri != null) {
                        try {
                            mContext.getContentResolver().openInputStream(uri);
                            countWithPicture++;
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "[hasMoreThanOnePhoto]e = " + e);
                        }
                    }
                }

                if (countWithPicture > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Custom photo handler for the editor.  The inner listener that this creates also has a
     * reference to the editor and acts as an {@link EditorListener}, and uses that editor to hold
     * state information in several of the listener methods.
     */
    public final class PhotoHandler extends PhotoSelectionHandler {

        final long mRawContactId;
        private final BaseRawContactEditorView mEditor;
        private final PhotoActionListener mPhotoEditorListener;

        public PhotoHandler(Context context, BaseRawContactEditorView editor, int photoMode,
                RawContactDeltaList state) {
            super(context, editor.getPhotoEditor().getChangeAnchorView(), photoMode, false, state);
            mEditor = editor;
            mRawContactId = editor.getRawContactId();
            mPhotoEditorListener = new PhotoEditorListener();
        }

        @Override
        public PhotoActionListener getListener() {
            return mPhotoEditorListener;
        }

        @Override
        public void startPhotoActivity(Intent intent, int requestCode, Uri photoUri) {
            mRawContactIdRequestingPhoto = mEditor.getRawContactId();
            mCurrentPhotoHandler = this;
            Log.d(TAG, "[startPhotoActivity]status changed as SUB_ACTIVITY,ogi is:" + mStatus);
            mStatus = Status.SUB_ACTIVITY;
            mCurrentPhotoUri = photoUri;
            ContactEditorFragment.this.startActivityForResult(intent, requestCode);
        }

        // M: Remove contacts photo when switch account to SIM type.
        public void removePictureChosen() {
            mEditor.setFullSizedPhoto(null);
            mUpdatedPhotos.clear();
            Log.d(TAG, "[removePictureChosen]");
        }
        // @}

        private final class PhotoEditorListener extends PhotoSelectionHandler.PhotoActionListener
                implements EditorListener {

            @Override
            public void onRequest(int request) {
                if (!hasValidState()) return;
				//wangcunxi modify begin Care_os
                if (request == 200) {
                    onClick(mEditor.getPhotoEditor());
                }
				//wangcunxi modify end
                if (request == EditorListener.REQUEST_PICK_PHOTO) {
                    onClick(mEditor.getPhotoEditor());
                }
                if (request == EditorListener.REQUEST_PICK_PRIMARY_PHOTO) {
                    useAsPrimaryChosen();
                }
            }

            @Override
            public void onDeleteRequested(Editor removedEditor) {
                // The picture cannot be deleted, it can only be removed, which is handled by
                // onRemovePictureChosen()
            }

            /**
             * User has chosen to set the selected photo as the (super) primary photo
             */
            public void useAsPrimaryChosen() {
                // Set the IsSuperPrimary for each editor
                int count = mContent.getChildCount();
                for (int i = 0; i < count; i++) {
                    final View childView = mContent.getChildAt(i);
                    if (childView instanceof BaseRawContactEditorView) {
                        final BaseRawContactEditorView editor =
                                (BaseRawContactEditorView) childView;
                        final PhotoEditorView photoEditor = editor.getPhotoEditor();
                        photoEditor.setSuperPrimary(editor == mEditor);
                    }
                }
                bindEditors();
            }

            /**
             * User has chosen to remove a picture
             */
            @Override
            public void onRemovePictureChosen() {
                mEditor.setPhotoEntry(null);

                // Prevent bitmap from being restored if rotate the device.
                // (only if we first chose a new photo before removing it)
                mUpdatedPhotos.remove(String.valueOf(mRawContactId));
                bindEditors();
            }

            @Override
            public void onPhotoSelected(Uri uri) throws FileNotFoundException {
                final Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(mContext, uri);
                //wangcunxi modify begin Care_os
                //setPhoto(mRawContactId, bitmap, uri);
                setPhoto(mRawContactId, bitmap, uri,"");
                //wangcunxi modify end
                mCurrentPhotoHandler = null;
                bindEditors();
            }

            @Override
            public Uri getCurrentPhotoUri() {
                return mCurrentPhotoUri;
            }

            @Override
            public void onPhotoSelectionDismissed() {
                // Nothing to do.
            }
            // add by y.haiyang for i99 (start)
            @Override
            public void onI99Click() {
                Intent intent = new Intent(mContext,I99ContactHeaderActivity.class);
                startActivityForResult(intent ,REQUEST_CODE_I99_PHOTO);
                isGoPhoto = true ;
            }
            @Override
            public void onI99PhotoSelected(Uri uri,String photoName){
                try{
                    final Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(mContext, uri);
                    setPhoto(mRawContactId, bitmap, uri,photoName);
                    mCurrentPhotoHandler = null;
                if (!mState.isEmpty()) {
                    //mRequestFocus = true;
                    bindEditors();
                } else {
                    Log.e(TAG, "mState is null");
                }
                }catch(Exception e){
                    Log.e(TAG, "Exception==== is null");

                }
            }
        }
    }

    /// M: Add for SIM contacts feature
    @Override
    protected boolean doSaveSIMContactAction(int saveMode, boolean backPressed) {
        Log.d(TAG, "[doSaveSIMContactAction] saveMode = " + saveMode
                + ",backPressed = " + backPressed);
        saveToIccCard(mState, saveMode, backPressed, ((Activity) mContext).getClass());
        return true;
    }
    //wangcunxi add begin Care_OS
    private onTopBarListener mTopBarListener = new onTopBarListener(){

        public void onLeftClick(View v){
            hideKeyBoard();
            //doRevertAction(); //dengying del
            //revert();
            
            //START: added by Yar @20170811
            onCancelEditConfirmed();
            mSubsciberAccount.setIsJoin(false);
            //END: added by Yar @20170811
            //getActivity().finish();
            
            //showDialogConfirmSave();
        }
        public void onRightClick(View v){
            //doSaveAction();//dengying del
        	//save(SaveMode.COMPACT,false);//dengying add
        	
    	    //wangcunxi add begin Cnext_OS
            if(isNameEmpty() || isNumberEmpty()){
                showConfigureDialog();
             }else if(isTooLong()){
            	 showConfigureLengthDialog();
             }else{
                 showDialogConfirmSave();
             		//save(SaveMode.CLOSE, /* backPressed =*/ false);
             }
           //showDialogConfirmSave();
        	
        	//save(SaveMode.CLOSE, /* backPressed =*/ false);
        	//doSaveAction();
        	//getActivity().finish();//dengying add
        }
        public void onTitleClick(View v){
            
        }
    };

    
    @Override
    public void updateTitle(String title){//dengying add
        mTopBar.setText(title);
    }
    
    private void hideKeyBoard(){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    /*
        * at save name and number cannt isEmpty
        */
    private boolean isNameEmpty(){
       if (mContent.getChildAt(0) instanceof RawContactEditorView) {
           RawContactEditorView editor = (RawContactEditorView) mContent.getChildAt(0);
           //wangcunxi modify begin Care_os(name is empty)
           //return editor.getNameEditor().isEmpty();
           return editor.getNameEditor().isEmpty() || editor.getNameEditor().getNameEmpty();
           //wangcunxi modify end
       }
       return false;
   }
    
	private boolean isTooLong() {
		if (mContent.getChildAt(0) instanceof RawContactEditorView) {
			RawContactEditorView editor = (RawContactEditorView) mContent.getChildAt(0);
			if ((editor.getNameEditor().getDisplayName().length()) > 15) {
				return true;
			}
		}
		return false;
	}
    
   public void showConfigureLengthDialog(){
        CareDialog dialog = new CareDialog.Builder(getActivity())
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setTitle(R.string.no_name_number_warning)
        .setMessage(R.string.contact_name_too_long)
        .setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int whichButton) {
                }
            }
        )
        .create();
        dialog.show();
   }	
	
	
   private boolean isNumberEmpty(){
       if (mContent.getChildAt(0) instanceof RawContactEditorView) {
           RawContactEditorView editor = (RawContactEditorView) mContent.getChildAt(0);
           
           return editor.getEditor(Phone.CONTENT_ITEM_TYPE).isEmpty();
       }
       return false;
   }
   public void showConfigureDialog(){
        CareDialog dialog = new CareDialog.Builder(getActivity())
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setTitle(R.string.no_name_number_warning)
        .setMessage(R.string.contact_no_name_number)
        .setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int whichButton) {
                    hideKeyBoard();
                    revert();
                    //doRevertAction();//dengying del
                }
            }
        )
        .setNegativeButton(android.R.string.cancel, null)
        .create();
        dialog.show();
   }

   public void showDialogConfirmSave(){
           CareDialog dialog = new CareDialog.Builder(getActivity())
           .setIconAttribute(android.R.attr.alertDialogIcon)
           .setTitle(R.string.i99_save_contact)
           .setMessage(R.string.i99_confirm_save_contact)
           .setPositiveButton(android.R.string.ok,
               new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int whichButton) {
                       //LogUtils.i(TAG, "[doSaveAction]");
                        /*if (mSubsciberAccount.isAccountTypeIccCard(mState)) {
                            //saveToIccCard(mState, SaveMode.CLOSE);//dengying del
                        } else {
                            //LogUtils.i(TAG, "save phone");
                            //save(SaveMode.CLOSE);//dengying del
                           }*/
                	   
                	   save(SaveMode.CLOSE, /* backPressed =*/ false);
                   }
               }
           )
           .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int whichButton) {
                       //doRevertAction();//dengying del
                       getActivity().finish();
                   }
               })
           .create();
           dialog.show();
      }
    //wangcunxi add end
}
