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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.ArraySet;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Manages emergency contacts of the user.
 */
public class EmergencyContactManager {
    private final SharedPreferences mSharedPreferences;
    private final String mKey;
    private final Context mContext;

    /**
     * Creates a new instance initialized with context and the shared preferences used to store the
     * emergency contacts under the specified key.
     */
    public EmergencyContactManager(Context context, SharedPreferences sharedPreferences,
                                   String key) {
        mContext = context;
        mSharedPreferences = sharedPreferences;
        mKey = key;
    }

    /**
     * Adds a new contact to the emergency contacts. */
    public void addContact(Uri contactUri) {
        // TODO: Consider refactoring this to use always setContacts() rather than
        // addContact()/removeContact()
        Set<Uri> emergencyContacts = getEmergencyContacts();
        if (emergencyContacts.add(contactUri)) {
            setEmergencyContacts(emergencyContacts);
        }
    }

    /** Removes the specified contact from the list of emergency contacts. */
    public void removeContact(Uri contactUri) {
        // TODO: Consider refactoring this to use always setContacts() rather than
        // addContact()/removeContact()
        Set<Uri> emergencyContacts = getEmergencyContacts();
        if (emergencyContacts.remove(contactUri)) {
            setEmergencyContacts(emergencyContacts);
        }
    }

    public Set<Uri> getEmergencyContacts() {
        Set<String> emergencyContactStrings = mSharedPreferences.getStringSet(mKey,
                Collections.<String>emptySet());
        Set<Uri> emergencyContacts = new ArraySet<Uri>(emergencyContactStrings.size());
        for (String emergencyContact : emergencyContactStrings) {
            Uri contactUri = Uri.parse(emergencyContact);
            if (isValidEmergencyContact(contactUri)) {
                emergencyContacts.add(contactUri);
            }
        }
        // If not all contacts were added, then we need to overwrite the emergency contacts stored
        // in shared preferences. This deals with emergency contacts being deleted from contacts:
        // currently we have no way to being notified when this happens.
        if (emergencyContacts.size() != emergencyContactStrings.size()) {
            setEmergencyContacts(emergencyContacts);
        }
        return emergencyContacts;
    }

    /** Returns the display name of the contact. */
    public static String getName(Context context, Uri contactUri) {
        Cursor cursor = context.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static boolean contactExists(Context context, Uri contactUri) {
        Cursor cursor = context.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }


    /** Returns the phone number of the contact. */
    public static String[] getPhoneNumbers(Context context, Uri contactUri) {
        // TODO: Investigate passing around CONTENT_LOOKUP_URI instead of content CONTENT_URI.
        // The method to get the CONTENT_LOOKUP_URI when the user selects a contact is:
        // ContactsContract.Contacts.getLookupUri(ContentResolver resolver, Uri contactUri)
        // Then use ContactsContract.Contacts.lookupContact(ContentResolver resolver, Uri lookupUri)
        // to get the CONTENT_URI
        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactCursor = contentResolver.query(contactUri, null, null, null, null);
        try {
            if (contactCursor != null && contactCursor.moveToFirst()) {
                if (contactCursor.getInt(contactCursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) != 0) {
                    String id = contactCursor.getString(
                            contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null,
                            null);
                    try {
                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            String[] phoneNumbers =
                                    new String[phoneCursor.getCount()];
                            for (int i = 0; i < phoneCursor.getCount(); i++) {
                                String phoneNumber =
                                        phoneCursor.getString(phoneCursor.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                                phoneNumbers[i] = phoneNumber;
                                phoneCursor.moveToNext();
                            }
                            return phoneNumbers;
                        }
                    } finally {
                        if (phoneCursor != null) {
                            phoneCursor.close();
                        }
                    }
                }
            }
        } finally {
            if (contactCursor != null) {
                contactCursor.close();
            }
        }
        return null;
    }

    /** Returns the Bitmap corresponding to the contact's photo. */
    public static Bitmap getContactPhoto(Context context, Uri contactUri) {
        Uri photoUri = Uri.withAppendedPath(contactUri,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }


    /** Returns whether the contact uri is not null and corresponds to an existing contact. */
    private boolean isValidEmergencyContact(Uri contactUri) {
        return contactUri != null && contactExists(mContext, contactUri);
    }

    private void setEmergencyContacts(Set<Uri> emergencyContacts) {
        Set<String> emergencyContactStrings = new ArraySet<String>(emergencyContacts.size());
        for (Uri contactUri : emergencyContacts) {
            emergencyContactStrings.add(contactUri.toString());
        }
        mSharedPreferences.edit().putStringSet(mKey, emergencyContactStrings).commit();
    }
}
