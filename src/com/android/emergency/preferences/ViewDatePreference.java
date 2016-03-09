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
import android.util.AttributeSet;

import com.android.emergency.R;

import java.util.Calendar;

/**
 * Custom {@link DatePreference} that shows the age of the user together with the date of birth
 * in the summary.
 */
public class ViewDatePreference extends DatePreference {
    public ViewDatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        Calendar dateOfBirth = getDate();
        if (dateOfBirth == null) {
            return super.getSummary();
        } else {
            return String.format(getContext().getString(R.string.dob_and_age),
                    computeAge(dateOfBirth),
                    super.getSummary());
        }
    }

    private int computeAge(Calendar dateOfBirth) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dateOfBirth.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dateOfBirth.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) <
                                dateOfBirth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        // Return 0 if the user specifies a date of birth in the future.
        return Math.max(0, age);
    }
}
