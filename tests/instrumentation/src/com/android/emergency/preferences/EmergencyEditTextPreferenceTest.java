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
package com.android.emergency.preferences;

import static com.google.common.truth.Truth.assertThat;

import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.edit.EditMedicalInfoActivity;
import com.android.emergency.edit.EditMedicalInfoFragment;

/**
 * Tests for {@link EmergencyEditTextPreference}.
 */
@MediumTest
public class EmergencyEditTextPreferenceTest
        extends ActivityInstrumentationTestCase2<EditMedicalInfoActivity> {
    private EmergencyEditTextPreference mPreference;
    private EditMedicalInfoFragment mEditInfoFragment;

    public EmergencyEditTextPreferenceTest() {
        super(EditMedicalInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEditInfoFragment = getActivity().getFragment();
        mPreference = (EmergencyEditTextPreference)
                mEditInfoFragment.findPreference(PreferenceKeys.KEY_MEDICAL_CONDITIONS);
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPreference.setText("");
                }
            });
        } catch (Throwable throwable) {
            fail("Should not throw exception: " + throwable.getMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        super.tearDown();
    }

    public void testReloadFromPreference() throws Throwable {
        String medicalConditions = "Asthma";
        mEditInfoFragment.getPreferenceManager().getSharedPreferences().edit()
                .putString(mPreference.getKey(), medicalConditions).commit();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreference.reloadFromPreference();
            }
        });
        assertThat(mPreference.getText()).isEqualTo(medicalConditions);
        assertThat(mPreference.isNotSet()).isFalse();
    }
}
