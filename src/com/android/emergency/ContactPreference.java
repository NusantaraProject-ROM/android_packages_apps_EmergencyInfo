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
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;


/**
 * A {@link Preference} to display a contact using the specified URI string.
 */
public class ContactPreference extends Preference {

    private final Uri mUri;
    private final DeleteContactListener mDeleteContactListener;

    /**
     * Listener for deleting a contact.
     */
    public interface DeleteContactListener {
        /**
         * Callback to delete a contact.
         */
        void onContactDelete(String contactUri);
    }

    /**
     * Instantiates a ContactPreference that displays an emergency contact, taking in a Context and
     * the Uri of the contact as a String.
     */
    public ContactPreference(Context context, String uriString,
                             DeleteContactListener deleteContactListener) {
        super(context);
        mUri = Uri.parse(uriString);
        mDeleteContactListener = deleteContactListener;
        String name = getName();
        setTitle((name != null) ? name : getContext().getString(R.string.unknown_contact));
        setWidgetLayoutResource(R.layout.preference_user_delete_widget);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View deleteContactIcon = view.findViewById(R.id.delete_contact);
        if (deleteContactIcon != null) {
            deleteContactIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(String.format(getContext()
                            .getString(R.string.remove_contact), getName()));
                    builder.setPositiveButton(getContext().getString(R.string.remove),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    mDeleteContactListener.onContactDelete(mUri.toString());
                                }
                            }).setNegativeButton(getContext().getString(R.string.cancel), null);
                    builder.create().show();
                }
            });
        }
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
        contactIntent.setData(mUri);
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
        // TODO: Investigate if this can be done in 1 query instead of 2.
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor contactCursor = contentResolver.query(getUri(), null, null, null, null);
        try {
            if (contactCursor != null && contactCursor.moveToFirst()) {
                String id = contactCursor.getString(
                        contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (contactCursor.getInt(contactCursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) != 0) {
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
