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

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/** A base class for {@link Preference} to select a date upon it being clicked. */
public class DatePreference extends Preference implements DatePickerDialog.OnDateSetListener,
        ReloadablePreferenceInterface {
    private static final String SEPARATOR = "/";
    private static final int YEAR_INDEX = 0;
    private static final int MONTH_INDEX = 1;
    private static final int DAY_INDEX = 2;

    private final DatePickerDialog mDatePickerDialog;
    private int mYear;
    private int mMonth;
    private int mDay;
    private boolean mDateExists = false;
    private final DateFormat mDateFormat;

    /** Creates a new instance initialized with {@code context} and {@code attrs}. */
    public DatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDateFormat = SimpleDateFormat.getDateInstance();
        final Calendar calendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(
                context,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.setTitle(getTitle());
        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDatePickerDialog(null);
                return true;
            }
        });
        setWidgetLayoutResource(R.layout.preference_remove_dob_widget);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View removeDateOfBirth = view.findViewById(R.id.remove_dob);
        if (!isSelectable() || !mDateExists) {
            removeDateOfBirth.setVisibility(View.GONE);
        } else {
            removeDateOfBirth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setDate("");
                }
            });
        }
    }

    private void showDatePickerDialog(Bundle state) {
        if (state != null) {
            mDatePickerDialog.onRestoreInstanceState(state);
        }
        if (mDateExists) {
            mDatePickerDialog.updateDate(mYear, mMonth, mDay);
        }
        mDatePickerDialog.show();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setDate(restorePersistedValue ? getPersistedString("") : (String) defaultValue);
    }

    @Override
    public void reloadFromPreference() {
        setDate(getPersistedString(""));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = serialize(year, month, day);
        if (callChangeListener(date)) {
            setDate(true /* dateExists */, year, month, day);
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
            return String.format(getContext()
                    .getString(R.string.dob_and_age),
                    computeAge(mYear, mMonth, mDay),
                    convertToLocaleDate(mYear, mMonth, mDay));
        }
    }

    private void setDate(String date) {
        if (!TextUtils.isEmpty(date)) {
            int[] yearMonthDay = deserialize(date, new int[3]);
            setDate(true /* dateExists */,
                    yearMonthDay[YEAR_INDEX],
                    yearMonthDay[MONTH_INDEX],
                    yearMonthDay[DAY_INDEX]);
        } else {
            setDate(false /* dateExists */,
                    mYear,
                    mMonth,
                    mDay);
        }
    }

    private void setDate(boolean dateExists, int year, int month, int day) {
        if (mDateExists != dateExists || year != mYear || month != mMonth || day != mDay) {
            mDateExists = dateExists;
            mYear = year;
            mMonth = month;
            mDay = day;
            String date = mDateExists ? serialize(year, month, day) : "";
            persistString(date);
            notifyChanged();
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

    private int computeAge(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - year;
        if (today.get(Calendar.MONTH) < month ||
                (today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) < day)) {
            age--;
        }
        // Return 0 if the user specifies a date of birth in the future.
        return Math.max(0, age);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDatePickerDialog == null || !mDatePickerDialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = mDatePickerDialog.onSaveInstanceState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDatePickerDialog(myState.dialogBundle);
        }
    }

    private static class SavedState extends BaseSavedState {
        boolean isDialogShowing;
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
