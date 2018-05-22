/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.preference.PreferenceManager;

/**
 * Provider used to read user emergency information name.
 */
public class EmergencyInfoNameProvider extends ContentProvider implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "EmergencyInfoNameProvider";

    private static final String AUTHORITY = "com.android.emergency.info.name";
    private static final String INFO_PATH = "name";
    private static final int EMERGENCY_INFO_NAME_CODE = 1;
    private static final UriMatcher sUriMatcher;
    private static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .path(INFO_PATH)
            .build();
    private String[] EMERGENCY_INFO_NAME_PROJECTION = {PreferenceKeys.KEY_NAME};
    private SharedPreferences mSharedPreferences;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, INFO_PATH, EMERGENCY_INFO_NAME_CODE);
    }

    @Override
    public boolean onCreate() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        int uriMatch = sUriMatcher.match(uri);
        MatrixCursor cursor = new MatrixCursor(EMERGENCY_INFO_NAME_PROJECTION);
        MatrixCursor.RowBuilder builder = cursor.newRow();
        switch (uriMatch) {
            case EMERGENCY_INFO_NAME_CODE:
                String value = mSharedPreferences.getString(PreferenceKeys.KEY_NAME, "");
                builder.add(value);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("getType operation is not supported.");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert operation is not supported.");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("delete operation is not supported.");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update operation is not supported.");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceKeys.KEY_NAME.equals(key)) {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
    }
}