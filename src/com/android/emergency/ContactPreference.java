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
package com.android.emergency;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.Preference;
import android.provider.ContactsContract;

/**
 * A {@link Preference} to display a contact using the specified URI string.
 */
public class ContactPreference extends Preference {

    private final Uri mUri;

    /**
     * Instantiates a ContactPreference that displays an emergency contact, taking in a Context and
     * the Uri of the contact as a String.
     */
    public ContactPreference(Context context, String uriString) {
        super(context);
        mUri = Uri.parse(uriString);
        String name = getName();
        setTitle((name != null) ? name : getContext().getString(R.string.unknown_contact));
    }

    /**
     * Displays a contact card for the contact.
     */
    public void displayContact() {
        Intent contactIntent = new Intent(Intent.ACTION_VIEW);
        contactIntent.setData(getUri());
        getContext().startActivity(contactIntent);
    }

    /**
     * Returns the URI for the contact.
     */
    public Uri getUri() {
        return mUri;
    }

    private String getName() {
        Cursor cursor = getContext().getContentResolver().query(getUri(), null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
