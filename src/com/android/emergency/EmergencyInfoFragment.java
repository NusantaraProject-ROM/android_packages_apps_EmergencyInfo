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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.Set;

/**
 * Fragment that displays health information and emergency contacts.
 * Takes in boolean readOnly to determine whether or not to allow information to be edited.
 */
public class EmergencyInfoFragment extends PreferenceFragment
        implements ContactPreference.DeleteContactListener {

    /** Result code for contact picker */
    private static final int CONTACT_PICKER_RESULT = 1001;

    /** Request code for runtime contacts permission */
    private static final int PERMISSION_REQUEST = 1002;

    /** Key for description preference */
    private static final String DESCRIPTION_KEY = "description";

    /** Key for emergency contacts preference */
    private static final String EMERGENCY_CONTACTS_KEY = "emergency_contacts";

    /** Key to look up whether or not the fragment should be read only from the bundle */
    private static final String READ_ONLY_KEY = "read_only";

    /** Key for date of birth preference */
    private static final String DATE_OF_BIRTH_KEY = "date_of_birth";

    /** Keys for all editable preferences- used to set up bindings */
    private static final String[] PREFERENCE_KEYS = {"name", "address", DATE_OF_BIRTH_KEY,
            "blood_type", "allergies", "medications", "medical_conditions", "organ_donor"};

    /** Whether or not this fragment should be read only */
    private boolean mReadOnly;

    /** SharedPreferences- initialized in onCreate */
    private SharedPreferences mSharedPreferences = null;

    /** Emergency contact manager that handles adding an removing emergency contacts. */
    private EmergencyContactManager mEmergencyContactManager;

    /** Reference to the preferenceScreen controlled by this fragment */
    private PreferenceScreen mPreferenceScreen;

    /**
     * Creates a new EmergencyInfoFragment that can be used to edit user info if {@code readOnly}
     * is false. Otherwise, it provides a non-editable view of the emergency info.
     */
    public static EmergencyInfoFragment createEmergencyInfoFragment(boolean readOnly) {
        Bundle emergencyInfoArgs = new Bundle();
        emergencyInfoArgs.putBoolean(READ_ONLY_KEY, readOnly);
        EmergencyInfoFragment emergencyInfoFragment = new EmergencyInfoFragment();
        emergencyInfoFragment.setArguments(emergencyInfoArgs);
        return emergencyInfoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emergency_info_layout, container, false);
        FloatingActionButton editInfoButton =
                (FloatingActionButton) view.findViewById(R.id.fab);
        // The button is the entry point from ViewInfoActivity to EditInfoActivity.
        if (!mReadOnly) {
            editInfoButton.setVisibility(View.GONE);
        } else {
            editInfoButton.setVisibility(View.VISIBLE);
            editInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                    startActivity(intent);
                }
            });
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.emergency_info);

        mReadOnly = getArguments().getBoolean(READ_ONLY_KEY);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEmergencyContactManager = new EmergencyContactManager(getContext(), mSharedPreferences,
                EMERGENCY_CONTACTS_KEY);
        mPreferenceScreen = getPreferenceScreen();

        for (String preferenceKey : PREFERENCE_KEYS) {
            Preference preference = findPreference(preferenceKey);
            if (!preferenceKey.equals(DATE_OF_BIRTH_KEY)) {
                bindPreferenceSummaryToValue(preference);
            }
            if (mReadOnly) {
                preference.setEnabled(false);
                preference.setShouldDisableView(false);
                preference.setSelectable(false);
            }
        }
        populateEmergencyContacts();
        if (mReadOnly) {
            Preference description = findPreference(DESCRIPTION_KEY);
            mPreferenceScreen.removePreference(description);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_PICKER_RESULT && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            mEmergencyContactManager.addContact(uri);
            MetricsLogger.action(getContext(), MetricsEvent.ACTION_ADD_EMERGENCY_CONTACT);
            populateEmergencyContacts();
            if (EmergencyContactManager.getPhoneNumbers(getContext(), uri) == null) {
                // TODO: show warning dialog: no phone number
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateEmergencyContacts();
            } else {
                mPreferenceScreen.removePreference(findPreference(EMERGENCY_CONTACTS_KEY));
            }
        }
    }

    @Override
    public void onContactDelete(Uri contactUri) {
        mEmergencyContactManager.removeContact(contactUri);
        MetricsLogger.action(getContext(), MetricsEvent.ACTION_DELETE_EMERGENCY_CONTACT);
        populateEmergencyContacts();
    }

    private static final Preference.OnPreferenceChangeListener
            sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            MetricsLogger.action(preference.getContext(),
                    MetricsEvent.ACTION_EDIT_EMERGENCY_INFO_FIELD, preference.getKey());
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                if (index >= 0) {
                    preference.setSummary(listPreference.getEntries()[index]);
                }
            } else {
                if (!stringValue.isEmpty()) {
                    preference.setSummary(stringValue);
                } else {
                    preference.setSummary(preference.getContext()
                            .getResources().getString(R.string.default_summary_none));
                }
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                mSharedPreferences.getString(preference.getKey(), ""));
    }

    private void populateEmergencyContacts() {
        PreferenceCategory emergencyContactsCategory =
                (PreferenceCategory) findPreference(EMERGENCY_CONTACTS_KEY);
        // TODO: Use a list adapter instead of removing all each time.
        emergencyContactsCategory.removeAll();
        Set<Uri> emergencyContacts = mEmergencyContactManager.getEmergencyContacts();

        if (!emergencyContacts.isEmpty()) {
            // Get permission if necessary, else populate emergency contacts list
            boolean hasContactsPermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
            boolean hasCallPermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
            if (!hasContactsPermission || !hasCallPermission) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST);
            } else {
                for (Uri contactUri : emergencyContacts) {
                    final ContactPreference contactPreference =
                            new ContactPreference(getContext(),
                                    contactUri,
                                    EmergencyContactManager.getName(getContext(), contactUri),
                                    mReadOnly ? null : this);
                    contactPreference.setOnPreferenceClickListener(
                            createContactPreferenceClickListener(contactPreference));
                    emergencyContactsCategory.addPreference(contactPreference);
                }
            }
        }

        if (!mReadOnly) {
            // If in edit mode, add a button to create a new emergency contact.
            emergencyContactsCategory.addPreference(createAddEmergencyContactPreference());
        } else if (emergencyContactsCategory.getPreferenceCount() == 0) {
            // If in view mode and there are no contacts, remove the section entirely.
            mPreferenceScreen.removePreference(emergencyContactsCategory);
        }
    }

    private Preference.OnPreferenceClickListener createContactPreferenceClickListener(
            final ContactPreference contactPreference) {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(mReadOnly) {
                    contactPreference.showCallContactDialog();
                } else {
                    contactPreference.displayContact();
                }
                return true;
            }
        };
    }


    /** Generates an add contact button */
    private Preference createAddEmergencyContactPreference() {
        Preference addEmergencyContact = new Preference(getContext());
        addEmergencyContact.setTitle(getString(R.string.add_emergency_contact));
        addEmergencyContact.setIcon(getResources().getDrawable(R.drawable.ic_menu_add_dark));
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
        return addEmergencyContact;
    }
}
