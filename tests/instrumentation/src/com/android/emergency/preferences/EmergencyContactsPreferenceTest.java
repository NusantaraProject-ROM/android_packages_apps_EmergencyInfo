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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.android.emergency.ContactTestUtils;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.edit.EditInfoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link EmergencyContactsPreference}.
 */
@LargeTest
public class EmergencyContactsPreferenceTest
        extends ActivityInstrumentationTestCase2<EditInfoActivity> {
    private PreferenceFragment mEditInfoFragment;
    private EmergencyContactsPreference mEmergencyContactsPreference;
    private ContentResolver mContentResolver;

    public EmergencyContactsPreferenceTest() {
        super(EditInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEditInfoFragment = getActivity().getFragment();

        mEmergencyContactsPreference =
                (EmergencyContactsPreference) mEditInfoFragment
                        .findPreference(PreferenceKeys.KEY_EMERGENCY_CONTACTS);
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mEmergencyContactsPreference.setEmergencyContacts(new ArrayList<Uri>());
                }
            });
        } catch (Throwable throwable) {
            fail("Should not throw exception: " + throwable.getMessage());
        }

        mContentResolver = getActivity().getContentResolver();
    }

    @Override
    protected void tearDown() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        super.tearDown();
    }

    public void testEmptyState() {
        assertThat(mEmergencyContactsPreference).isNotNull();
        assertThat(mEmergencyContactsPreference.isPersistent()).isTrue();
        assertThat(mEmergencyContactsPreference.isNotSet()).isTrue();
        assertThat(mEmergencyContactsPreference.getEmergencyContacts()).isEmpty();
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(0);
    }

    public void testAddAndRemoveEmergencyContact() throws Throwable {
        final String name = "Jane";
        final String phoneNumber = "456";

        final Uri phoneUri =
                ContactTestUtils.createContact(mContentResolver, name, phoneNumber);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmergencyContactsPreference.addNewEmergencyContact(phoneUri);
            }
        });

        assertThat(mEmergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(1);
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(1);
        ContactPreference contactPreference = (ContactPreference)
                mEmergencyContactsPreference.getPreference(0);

        assertThat(contactPreference.getPhoneUri()).isEqualTo(phoneUri);
        assertThat(contactPreference.getTitle()).isEqualTo(name);
        assertThat((String) contactPreference.getSummary()).contains(phoneNumber);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmergencyContactsPreference.onRemoveContactPreference(
                        (ContactPreference) mEmergencyContactsPreference.getPreference(0));
            }
        });

        assertThat(mEmergencyContactsPreference.getEmergencyContacts()).isEmpty();
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(0);

        // Clean up the inserted contact
        assertThat(ContactTestUtils.deleteContact(mContentResolver, name, phoneNumber)).isTrue();
    }

    public void testReloadFromPreference() throws Throwable {
        final String nameJane = "Jane";
        final String phoneNumberJane = "456";
        final Uri emergencyContactJane = ContactTestUtils
                .createContact(mContentResolver, nameJane, phoneNumberJane);

        final String nameJohn = "John";
        final String phoneNumberJohn = "123";
        final Uri emergencyContactJohn = ContactTestUtils
                .createContact(mContentResolver, nameJohn, phoneNumberJohn);

        final List<Uri> emergencyContacts = new ArrayList<>();
        emergencyContacts.add(emergencyContactJane);
        emergencyContacts.add(emergencyContactJohn);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmergencyContactsPreference.setEmergencyContacts(emergencyContacts);
            }
        });

        assertThat(mEmergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(2);
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(2);

        // Delete Jane from another app (e.g. contacts)
        assertThat(ContactTestUtils
                .deleteContact(mContentResolver, nameJane, phoneNumberJane)).isTrue();
        getInstrumentation().waitForIdleSync();

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmergencyContactsPreference.reloadFromPreference();
            }
        });

        getInstrumentation().waitForIdleSync();

        // Assert the only remaining contact is John
        assertThat(mEmergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(1);
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(1);
        ContactPreference contactPreference = (ContactPreference)
                mEmergencyContactsPreference.getPreference(0);
        assertThat(contactPreference.getPhoneUri()).isEqualTo(emergencyContactJohn);

        // Clean up the inserted contact
        assertThat(ContactTestUtils
                .deleteContact(mContentResolver, nameJohn, phoneNumberJohn)).isTrue();
    }

    public void testWidgetClick_positiveButton() throws Throwable {
        final String name = "Jane";
        final String phoneNumber = "456";

        final Uri emergencyPhoneUri =
                ContactTestUtils.createContact(mContentResolver, name, phoneNumber);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmergencyContactsPreference.addNewEmergencyContact(emergencyPhoneUri);
            }
        });

        assertThat(mEmergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(1);
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(1);
        ContactPreference contactPreference = (ContactPreference)
                mEmergencyContactsPreference.getPreference(0);

        View contactPreferenceView = contactPreference.getView(null, null);
        assertThat(contactPreferenceView).isNotNull();
        final View deleteContactWidget = contactPreferenceView.findViewById(R.id.delete_contact);
        assertThat(deleteContactWidget.getVisibility()).isEqualTo(View.VISIBLE);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                deleteContactWidget.performClick();
            }
        });

        getInstrumentation().waitForIdleSync();
        final AlertDialog removeContactDialog = contactPreference.getRemoveContactDialog();
        assertThat(removeContactDialog.isShowing()).isTrue();

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeContactDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();

        assertThat(mEmergencyContactsPreference.getEmergencyContacts()).isEmpty();
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(0);

        // Clean up the inserted contact
        assertThat(ContactTestUtils.deleteContact(mContentResolver, name, phoneNumber)).isTrue();
    }

    public void testWidgetClick_negativeButton() throws Throwable {
        final String name = "Jane";
        final String phoneNumber = "456";

        final Uri emergencyPhoneUri =
                ContactTestUtils.createContact(mContentResolver, name, phoneNumber);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmergencyContactsPreference.addNewEmergencyContact(emergencyPhoneUri);
            }
        });

        assertThat(mEmergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(1);
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(1);
        ContactPreference contactPreference = (ContactPreference)
                mEmergencyContactsPreference.getPreference(0);

        View contactPreferenceView = contactPreference.getView(null, null);
        assertThat(contactPreferenceView).isNotNull();
        final View deleteContactWidget = contactPreferenceView.findViewById(R.id.delete_contact);
        assertThat(deleteContactWidget.getVisibility()).isEqualTo(View.VISIBLE);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                deleteContactWidget.performClick();
            }
        });
        getInstrumentation().waitForIdleSync();

        getInstrumentation().waitForIdleSync();
        final AlertDialog removeContactDialog = contactPreference.getRemoveContactDialog();
        assertThat(removeContactDialog.isShowing()).isTrue();

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeContactDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            }
        });

        assertThat(mEmergencyContactsPreference.getEmergencyContacts().size()).isEqualTo(1);
        assertThat(mEmergencyContactsPreference.getPreferenceCount()).isEqualTo(1);

        // Clean up the inserted contact
        assertThat(ContactTestUtils.deleteContact(mContentResolver, name, phoneNumber)).isTrue();
    }
}
