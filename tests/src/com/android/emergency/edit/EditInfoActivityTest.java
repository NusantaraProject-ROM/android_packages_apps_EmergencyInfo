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

import static com.google.common.truth.Truth.assertThat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Pair;

import com.android.emergency.ContactTestUtils;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.preferences.EmergencyContactsPreference;
import com.android.emergency.preferences.EmergencyEditTextPreference;
import com.android.emergency.preferences.EmergencyListPreference;
import com.android.emergency.preferences.NameAutoCompletePreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link EditInfoActivity}.
 */
@LargeTest
public class EditInfoActivityTest extends ActivityInstrumentationTestCase2<EditInfoActivity> {
    private ArrayList<Pair<String, Fragment>> mFragments;
    private EditEmergencyInfoFragment mEditEmergencyInfoFragment;
    private EditEmergencyContactsFragment mEditEmergencyContactsFragment;
    private PowerManager.WakeLock mKeepScreenOnWakeLock;

    public EditInfoActivityTest() {
        super(EditInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        forceScreenOn();

        mFragments = getActivity().getFragments();
        mEditEmergencyInfoFragment = (EditEmergencyInfoFragment) mFragments.get(0).second;
        mEditEmergencyContactsFragment = (EditEmergencyContactsFragment) mFragments.get(1).second;
    }

    @Override
    protected void tearDown() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        releaseScreenOn();
        super.tearDown();
    }

    public void testTwoFragments() {
        assertThat(mFragments.size()).isEqualTo(2);
    }

