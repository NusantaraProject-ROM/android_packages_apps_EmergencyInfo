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

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.AttributeSet;

import com.android.emergency.R;

/**
 * Custom {@link EmergencyListPreference} that provides accessible strings for the '+' and '-'
 * symbols used in blood types.
 */
public class BloodTypeListPreference extends EmergencyListPreference {
    private String[] mContentDescriptions;

    public BloodTypeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContentDescriptions =
                getContext().getResources().getStringArray(R.array.blood_type_content_description);
        // Override entries with accessible entries.
        setEntries(createAccessibleEntries(getEntries(), mContentDescriptions));
    }

    @Override
    public CharSequence getSummary() {
        final String value = getValue();
        if (TextUtils.isEmpty(value)) {
            return super.getSummary();
        } else {
            return createAccessibleSequence(value, mContentDescriptions[findIndexOfValue(value)]);
        }
    }

    private static CharSequence[] createAccessibleEntries(CharSequence entries[],
                                                   String[] contentDescriptions) {
        CharSequence[] accessibleEntries = new CharSequence[entries.length];
        for (int i = 0; i < entries.length; i++) {
            accessibleEntries[i] = createAccessibleSequence(entries[i], contentDescriptions[i]);
        }
        return accessibleEntries;
    }

    private static SpannableString createAccessibleSequence(CharSequence displayText,
                                                            String accessibleText) {
        SpannableString str = new SpannableString(displayText);
        str.setSpan(new TtsSpan.TextBuilder(accessibleText).build(), 0,
                displayText.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return str;
    }
}
