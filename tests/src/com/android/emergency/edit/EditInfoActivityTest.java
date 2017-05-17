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
import static com.google.common.truth.Truth.assertWithMessage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
    private EditInfoFragment mFragment;
    private PreferenceGroup mMedicalInfoParent;
    private PowerManager.WakeLock mKeepScreenOnWakeLock;

    public EditInfoActivityTest() {
        super(EditInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        forceScreenOn();

        mFragment = (EditInfoFragment) getActivity().getFragment();
        mMedicalInfoParent = mFragment.getMedicalInfoParent();
    }

    @Override
    protected void tearDown() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        releaseScreenOn();
        super.tearDown();
    }

    public void testInitialState() {
        // Because the initial state of each preference is empty, the edit activity removes the
        // preference. As a result, we expect them all to be null.
        for (String key : PreferenceKeys.KEYS_EDIT_EMERGENCY_INFO) {
            assertWithMessage(key).that(mMedicalInfoParent.findPreference(key)).isNull();
        }
        EmergencyContactsPreference emergencyContactsPreference =
                (EmergencyContactsPreference) mFragment.findPreference(
                        PreferenceKeys.KEY_EMERGENCY_CONTACTS);
        assertThat(emergencyContactsPreference).isNotNull();
        assertThat(emergencyContactsPreference.getPreferenceCount()).isEqualTo(0);
    }

    public void testClearAllPreferences () throws Throwable {
        final NameAutoCompletePreference namePreference =
                (NameAutoCompletePreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_NAME);
        final EmergencyEditTextPreference addressPreference =
                (EmergencyEditTextPreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_ADDRESS);
        final EmergencyListPreference bloodTypePreference =
                (EmergencyListPreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_BLOOD_TYPE);
        final EmergencyEditTextPreference allergiesPreference =
                (EmergencyEditTextPreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_ALLERGIES);
        final EmergencyEditTextPreference medicationsPreference =
                (EmergencyEditTextPreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_MEDICATIONS);
        final EmergencyEditTextPreference medicalConditionsPreference =
                (EmergencyEditTextPreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_MEDICAL_CONDITIONS);
        final EmergencyListPreference organDonorPreference =
                (EmergencyListPreference) mFragment.getMedicalInfoPreference(
                        PreferenceKeys.KEY_ORGAN_DONOR);

        final EmergencyContactsPreference emergencyContactsPreference =
                (EmergencyContactsPreference) mFragment
                        .findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);
        final Uri contactUri = ContactTestUtils
                .createContact(getActivity().getContentResolver(), "Michael", "789");
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

        String unknownName = getActivity().getResources().getString(R.string.unknown_name);
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
                (EditInfoActivity.ClearAllDialogFragment) getActivity().getFragmentManager()
                        .findFragmentByTag(EditInfoActivity.TAG_CLEAR_ALL_DIALOG);
        assertThat(clearAllDialogFragment).isNull();
        getInstrumentation().invokeMenuActionSync(getActivity(), R.id.action_clear_all,
                0 /* flags */);
        getInstrumentation().waitForIdleSync();
        final EditInfoActivity.ClearAllDialogFragment clearAllDialogFragmentAfterwards =
                (EditInfoActivity.ClearAllDialogFragment) getActivity().getFragmentManager()
                        .findFragmentByTag(EditInfoActivity.TAG_CLEAR_ALL_DIALOG);

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

        // After the clear all the preferences dialog is confirmed, the preferences values are
        // reloaded, and the existing object references are updated in-place.
        assertThat(namePreference.getSummary()).isNull();
        assertThat(addressPreference.getSummary()).isNull();
        assertThat(bloodTypePreference.getSummary().toString()).isEqualTo(unknownBloodType);
        assertThat(allergiesPreference.getSummary()).isNull();
        assertThat(medicationsPreference.getSummary()).isNull();
        assertThat(medicalConditionsPreference.getSummary()).isNull();
        assertThat(organDonorPreference.getSummary()).isEqualTo(unknownOrganDonor);
        assertThat(emergencyContactsPreference.getEmergencyContacts()).isEmpty();
        assertThat(emergencyContactsPreference.getPreferenceCount()).isEqualTo(0);
        // The preference values are not displayed, being empty.
        for (String key : PreferenceKeys.KEYS_EDIT_EMERGENCY_INFO) {
            assertWithMessage(key).that(mMedicalInfoParent.findPreference(key)).isNull();
        }

        assertThat(ContactTestUtils
                .deleteContact(getActivity().getContentResolver(), "Michael", "789")).isTrue();
    }

    public void testAddContactPreference() throws Throwable {
        Preference addContactPreference =
                mFragment.findPreference(PreferenceKeys.KEY_ADD_EMERGENCY_CONTACT);
        assertThat(addContactPreference).isNotNull();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PICK);
        intentFilter.addDataType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);

        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(intentFilter, null, true /* block */);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                addContactPreference
                        .getOnPreferenceClickListener().onPreferenceClick(addContactPreference);
            }
        });

        assertThat(getInstrumentation().checkMonitorHit(activityMonitor, 1 /* minHits */)).isTrue();
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