    public void testInitialState() {
        for (String key : PreferenceKeys.KEYS_EDIT_EMERGENCY_INFO) {
            assertThat(mEditEmergencyInfoFragment.findPreference(key)).isNotNull();
        }
        EmergencyContactsPreference emergencyContactsPreference =
                (EmergencyContactsPreference) mEditEmergencyContactsFragment
                        .findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);
        assertThat(emergencyContactsPreference).isNotNull();
        assertThat(emergencyContactsPreference.getPreferenceCount()).isEqualTo(0);
    }

    public void testClearAllPreferences () throws Throwable {
        EditInfoActivity editInfoActivity = getActivity();
        final NameAutoCompletePreference namePreference =
                (NameAutoCompletePreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_NAME);
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

        final EmergencyContactsPreference emergencyContactsPreference =
                (EmergencyContactsPreference) mEditEmergencyContactsFragment
                        .findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);
        final Uri contactUri = ContactTestUtils
                .createContact(editInfoActivity.getContentResolver(), "Michael", "789");
        final List<Uri> emergencyContacts = new ArrayList<>();
        emergencyContacts.add(contactUri);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                namePreference.setText("John");
                addressPreference.setText("Home");
                bloodTypePreference.setValue("A+");
                allergiesPreference.setText("Peanuts");
                medicationsPreference.setText("Aspirin");
                medicalConditionsPreference.setText("Asthma");
                organDonorPreference.setValue("Yes");
                emergencyContactsPreference.setEmergencyContacts(emergencyContacts);
            }
        });

        String unknownName = editInfoActivity.getResources().getString(R.string.unknown_name);
        String unknownAddress = editInfoActivity.getResources().getString(R.string.unknown_address);
        String unknownBloodType =
                editInfoActivity.getResources().getString(R.string.unknown_blood_type);
        String unknownAllergies =
                editInfoActivity.getResources().getString(R.string.unknown_allergies);
        String unknownMedications =
                editInfoActivity.getResources().getString(R.string.unknown_medications);
        String unknownMedicalConditions =
                editInfoActivity.getResources().getString(R.string.unknown_medical_conditions);
        String unknownOrganDonor =
                editInfoActivity.getResources().getString(R.string.unknown_organ_donor);

        assertThat(namePreference.getSummary()).isNotEqualTo(unknownName);
        assertThat(addressPreference.getSummary()).isNotEqualTo(unknownAddress);
        assertThat(bloodTypePreference.getSummary()).isNotEqualTo(unknownBloodType);
        assertThat(allergiesPreference.getSummary()).isNotEqualTo(unknownAllergies);
        assertThat(medicationsPreference.getSummary()).isNotEqualTo(unknownMedications);
        assertThat(medicalConditionsPreference.getSummary()).isNotEqualTo(unknownMedicalConditions);
        assertThat(organDonorPreference.getSummary()).isNotEqualTo(unknownOrganDonor);
        assertThat(emergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(1);
        assertThat(emergencyContactsPreference.getPreferenceCount()).isEqualTo(1);

        EditInfoActivity.ClearAllDialogFragment clearAllDialogFragment =
                (EditInfoActivity.ClearAllDialogFragment) editInfoActivity.getFragmentManager()
                        .findFragmentByTag(EditInfoActivity.TAG_CLEAR_ALL_DIALOG);
        assertThat(clearAllDialogFragment).isNull();
        getInstrumentation().invokeMenuActionSync(editInfoActivity, R.id.action_clear_all,
                0 /* flags */);
        getInstrumentation().waitForIdleSync();
        final EditInfoActivity.ClearAllDialogFragment clearAllDialogFragmentAfterwards =
                (EditInfoActivity.ClearAllDialogFragment) editInfoActivity.getFragmentManager()
                        .findFragmentByTag(EditInfoActivity.TAG_CLEAR_ALL_DIALOG);

        // Temporarily convert a crashing test to a failing one by asserting some things aren't
        // null that end up being null today. In the end, we want to fix the tests.
        assertThat(clearAllDialogFragmentAfterwards).isNotNull();
        Dialog clearAllDialog = clearAllDialogFragmentAfterwards.getDialog();
        assertThat(clearAllDialog).isNotNull();
        assertThat(clearAllDialog.isShowing()).isTrue();

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((AlertDialog) clearAllDialogFragmentAfterwards.getDialog())
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .performClick();
            }
        });
        getInstrumentation().waitForIdleSync();

        assertThat(editInfoActivity.getFragments()).isEqualTo(mFragments);

        // After clearing all the preferences, onCreate is called for both fragments.
        // This makes the preferences point to old ones. Here we load what the user
        // is seeing
        final NameAutoCompletePreference namePreferenceAfterClear =
                (NameAutoCompletePreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_NAME);
        final EmergencyEditTextPreference addressPreferenceAfterClear =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_ADDRESS);
        final EmergencyListPreference bloodTypePreferenceAfterClear =
                (EmergencyListPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_BLOOD_TYPE);
        final EmergencyEditTextPreference allergiesPreferenceAfterClear =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_ALLERGIES);
        final EmergencyEditTextPreference medicationsPreferenceAfterClear =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_MEDICATIONS);
        final EmergencyEditTextPreference medicalConditionsPreferenceAfterClear =
                (EmergencyEditTextPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_MEDICAL_CONDITIONS);
        final EmergencyListPreference organDonorPreferenceAfterClear =
                (EmergencyListPreference) mEditEmergencyInfoFragment
                        .findPreference(PreferenceKeys.KEY_ORGAN_DONOR);

        final EmergencyContactsPreference emergencyContactsPreferenceAfterClear =
                (EmergencyContactsPreference) mEditEmergencyContactsFragment
                        .findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);

        assertThat(namePreferenceAfterClear.getSummary()).isEqualTo(unknownName);
        assertThat(addressPreferenceAfterClear.getSummary()).isEqualTo(unknownAddress);
        assertThat(bloodTypePreferenceAfterClear.getSummary().toString())
                .isEqualTo(unknownBloodType);
        assertThat(allergiesPreferenceAfterClear.getSummary()).isEqualTo(unknownAllergies);
        assertThat(medicationsPreferenceAfterClear.getSummary()).isEqualTo(unknownMedications);
        assertThat(medicalConditionsPreferenceAfterClear.getSummary()).isEqualTo(
                unknownMedicalConditions);
        assertThat(organDonorPreferenceAfterClear.getSummary()).isEqualTo(unknownOrganDonor);
        assertThat(emergencyContactsPreferenceAfterClear.getEmergencyContacts()).isEmpty();
        assertThat(emergencyContactsPreferenceAfterClear.getPreferenceCount()).isEqualTo(0);

        assertThat(ContactTestUtils
                .deleteContact(getActivity().getContentResolver(), "Michael", "789")).isTrue();
    }

    private void forceScreenOn() {
        int levelAndFlags = PowerManager.FULL_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE
                | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        PowerManager powerManager =
                (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        mKeepScreenOnWakeLock = powerManager.newWakeLock(levelAndFlags, "EditEmergencyInfo");
        mKeepScreenOnWakeLock.setReferenceCounted(false);
        mKeepScreenOnWakeLock.acquire();
    }

    private void releaseScreenOn() {
        mKeepScreenOnWakeLock.release();
    }
}
