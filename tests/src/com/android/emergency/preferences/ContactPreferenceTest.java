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

import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;

import com.android.emergency.ContactTestUtils;
import com.android.emergency.EmergencyContactManager;
import com.android.emergency.R;
import com.android.emergency.edit.EditInfoActivity;

/**
 * Tests for {@link ContactPreference}.
 */
public class ContactPreferenceTest extends ActivityInstrumentationTestCase2<EditInfoActivity> {
    public ContactPreferenceTest() {
        super(EditInfoActivity.class);
    }

    public void testContactPreference() {
        String name = "Jake";
        String phoneNumber = "123456";
        Uri contactUri =
                ContactTestUtils.createContact(getActivity().getContentResolver(),
                        name,
                        phoneNumber);
        ContactPreference contactPreference = new ContactPreference(getActivity(), contactUri);
        assertEquals(contactUri, contactPreference.getContactUri());
        assertEquals(name, contactPreference.getContact().getName());
        assertEquals(phoneNumber, contactPreference.getContact().getPhoneNumber());

        assertNull(contactPreference.getRemoveContactDialog());
        contactPreference.setRemoveContactPreferenceListener(
                new ContactPreference.RemoveContactPreferenceListener() {
                    @Override
                    public void onRemoveContactPreference(ContactPreference preference) {
                        // Do nothing
                    }
                });
        assertNotNull(contactPreference.getRemoveContactDialog());

        assertTrue(ContactTestUtils.deleteContact(getActivity().getContentResolver(),
                name,
                phoneNumber));
    }
}
