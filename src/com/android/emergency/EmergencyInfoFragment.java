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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;

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

    /** Whether or not this fragment should be read only */
    private boolean mReadOnly;

    /** A list with all the preferences that are always present (in view and edit mode). */
    private final List<Preference> mPreferences = new ArrayList<Preference>();

    /** The category that holds the emergency contacts. */
    private EmergencyContactsPreference mEmergencyContactsPreferenceCategory;

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
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
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
}
