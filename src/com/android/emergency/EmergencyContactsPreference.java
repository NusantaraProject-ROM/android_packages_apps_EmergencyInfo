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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.ArraySet;
import android.util.AttributeSet;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Custom {@link PreferenceCategory} that deals with contacts being deleted from the contacts app.
 */
public class EmergencyContactsPreference extends PreferenceCategory
        implements ReloadablePreferenceInterface,
        ContactPreference.RemoveContactPreferenceListener {

    private boolean mReadOnly = false;

    private Set<Uri> mEmergencyContacts = new ArraySet<Uri>();

    public EmergencyContactsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Sets whether this category will be used in read only mode or in edit mode. */
    public void setReadOnly(boolean readOnly) {
        mReadOnly = readOnly;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setEmergencyContacts(restorePersistedValue ?
                getPersistedEmergencyContacts() :
                deserializeAndFilterExisting((Set<String>) defaultValue));
    }

    @Override
    public void reloadFromPreference() {
        setEmergencyContacts(getPersistedEmergencyContacts());
    }

    @Override
    public void onRemoveContactPreference(ContactPreference contactPreference) {
        Uri newContact = contactPreference.getContactUri();
        if (mEmergencyContacts.contains(newContact)) {
            Set<Uri> updatedContacts = new ArraySet<Uri>((ArraySet<Uri>) mEmergencyContacts);
            if (updatedContacts.remove(newContact) && callChangeListener(updatedContacts)) {
                MetricsLogger.action(getContext(), MetricsEvent.ACTION_DELETE_EMERGENCY_CONTACT);
                setEmergencyContacts(updatedContacts);
            }
        }
    }

    /** Adds a new emergency contact. */
    public void addNewEmergencyContact(Uri contactUri) {
        if (EmergencyContactManager.getPhoneNumbers(getContext(), contactUri) == null) {
            // TODO: show warning dialog: no phone number: then keep/discard
        } else {
            if (!mEmergencyContacts.contains(contactUri)) {
                Set<Uri> updatedContacts = new ArraySet<Uri>((ArraySet<Uri>) mEmergencyContacts);
                if (updatedContacts.add(contactUri) && callChangeListener(updatedContacts)) {
                    MetricsLogger.action(getContext(), MetricsEvent.ACTION_ADD_EMERGENCY_CONTACT);
                    setEmergencyContacts(updatedContacts);
                }
            }
        }
    }

    private void setEmergencyContacts(Set<Uri> emergencyContacts) {
        mEmergencyContacts = emergencyContacts;
        persistEmergencyContacts(emergencyContacts);
        notifyChanged();
        removeAll();

        for (Uri contactUri : emergencyContacts) {
            addContactPreference(contactUri);
        }
    }

    private void addContactPreference(Uri contactUri) {
        final ContactPreference contactPreference =
                new ContactPreference(getContext(),
                        contactUri,
                        EmergencyContactManager.getName(getContext(), contactUri));
        contactPreference
                .setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                if (mReadOnly) {
                                    contactPreference.showCallContactDialog();
                                } else {
                                    contactPreference.displayContact();
                                }
                                return true;
                            }
                        }
                );
        contactPreference.setRemoveContactPreferenceListener(mReadOnly ? null : this);
        addPreference(contactPreference);
    }

    private Set<Uri> getPersistedEmergencyContacts() {
        return deserializeAndFilterExisting(
                getPersistedStringSet(Collections.<String>emptySet()));
    }

    /**
     * Converts the strings to Uris and only keeps those corresponding to still existing contacts.
     */
    private Set<Uri> deserializeAndFilterExisting(Set<String> emergencyContactStrings) {
        Set<Uri> emergencyContacts = new ArraySet<Uri>(emergencyContactStrings.size());
        for (String emergencyContact : emergencyContactStrings) {
            Uri contactUri = Uri.parse(emergencyContact);
            if (EmergencyContactManager.isValidEmergencyContact(getContext(), contactUri)) {
                emergencyContacts.add(contactUri);
            }
        }

        // If not all contacts were added, then we need to overwrite the emergency contacts stored
        // in shared preferences. This deals with emergency contacts being deleted from contacts:
        // currently we have no way to being notified when this happens.
        if (emergencyContacts.size() != emergencyContactStrings.size()) {
            persistEmergencyContacts(emergencyContacts);
        }

        return emergencyContacts;
    }

    private void persistEmergencyContacts(Set<Uri> emergencyContacts) {
        Set<String> emergencyContactStrings = serialize(emergencyContacts);
        persistStringSet(emergencyContactStrings);
    }

    /** Converts the Uris to their string representation. */
    private Set<String> serialize(Set<Uri> emergencyContacts) {
        Set<String> emergencyContactStrings = new ArraySet<String>(emergencyContacts.size());
        for (Uri uri : emergencyContacts) {
            emergencyContactStrings.add(uri.toString());
        }
        return emergencyContactStrings;
    }
}
