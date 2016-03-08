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

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays personal and medical information.
 */
public class EmergencyInfoFragment extends PreferenceFragment {

    /** Key for description preference */
    private static final String KEY_DESCRIPTION = "description";

    /** Keys for all editable preferences- used to set up bindings */
    private static final String[] PREFERENCE_KEYS = {"name", "address", "date_of_birth",
            "blood_type", "allergies", "medications", "medical_conditions", "organ_donor"};

    private static final String ARG_VIEW_MODE = "view_mode";

    /** Whether or not this fragment should be in view mode */
    private boolean mInViewMode;

    /** A list with all the preferences that are always present (in view and edit mode). */
    private final List<Preference> mPreferences = new ArrayList<Preference>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.emergency_info);
        mInViewMode = getArguments().getBoolean(ARG_VIEW_MODE);

        if (mInViewMode) {
            Preference description = findPreference(KEY_DESCRIPTION);
            getPreferenceScreen().removePreference(description);
        }

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

            if (mInViewMode) {
                preference.setEnabled(false);
                preference.setShouldDisableView(false);
                preference.setSelectable(false);
            }
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
    }

    public static Fragment newInstance(boolean inViewMode) {
        Bundle emergencyInfoArgs = new Bundle();
        emergencyInfoArgs.putBoolean(ARG_VIEW_MODE, inViewMode);
        EmergencyInfoFragment emergencyInfoFragment = new EmergencyInfoFragment();
        emergencyInfoFragment.setArguments(emergencyInfoArgs);
        return emergencyInfoFragment;
    }
}
