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
package com.android.emergency.view;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.android.emergency.EmergencyContactManager;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.preferences.EmergencyContactsPreference;

import java.util.Collections;

/**
 * Fragment that displays emergency contacts.
 */
public class ViewEmergencyContactsFragment extends PreferenceFragment {
    /** The category that holds the emergency contacts. */
    private EmergencyContactsPreference mEmergencyContactsPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.view_emergency_contacts);
        mEmergencyContactsPreference = (EmergencyContactsPreference)
                findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);
    }

    @Override
    public void onResume() {
        super.onResume();
        mEmergencyContactsPreference.reloadFromPreference();
    }

    public static Fragment newInstance() {
        return new ViewEmergencyContactsFragment();
    }

    /** Returns true if there is at least one valid (still existing) emergency contact. */
    public static boolean hasAtLeastOneEmergencyContact(Context context) {
        for (String uriString : PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PreferenceKeys.KEY_EMERGENCY_CONTACTS,
                        Collections.<String>emptySet())) {
            if (EmergencyContactManager.isValidEmergencyContact(context, Uri.parse(uriString))) {
                return true;
            }
        }
        return false;
    }
}
