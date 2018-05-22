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

import static com.google.common.truth.Truth.assertThat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class EmergencyInfoNameProviderTest {
    private ContentResolver mContentResolver;
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private static final String TESTED_NAME = "John";
    private static final String AUTHORITY = "com.android.emergency.info.name";
    private static final String VALID_CONTENT_PATH = "name";
    private static final String INVALID_CONTENT_PATH = "invalidpath";

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        ShadowContentResolver.registerProviderInternal(AUTHORITY,
                Robolectric.setupContentProvider(EmergencyInfoNameProvider.class));
        mContentResolver = mContext.getContentResolver();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferences.edit().putString(PreferenceKeys.KEY_NAME, TESTED_NAME).commit();
    }

    @Test
    public void query_validContentUri_returnsValidName() {
        final Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path(VALID_CONTENT_PATH)
                .build();

        String result = "";
        try (Cursor cursor = mContentResolver.query(uri, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(PreferenceKeys.KEY_NAME);
                result = index > -1 ? cursor.getString(index) : null;
            }
        }

        assertThat(result).isEqualTo(TESTED_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_invalidContentUri_throws() {
        final Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path(INVALID_CONTENT_PATH)
                .build();

        mContentResolver.query(uri, null, null, null, null);
    }
}
