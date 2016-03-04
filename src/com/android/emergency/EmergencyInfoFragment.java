/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.emergency;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.view.AbsSavedState;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays health information and emergency contacts.
 * Takes in boolean readOnly to determine whether or not to allow information to be edited.
 */
public class EmergencyInfoFragment extends PreferenceFragment {

    /** Result code for contact picker */
    private static final int CONTACT_PICKER_RESULT = 1001;

    /** Key for description preference */
    private static final String KEY_DESCRIPTION = "description";

    /** Key for emergency contacts preference */
    private static final String KEY_EMERGENCY_CONTACTS = "emergency_contacts";

    /** Key for the add contact preference */
    private static final String KEY_ADD_CONTACT = "add_contact";

    /** Key to look up whether or not the fragment should be read only from the bundle */
    private static final String KEY_READ_ONLY = "read_only";

    /** Keys for all editable preferences- used to set up bindings */
    private static final String[] PREFERENCE_KEYS = {"name", "address", "date_of_birth",
            "blood_type", "allergies", "medications", "medical_conditions", "organ_donor"};

    /** Key to store the dialog bundle to choose or create a new contact. */
    private static final String KEY_CHOOSE_OR_CREATE_CONTACT_DIALOG =
            "choose_or_create_contact_dialog";

    /** Whether or not this fragment should be read only */
    private boolean mReadOnly;

    /** A list with all the preferences that are always present (in view and edit mode). */
    private final List<Preference> mPreferences = new ArrayList<Preference>();

    /** The category that holds the emergency contacts. */
    private EmergencyContactsPreference mEmergencyContactsPreferenceCategory;

    /** Choose or create contact dialog, non-null when opened. */
    private Dialog mChooseOrCreateContactDialog;
    /**
     * Creates a new EmergencyInfoFragment that can be used to edit user info if {@code readOnly}
     * is false. Otherwise, it provides a non-editable view of the emergency info.
     */
    public static EmergencyInfoFragment createEmergencyInfoFragment(boolean readOnly) {
        Bundle emergencyInfoArgs = new Bundle();
        emergencyInfoArgs.putBoolean(KEY_READ_ONLY, readOnly);
        EmergencyInfoFragment emergencyInfoFragment = new EmergencyInfoFragment();
        emergencyInfoFragment.setArguments(emergencyInfoArgs);
        return emergencyInfoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.emergency_info);
        mReadOnly = getArguments().getBoolean(KEY_READ_ONLY);
        if (mReadOnly) {
            Preference description = findPreference(KEY_DESCRIPTION);
            getPreferenceScreen().removePreference(description);
        }

        mEmergencyContactsPreferenceCategory = (EmergencyContactsPreference)
                findPreference(KEY_EMERGENCY_CONTACTS);
        mEmergencyContactsPreferenceCategory.setReadOnly(mReadOnly);

        for (String preferenceKey : PREFERENCE_KEYS) {
            Preference preference = findPreference(preferenceKey);
            mPreferences.add(preference);
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    MetricsLogger.action(preference.getContext(),
                            MetricsEvent.ACTION_EDIT_EMERGENCY_INFO_FIELD, preference.getKey());
                    return true;
                }
            });

            if (mReadOnly) {
                preference.setEnabled(false);
                preference.setShouldDisableView(false);
                preference.setSelectable(false);
            }
        }

        Preference addEmergencyContact = findPreference(KEY_ADD_CONTACT);
        if (mReadOnly) {
            getPreferenceScreen().removePreference(addEmergencyContact);
        } else {
            addEmergencyContact.setOnPreferenceClickListener(new Preference
                    .OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showChooseOrCreateContactDialog(null);
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Preference preference : mPreferences) {
            if (preference instanceof ReloadablePreferenceInterface) {
                ((ReloadablePreferenceInterface) preference).reloadFromPreference();
            }
        }
        mEmergencyContactsPreferenceCategory.reloadFromPreference();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_PICKER_RESULT && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            mEmergencyContactsPreferenceCategory.addNewEmergencyContact(uri);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mChooseOrCreateContactDialog == null || !mChooseOrCreateContactDialog.isShowing()) {
            return;
        }

        final SavedState myState = new SavedState(savedInstanceState);
        myState.isDialogShowing = true;
        myState.dialogBundle = mChooseOrCreateContactDialog.onSaveInstanceState();
        savedInstanceState.putParcelable(KEY_CHOOSE_OR_CREATE_CONTACT_DIALOG, myState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            SavedState myState =
                    (SavedState) savedInstanceState.get(KEY_CHOOSE_OR_CREATE_CONTACT_DIALOG);
            if (myState != null && myState.isDialogShowing) {
                showChooseOrCreateContactDialog(myState.dialogBundle);
            }
        }
    }

    private void showChooseOrCreateContactDialog(Bundle state) {
        AlertDialog.Builder chooseOrCreateDialogBuilder =
                new AlertDialog.Builder(getContext());
        chooseOrCreateDialogBuilder.setTitle(getContext()
                .getString(R.string.add_emergency_contact));
        String[] chooseOrCreate =
                getContext().getResources()
                        .getStringArray(R.array.choose_create_contact_values);
        chooseOrCreateDialogBuilder.setItems(chooseOrCreate,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                // Choose an existing contact
                                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                        ContactsContract.Contacts.CONTENT_URI);
                                startActivityForResult(contactPickerIntent,
                                        CONTACT_PICKER_RESULT);
                                break;
                            case 1:
                                // Create a new contact
                                Intent createContactIntent = new Intent(Intent.ACTION_INSERT);
                                createContactIntent
                                        .setType(ContactsContract.Contacts.CONTENT_TYPE);
                                // Fix for 4.0.3 +
                                createContactIntent
                                        .putExtra("finishActivityOnSaveCompleted", true);
                                startActivityForResult(createContactIntent,
                                        CONTACT_PICKER_RESULT);
                                break;
                        }
                    }
                });
        Dialog dialog = mChooseOrCreateContactDialog = chooseOrCreateDialogBuilder.create();
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mChooseOrCreateContactDialog = null;
            }
        });
        dialog.show();
    }

    private static class SavedState extends AbsSavedState {
        boolean isDialogShowing;
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
