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
import com.android.emergency.R;
import com.android.emergency.preferences.BirthdayPreference;
import com.android.emergency.preferences.EmergencyContactsPreference;
import com.android.emergency.preferences.EmergencyEditTextPreference;
import com.android.emergency.preferences.EmergencyListPreference;
import com.android.emergency.preferences.NameAutoCompletePreference;

import java.util.ArrayList;

/**
 * Tests for {@link EditInfoActivity}.
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();


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

    public void testClearAllPreferences () throws Throwable {
        final NameAutoCompletePreference namePreference =
                (NameAutoCompletePreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_NAME);
        final BirthdayPreference dateOfBirthPreference =
                (BirthdayPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_DATE_OF_BIRTH);
        final EmergencyEditTextPreference addressPreference =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_ADDRESS);
        final EmergencyListPreference bloodTypePreference =
                (EmergencyListPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_BLOOD_TYPE);
        final EmergencyEditTextPreference allergiesPreference =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_ALLERGIES);
        final EmergencyEditTextPreference medicationsPreference =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_MEDICATIONS);
        final EmergencyEditTextPreference medicalConditionsPreference =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_MEDICAL_CONDITIONS);
        final EmergencyListPreference organDonorPreference =
                (EmergencyListPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_ORGAN_DONOR);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                namePreference.setText("John");
                dateOfBirthPreference.setDateOfBirth(123456L);
                addressPreference.setText("Home");
                bloodTypePreference.setValue("A+");
                allergiesPreference.setText("Peanuts");
                medicationsPreference.setText("Aspirin");
                medicalConditionsPreference.setText("Asthma");
                organDonorPreference.setValue("Yes");
            }
        });

        String unknownName = getActivity().getResources().getString(R.string.unknown_name);
        String unknownDateOfBirth =
                getActivity().getResources().getString(R.string.unknown_date_of_birth);
        String unknownAddress = getActivity().getResources().getString(R.string.unknown_address);
        String unknownBloodType =
                getActivity().getResources().getString(R.string.unknown_blood_type);
        String unknownAllergies =
                getActivity().getResources().getString(R.string.unknown_allergies);
        String unknownMedications =
                getActivity().getResources().getString(R.string.unknown_medications);
        String unknownMedicalConditions =
                getActivity().getResources().getString(R.string.unknown_medical_conditions);
        String unknownOrganDonor =
                getActivity().getResources().getString(R.string.unknown_organ_donor);

        assertNotSame(unknownName, namePreference.getSummary());
        assertNotSame(unknownDateOfBirth, dateOfBirthPreference.getSummary());
        assertNotSame(unknownAddress, addressPreference.getSummary());
        assertNotSame(unknownBloodType, bloodTypePreference.getSummary());
        assertNotSame(unknownAllergies, allergiesPreference.getSummary());
        assertNotSame(unknownMedications, medicationsPreference.getSummary());
        assertNotSame(unknownMedicalConditions, medicalConditionsPreference.getSummary());
        assertNotSame(unknownOrganDonor, organDonorPreference.getSummary());

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().onClearAllPreferences();
            }
        });
        getInstrumentation().waitForIdleSync();

        assertEquals(unknownName, namePreference.getSummary());
        assertEquals(unknownDateOfBirth, dateOfBirthPreference.getSummary());
        assertEquals(unknownAddress, addressPreference.getSummary());
        assertEquals(unknownBloodType, bloodTypePreference.getSummary().toString());
        assertEquals(unknownAllergies, allergiesPreference.getSummary());
        assertEquals(unknownMedications, medicationsPreference.getSummary());
        assertEquals(unknownMedicalConditions, medicalConditionsPreference.getSummary());
        assertEquals(unknownOrganDonor, organDonorPreference.getSummary());

        // TODO: Test that contacts are deleted as well

    }

    // TODO: test dialogs
}
