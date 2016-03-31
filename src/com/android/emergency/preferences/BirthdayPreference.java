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

import com.android.emergency.R;
import com.android.emergency.ReloadablePreferenceInterface;
import com.android.internal.annotations.VisibleForTesting;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A base class for {@link Preference} to select a date of birth upon it being clicked. Internally
 * the date of birth is stored as the UTC time in milliseconds at the beginning of the day.
 *
 * <p>The user should pass a String as a default value for the date in milliseconds in UTC time.
 */
public class BirthdayPreference extends Preference implements DatePickerDialog.OnDateSetListener,
        ReloadablePreferenceInterface {
    /** Default value used when no date of birth is set. */
    public static final long DEFAULT_UNSET_VALUE = Long.MIN_VALUE;
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    private final DatePickerDialog mDatePickerDialog;
    private long mDateTimeMillis;
    private boolean mDateSet;
    private final DateFormat mDateFormat;

    /** Creates a new instance initialized with {@code context} and {@code attrs}. */
    public BirthdayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDateTimeMillis = DEFAULT_UNSET_VALUE;
        mDateFormat = DateFormat.getDateInstance();
        mDateFormat.setTimeZone(UTC_TIME_ZONE);
        // Calendar.getInstance() uses default locale
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
        if (!isSelectable() || isNotSet()) {
            removeDateOfBirth.setVisibility(View.GONE);
        } else {
            removeDateOfBirth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callChangeListener(DEFAULT_UNSET_VALUE)) {
                        setDateOfBirth(DEFAULT_UNSET_VALUE);
                    }
                }
            });
        }
    }

    private void showDatePickerDialog(Bundle state) {
        if (state != null) {
            mDatePickerDialog.onRestoreInstanceState(state);
        }
        if (!isNotSet()) {
            Calendar calendar = Calendar.getInstance(UTC_TIME_ZONE);
            calendar.setTimeInMillis(mDateTimeMillis);
            mDatePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        mDatePickerDialog.show();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setDateOfBirth(restorePersistedValue ? getPersistedLong(DEFAULT_UNSET_VALUE)
                : (Long) defaultValue);
    }

    @Override
    public void reloadFromPreference() {
        setDateOfBirth(getPersistedLong(DEFAULT_UNSET_VALUE));
    }


    @Override
    public boolean isNotSet() {
        return mDateTimeMillis == DEFAULT_UNSET_VALUE;
    }

    /**
     * Returns the date of birth in milliseconds in UTC time.
     */
    @VisibleForTesting
    public long getDateOfBirth() {
        return mDateTimeMillis;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(UTC_TIME_ZONE);
        calendar.clear(); // Clears hour, minutes and seconds
        calendar.set(year, month, day);
        long dateTimeInMillis = calendar.getTimeInMillis();
        if (callChangeListener(dateTimeInMillis)) {
            setDateOfBirth(dateTimeInMillis);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String longValueAsStr = a.getString(index);
        if (TextUtils.isEmpty(longValueAsStr)) {
            return DEFAULT_UNSET_VALUE;
        }
        return Long.parseLong(longValueAsStr);
    }

    @Override
    public CharSequence getSummary() {
        if (isNotSet()) {
            return super.getSummary();
        } else {
            return mDateFormat.format(new Date(mDateTimeMillis));
        }
    }

    /**
     * Sets the date of birth in millis in UTC time.
     */
    @VisibleForTesting
    public void setDateOfBirth(long dateTimeMillis) {
        // Always persist/notify the first time.
        final boolean changed = mDateTimeMillis != dateTimeMillis;
        if (changed || !mDateSet) {
            mDateSet = true;
            mDateTimeMillis = dateTimeMillis;
            persistLong(mDateTimeMillis);
            if(changed) {
                notifyChanged();
            }
        }
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

    @VisibleForTesting
    public DatePickerDialog getDatePickerDialog() {
        return mDatePickerDialog;
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