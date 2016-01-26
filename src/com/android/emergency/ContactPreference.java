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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;


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
     * Calls the contact.
     */
    public void callContact() {
        Uri number = getNumber();
        if (number == null) {
            String errorMessage = getContext().getString(R.string.phone_number_error);
            Toast errorToast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
            // TODO: Get toast to display over lock screen
            errorToast.show();
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL, number);
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                MetricsLogger.action(getContext(), MetricsEvent.ACTION_CALL_EMERGENCY_CONTACT);
                getContext().startActivity(callIntent);
            }
        }
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
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private Uri getNumber() {
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor contactCursor = contentResolver.query(getUri(), null, null, null, null);
        try {
            if (contactCursor != null && contactCursor.moveToFirst()) {
                String id = contactCursor.getString(
                        contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (Integer.parseInt(contactCursor.getString(contactCursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    try {
                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            return Uri.parse("tel:" + phoneCursor.getString(
                                    phoneCursor.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        }
                    } finally {
                        phoneCursor.close();
                    }
                }
            }
        } finally {
            contactCursor.close();
        }
        return null;
    }
}
