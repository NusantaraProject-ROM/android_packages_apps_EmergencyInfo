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
package com.android.emergency.edit;

import android.app.Fragment;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.preferences.EmergencyContactsPreference;

import java.util.ArrayList;

/**
 * Tests for {@link EditInfoActivity}
 */
public class EditInfoActivityTest extends ActivityInstrumentationTestCase2<EditInfoActivity> {
    private ArrayList<Pair<String, Fragment>> mFragments;
    private EditEmergencyInfoFragment mEditEmergencyInfoFragment;
    private EditEmergencyContactsFragment mEditEmergencyContactsFragment;

    public EditInfoActivityTest() {
        super(EditInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().apply();

        mFragments = getActivity().getFragments();
        mEditEmergencyInfoFragment = (EditEmergencyInfoFragment) mFragments.get(0).second;
        mEditEmergencyContactsFragment = (EditEmergencyContactsFragment) mFragments.get(1).second;
    }

    public void testTwoFragments() {
        assertEquals(2, mFragments.size());
    }

    public void testEditEmergencyInfoFragmentInitialState() {
        for (String key : PreferenceKeys.KEYS_EDIT_EMERGENCY_INFO) {
            assertNotNull(mEditEmergencyInfoFragment.findPreference(key));
        }
    }

    public void testEditEmergencyContactFragmentInitialState() {
        EmergencyContactsPreference emergencyContactsPreference =
                (EmergencyContactsPreference) mEditEmergencyContactsFragment
                        .findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);
        assertNotNull(emergencyContactsPreference);
        assertEquals(0, emergencyContactsPreference.getPreferenceCount());
    }

    // TODO: test dialogs and clear all
}
