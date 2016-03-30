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

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.test.ActivityInstrumentationTestCase2;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.edit.EditEmergencyInfoFragment;
import com.android.emergency.edit.EditInfoActivity;

/**
 * Tests for {@link BirthdayPreference}.
 */
public class BirthdayPreferenceTest extends ActivityInstrumentationTestCase2<EditInfoActivity> {

    private BirthdayPreference mBirthdayPreference;
    private EditEmergencyInfoFragment mEditInfoFragment;

    public BirthdayPreferenceTest() {
        super(EditInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEditInfoFragment = (EditEmergencyInfoFragment) getActivity().getFragments().get(0).second;
        mBirthdayPreference = (BirthdayPreference)
                mEditInfoFragment.findPreference(PreferenceKeys.KEY_DATE_OF_BIRTH);
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBirthdayPreference.setDateOfBirth(BirthdayPreference.DEFAULT_UNSET_VALUE);
                }
            });
        } catch (Throwable throwable) {
            fail("Should not throw exception");
        }
    }

    public void testGetSummary() {
        String summary = (String) mBirthdayPreference.getSummary();
        String summaryExp = getActivity().getResources().getString(R.string.unknown_date_of_birth);
        assertEquals(summaryExp, summary);
    }

    public void testGetTitle() {
        CharSequence title = mBirthdayPreference.getTitle();
        String titleExp = getActivity().getResources().getString(R.string.date_of_birth);
        assertEquals(titleExp, title);
    }

    public void testProperties() {
        assertNotNull(mBirthdayPreference);
        assertEquals(PreferenceKeys.KEY_DATE_OF_BIRTH, mBirthdayPreference.getKey());
        assertTrue(mBirthdayPreference.isEnabled());
        assertTrue(mBirthdayPreference.isPersistent());
        assertTrue(mBirthdayPreference.isSelectable());
        assertTrue(mBirthdayPreference.isNotSet());
        assertEquals(BirthdayPreference.DEFAULT_UNSET_VALUE, mBirthdayPreference.getDateOfBirth());
    }

    public void testSetDate() throws Throwable {
        final long dateMs = 123456600L;
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBirthdayPreference.setDateOfBirth(dateMs);
            }
        });
        assertEquals(dateMs, mBirthdayPreference.getDateOfBirth());
        assertFalse(mBirthdayPreference.isNotSet());
        assertEquals(dateMs, mBirthdayPreference.getSharedPreferences()
                .getLong(mBirthdayPreference.getKey(), 0));
        assertNotSame(getActivity().getResources().getString(R.string.unknown_date_of_birth),
                mBirthdayPreference.getSummary());
    }

    public void testSetDate_fromDatePicker() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 9th of January 1987
                mBirthdayPreference.onDateSet(null /* view*/, 1987, 0, 9);
            }
        });
        assertEquals(537148800000L, mBirthdayPreference.getDateOfBirth());
    }

    public void testDialogShowAndDismiss_positiveButton() throws Throwable {
        final DatePickerDialog datePickerDialog = mBirthdayPreference.getDatePickerDialog();
        assertFalse(datePickerDialog.isShowing());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBirthdayPreference.getOnPreferenceClickListener()
                        .onPreferenceClick(mBirthdayPreference);
            }
        });
        assertTrue(datePickerDialog.isShowing());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(datePickerDialog.isShowing());
    }

    public void testDialogShowAndDismiss_negativeButton() throws Throwable {
        final DatePickerDialog datePickerDialog = mBirthdayPreference.getDatePickerDialog();
        assertFalse(datePickerDialog.isShowing());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBirthdayPreference.getOnPreferenceClickListener()
                        .onPreferenceClick(mBirthdayPreference);
            }
        });
        assertTrue(datePickerDialog.isShowing());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(datePickerDialog.isShowing());
    }

    public void testReloadFromPreference() throws Throwable {
        long dateMs = 123456600L;
        mBirthdayPreference.getSharedPreferences().edit()
                .putLong(mBirthdayPreference.getKey(), dateMs).apply();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBirthdayPreference.reloadFromPreference();
            }

        });
        assertEquals(dateMs, mBirthdayPreference.getDateOfBirth());
    }

    //TODO: test clearing date with widget layout
}