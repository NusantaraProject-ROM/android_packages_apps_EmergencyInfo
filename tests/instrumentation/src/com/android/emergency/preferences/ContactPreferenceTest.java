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

import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.ContactsContract;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

import com.android.emergency.ContactTestUtils;
import com.android.emergency.edit.EditInfoActivity;

/**
 * Tests for {@link ContactPreference}.
 */
@MediumTest
public class ContactPreferenceTest extends ActivityInstrumentationTestCase2<EditInfoActivity> {
    private static final String NAME = "Jake";
    private static final String PHONE_NUMBER = "123456";
    private ContactPreference mContactPreference;
    private Uri mPhoneUri;

    public ContactPreferenceTest() {
        super(EditInfoActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPhoneUri =
                ContactTestUtils.createContact(getActivity().getContentResolver(),
                        NAME,
                        PHONE_NUMBER);
        mContactPreference = new ContactPreference(getActivity(), mPhoneUri);
    }

    @Override
    protected void tearDown() throws Exception {
        assertThat(ContactTestUtils.deleteContact(getActivity().getContentResolver(),
                NAME,
                PHONE_NUMBER)).isTrue();
        super.tearDown();
    }

    public void testContactPreference() {
        assertThat(mContactPreference.getPhoneUri()).isEqualTo(mPhoneUri);
        assertThat(mContactPreference.getContact().getName()).isEqualTo(NAME);
        assertThat(mContactPreference.getContact().getPhoneNumber()).isEqualTo(PHONE_NUMBER);

        assertThat(mContactPreference.getRemoveContactDialog()).isNull();
        mContactPreference.setRemoveContactPreferenceListener(
                new ContactPreference.RemoveContactPreferenceListener() {
                    @Override
                    public void onRemoveContactPreference(ContactPreference preference) {
                        // Do nothing
                    }
                });
        assertThat(mContactPreference.getRemoveContactDialog()).isNotNull();
    }

    public void testDisplayContact() throws Throwable {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_VIEW);
        intentFilter.addDataType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(intentFilter, null, true /* block */);
        mContactPreference.displayContact();

        assertThat(getInstrumentation().checkMonitorHit(activityMonitor, 1 /* minHits */)).isTrue();
    }

    public void testCallContact() throws Throwable {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CALL);
        intentFilter.addDataScheme("tel");
        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(intentFilter, null, true /* block */);
        mContactPreference.callContact();

        assertThat(getInstrumentation().checkMonitorHit(activityMonitor, 1 /* minHits */)).isTrue();
    }
}
