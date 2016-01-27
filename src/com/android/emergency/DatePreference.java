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
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/** A base class for {@link DialogPreference} to select a date upon it being clicked. */
public class DatePreference extends DialogPreference {
    private static final String SEPARATOR = "/";
    private static final int YEAR_INDEX = 0;
    private static final int MONTH_INDEX = 1;
    private static final int DAY_INDEX = 2;

    private DatePicker mDatePicker = null;
    private int mYear;
    private int mMonth;
    private int mDay;
    private boolean mDateExists = false;
    private final DateFormat mDateFormat;

    /** Creates a new instance initialized with {@code context} and {@code attrs}. */
    public DatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDateFormat = SimpleDateFormat.getDateInstance();
    }

    @Override
    protected View onCreateDialogView() {
        mDatePicker = (DatePicker) View.inflate(getContext(), R.layout.date_picker, null);
        return mDatePicker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        if (mDateExists) {
            mDatePicker.updateDate(mYear, mMonth, mDay);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String date;
        if (restorePersistedValue) {
            date = getPersistedString("");
        } else {
            date = (String) defaultValue;
        }
        if (date != null && !date.isEmpty()) {
            int[] yearMonthDay = deserialize(date, new int[3]);
            setDate(yearMonthDay[YEAR_INDEX], yearMonthDay[MONTH_INDEX],
                    yearMonthDay[DAY_INDEX]);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int year = mDatePicker.getYear();
            int month = mDatePicker.getMonth();
            int day = mDatePicker.getDayOfMonth();
            String date = serialize(year, month, day);
            if (callChangeListener(date)) {
                setDate(year, month, day);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    public CharSequence getSummary() {
        if (!mDateExists) {
            return super.getSummary();
        } else {
            return convertToLocaleDate(mYear, mMonth, mDay);
        }
    }

    private void setDate(int year, int month, int day) {
        if (!mDateExists ||year != mYear || month != mMonth || day != mDay) {
            mYear = year;
            mMonth = month;
            mDay = day;
            mDateExists = true;
            String date = serialize(year, month, day);
            persistString(date);
            notifyChanged();
            MetricsLogger.action(getContext(),
                    MetricsEvent.ACTION_EDIT_EMERGENCY_INFO_FIELD, getKey());
        }
    }

    private static String serialize(int year, int month, int day) {
        return year + SEPARATOR + month + SEPARATOR + day;
    }

    private static int[] deserialize(String date, int[] yearMonthDay) {
        if (yearMonthDay == null || yearMonthDay.length < 3) {
            yearMonthDay = new int[3];
        }
        String parts[] = date.split(SEPARATOR);
        yearMonthDay[YEAR_INDEX] = Integer.parseInt(parts[YEAR_INDEX]);
        yearMonthDay[MONTH_INDEX] = Integer.parseInt(parts[MONTH_INDEX]);
        yearMonthDay[DAY_INDEX] = Integer.parseInt(parts[DAY_INDEX]);
        return yearMonthDay;
    }

    private String convertToLocaleDate(int year, int month, int day) {
        return mDateFormat.format(new GregorianCalendar(year, month, day).getTime());
    }
}
