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
import android.test.suitebuilder.annotation.LargeTest;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.edit.EditMedicalInfoActivity;
import com.android.emergency.edit.EditMedicalInfoFragment;

/**
 * Tests for {@link EmergencyListPreference}.
 */
@LargeTest
public class EmergencyListPreferenceTest
        extends ActivityInstrumentationTestCase2<EditMedicalInfoActivity> {
    private EmergencyListPreference mOrganDonorPreference;
    private EmergencyListPreference mBloodTypeListPreference;
    private EditMedicalInfoFragment mEditInfoFragment;

    public EmergencyListPreferenceTest() {
        super(EditMedicalInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEditInfoFragment = getActivity().getFragment();
        mOrganDonorPreference = (EmergencyListPreference)
                mEditInfoFragment.findPreference(PreferenceKeys.KEY_ORGAN_DONOR);
        mBloodTypeListPreference = (EmergencyListPreference)
                mEditInfoFragment.findPreference(PreferenceKeys.KEY_BLOOD_TYPE);
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mOrganDonorPreference.setValue("");
                    mBloodTypeListPreference.setValue("");
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

    public void testSummary_organDonor() {
        String summary = (String) mOrganDonorPreference.getSummary();
        String summaryExp =
                getActivity().getResources().getString(R.string.unknown_organ_donor);
        assertThat(summary).isEqualTo(summaryExp);
    }

    public void testSummary_bloodType() {
        String summary = mBloodTypeListPreference.getSummary().toString();
        CharSequence summaryExp =
                getActivity().getResources().getString(R.string.unknown_blood_type);
        assertThat(summary).isEqualTo(summaryExp);
    }

    public void testTitle_organDonor() {
        String title = (String) mOrganDonorPreference.getTitle();
        String titleExp =
                getActivity().getResources().getString(R.string.organ_donor);
        assertThat(title).isEqualTo(titleExp);
    }

    public void testTitle_bloodType() {
        String title = (String) mBloodTypeListPreference.getTitle();
        String titleExp =
                getActivity().getResources().getString(R.string.blood_type);
        assertThat(title).isEqualTo(titleExp);
    }

    public void testProperties_organDonor() {
        assertThat(mOrganDonorPreference).isNotNull();
        assertThat(mOrganDonorPreference.getKey()).isEqualTo(PreferenceKeys.KEY_ORGAN_DONOR);
        assertThat(mOrganDonorPreference.isEnabled()).isTrue();
        assertThat(mOrganDonorPreference.isPersistent()).isTrue();
        assertThat(mOrganDonorPreference.isSelectable()).isTrue();
        assertThat(mOrganDonorPreference.isNotSet()).isTrue();
        assertThat(mOrganDonorPreference.getValue()).isEqualTo("");
        assertThat(mOrganDonorPreference.getEntries().length).isEqualTo(
            mOrganDonorPreference.getEntryValues().length);
        assertNull(mOrganDonorPreference.getContentDescriptions());
    }

    public void testProperties_bloodType() {
        assertThat(mBloodTypeListPreference).isNotNull();
        assertThat(mBloodTypeListPreference.getKey()).isEqualTo(PreferenceKeys.KEY_BLOOD_TYPE);
        assertThat(mBloodTypeListPreference.isEnabled()).isTrue();
        assertThat(mBloodTypeListPreference.isPersistent()).isTrue();
        assertThat(mBloodTypeListPreference.isSelectable()).isTrue();
        assertThat(mBloodTypeListPreference.isNotSet()).isTrue();
        assertThat(mBloodTypeListPreference.getValue()).isEqualTo("");
        assertThat(mBloodTypeListPreference.getEntries().length).isEqualTo(
                mBloodTypeListPreference.getEntryValues().length);
        assertThat(mBloodTypeListPreference.getContentDescriptions().length).isEqualTo(
                mBloodTypeListPreference.getEntries().length);
    }

    public void testReloadFromPreference() throws Throwable {
        mEditInfoFragment.getPreferenceManager().getSharedPreferences()
                .edit()
                .putString(mOrganDonorPreference.getKey(),
                        (String) mOrganDonorPreference.getEntryValues()[0])
                .commit();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOrganDonorPreference.reloadFromPreference();
            }
        });
        assertThat(mOrganDonorPreference.getValue()).isEqualTo(
                mOrganDonorPreference.getEntryValues()[0]);
        assertThat(mOrganDonorPreference.isNotSet()).isFalse();
    }

    public void testSetValue() throws Throwable {
        for (int i = 0; i < mOrganDonorPreference.getEntryValues().length; i++) {
            final int index = i;
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mOrganDonorPreference.setValue((String)
                            mOrganDonorPreference.getEntryValues()[index]);
                }
            });

            assertThat(mOrganDonorPreference.getValue()).isEqualTo(
                    mOrganDonorPreference.getEntryValues()[index]);
            if (!TextUtils.isEmpty(mOrganDonorPreference.getEntryValues()[index])) {
                assertThat(mOrganDonorPreference.getSummary()).isEqualTo(
                        mOrganDonorPreference.getEntries()[index]);
            } else {
                assertThat(mOrganDonorPreference.getSummary()).isEqualTo(
                        getActivity().getResources().getString(R.string.unknown_organ_donor));
            }
        }
    }

    public void testContentDescriptions() {
        for (int i = 0; i < mBloodTypeListPreference.getEntries().length; i++) {
            SpannableString entry = ((SpannableString) mBloodTypeListPreference.getEntries()[i]);
            TtsSpan[] span = entry.getSpans(0,
                    mBloodTypeListPreference.getContentDescriptions().length, TtsSpan.class);
            assertThat(span.length).isEqualTo(1);
            assertThat(mBloodTypeListPreference.getContentDescriptions()[i]).isEqualTo(
                    span[0].getArgs().get(TtsSpan.ARG_TEXT));
        }
    }
}
