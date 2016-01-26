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
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.Collections;
import java.util.Set;

/**
 * Fragment that displays health information and emergency contacts.
 * Takes in boolean readOnly to determine whether or not to allow information to be edited.
 */
public class EmergencyInfoFragment extends PreferenceFragment {

    /** Result code for contact picker */
    private static final int CONTACT_PICKER_RESULT = 1001;

    /** Request code for runtime contacts permission */
    private static final int CONTACT_PERMISSION_REQUEST = 1002;

    /** Key for contact actions dialog */
    private static final String CONTACT_ACTIONS_DIALOG_KEY = "contact_actions";

    /** Key for description preference */
    private static final String DESCRIPTION_KEY = "description";

    /** Key for emergency contacts preference */
    private static final String EMERGENCY_CONTACTS_KEY = "emergency_contacts";

    /** Key to look up whether or not the fragment should be read only from the bundle */
    private static final String READ_ONLY_KEY = "read_only";

    /** Keys for all editable preferences- used to set up bindings */
    private static final String[] PREFERENCE_KEYS = {"name", "address", "blood_type", "allergies",
            "medications", "medical_conditions", "organ_donor"};

    /** Whether or not this fragment should be read only */
    private boolean mReadOnly;

    /** SharedPreferences- initialized in onCreate */
    private SharedPreferences mSharedPreferences = null;

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
                    // TODO: This actually then requires the user to unlock the phone (which is
                    // desired), but in two steps. Explore the possibility of doing it in one step.
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
        mPreferenceScreen = getPreferenceScreen();

        for (String preferenceKey : PREFERENCE_KEYS) {
            Preference preference = findPreference(preferenceKey);
            bindPreferenceSummaryToValue(preference);
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
            Uri result = data.getData();
            // Manipulate a copy of emergency contacts rather than editing directly- see
            // getEmergencyContacts for why this is necessary.
            Set<String> oldContacts = getEmergencyContacts();
            ArraySet<String> newContacts = new ArraySet<String>(oldContacts.size() + 1);
            newContacts.addAll(oldContacts);

            newContacts.add(result.toString());
            setEmergencyContacts(newContacts);

            MetricsLogger.action(getContext(), MetricsEvent.ACTION_ADD_EMERGENCY_CONTACT);
            populateEmergencyContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (requestCode == CONTACT_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateEmergencyContacts();
            } else {
                mPreferenceScreen.removePreference(findPreference(EMERGENCY_CONTACTS_KEY));
            }
        }
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
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
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
        emergencyContactsCategory.removeAll();
        Set<String> emergencyContacts = getEmergencyContacts();

        if (!emergencyContacts.isEmpty()) {
            // Get permission if necessary, else populate emergency contacts list
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        CONTACT_PERMISSION_REQUEST);
            } else {
                for (String contactUri : emergencyContacts) {
                    final ContactPreference contactPreference =
                            new ContactPreference(getContext(), contactUri);
                    contactPreference.setOnPreferenceClickListener(
                            createContactPreferenceClickListener(contactPreference));
                    emergencyContactsCategory.addPreference(contactPreference);
                }
            }
        }

        if (!mReadOnly) {
            // If in edit mode, add a button to create a new emergency contact.
            emergencyContactsCategory.addPreference(createAddEmergencyContactPreference());
        } else if (emergencyContacts.isEmpty()) {
            // If in view mode and there are no contacts, remove the section entirely.
            mPreferenceScreen.removePreference(emergencyContactsCategory);
        }
    }

    private Preference.OnPreferenceClickListener createContactPreferenceClickListener(
            final ContactPreference contactPreference) {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Uri contactUri = contactPreference.getUri();

                if (mReadOnly) {
                    // TODO: Call the contact instead of displaying a card.
                    contactPreference.displayContact();
                } else {
                    ContactActionsDialogFragment contactActionsDialogFragment =
                            new ContactActionsDialogFragment();
                    contactActionsDialogFragment.setTitle(contactPreference.getTitle());
                    contactActionsDialogFragment.setDialogActionCallback(
                            new ContactActionsDialogFragment.DialogActionCallback() {
                                @Override
                                public void onContactDelete() {
                                    // Manipulate a copy of emergency contacts rather than
                                    // editing directly- see getEmergencyContacts for why
                                    // this is necessary.
                                    Set<String> oldContacts = getEmergencyContacts();
                                    ArraySet<String> newContacts = new ArraySet<String>(
                                            oldContacts.size());

                                    newContacts.addAll(oldContacts);
                                    newContacts.remove(contactUri.toString());
                                    setEmergencyContacts(newContacts);
                                    MetricsLogger.action(getContext(),
                                            MetricsEvent.ACTION_DELETE_EMERGENCY_CONTACT);

                                    populateEmergencyContacts();
                                }

                                @Override
                                public void onContactDisplay() {
                                    contactPreference.displayContact();
                                }
                            });
                    contactActionsDialogFragment.show(getFragmentManager(),
                            CONTACT_ACTIONS_DIALOG_KEY);
                }
                return true;
            }
        };
    }

    /** Generates an add contact button */
    private Preference createAddEmergencyContactPreference() {
        Preference addEmergencyContact = new Preference(getContext());
        addEmergencyContact.setTitle(getString(R.string.add_emergency_contact));
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


    /**
     * Returns a Set of stored emergency contacts. If editing, make a copy of the set as
     * described by {@link SharedPreferences#getStringSet(String, Set<String>)}, then call
     * {@link #setEmergencyContacts(Set)} to store the new contact information.
     */
    private Set<String> getEmergencyContacts() {
        Set<String> emergencyContacts = mSharedPreferences
                .getStringSet(EMERGENCY_CONTACTS_KEY, Collections.<String>emptySet());
        return emergencyContacts;
    }

    private void setEmergencyContacts(Set<String> emergencyContacts) {
        mSharedPreferences.edit().putStringSet(EMERGENCY_CONTACTS_KEY, emergencyContacts)
                .commit();
    }
}
