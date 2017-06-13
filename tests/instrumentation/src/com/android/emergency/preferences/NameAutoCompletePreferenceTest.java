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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.edit.EditMedicalInfoActivity;
import com.android.emergency.edit.EditMedicalInfoFragment;

/**
 * Tests for {@link NameAutoCompletePreference}.
 */
@LargeTest
public class NameAutoCompletePreferenceTest
        extends ActivityInstrumentationTestCase2<EditMedicalInfoActivity> {
    private NameAutoCompletePreference mNameAutoCompletePreference;
    private EditMedicalInfoFragment mEditInfoFragment;

    public NameAutoCompletePreferenceTest() {
        super(EditMedicalInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEditInfoFragment = getActivity().getFragment();
        mNameAutoCompletePreference = (NameAutoCompletePreference)
                mEditInfoFragment.findPreference(PreferenceKeys.KEY_NAME);
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNameAutoCompletePreference.setText("");
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

    public void testDialogShowAndDismiss_positiveButton() throws Throwable {
        assertThat(mNameAutoCompletePreference.getDialog()).isNull();
        assertThat(mNameAutoCompletePreference).isNotNull();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNameAutoCompletePreference.onClick();
            }
        });
        final AlertDialog dialog = (AlertDialog) mNameAutoCompletePreference.getDialog();
        assertThat(dialog.isShowing()).isTrue();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertThat(dialog.isShowing()).isFalse();
    }

    public void testDialogShowAndDismiss_negativeButton() throws Throwable {
        assertThat(mNameAutoCompletePreference.getDialog()).isNull();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNameAutoCompletePreference.onClick();
            }
        });
        final AlertDialog dialog = (AlertDialog) mNameAutoCompletePreference.getDialog();
        assertThat(dialog.isShowing()).isTrue();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertThat(dialog.isShowing()).isFalse();
    }
}
