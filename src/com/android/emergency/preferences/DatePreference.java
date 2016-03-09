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
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import com.android.emergency.R;
import com.android.emergency.ReloadablePreferenceInterface;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A base class for {@link Preference} to select a date upon it being clicked.
 *
 * <p>The user should pass a String as a default value for the date in milliseconds since epoch.
 */
public class DatePreference extends Preference implements DatePickerDialog.OnDateSetListener,
        ReloadablePreferenceInterface {
    private static final long DEFAULT_UNSET_VALUE = Long.MIN_VALUE;
    private final DatePickerDialog mDatePickerDialog;
    private long mDateTimeMillis;
    private boolean mDateSet;
    private final DateFormat mDateFormat = DateFormat.getDateInstance();

    /** Creates a new instance initialized with {@code context} and {@code attrs}. */
    public DatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
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
                    setDate(DEFAULT_UNSET_VALUE);
                }
            });
        }
    }

    private void showDatePickerDialog(Bundle state) {
        if (state != null) {
            mDatePickerDialog.onRestoreInstanceState(state);
        }
        if (!isNotSet()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mDateTimeMillis);
            mDatePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        mDatePickerDialog.show();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setDate(restorePersistedValue ? getPersistedLong(DEFAULT_UNSET_VALUE) : (Long) defaultValue);
    }

    @Override
    public void reloadFromPreference() {
        setDate(getPersistedLong(DEFAULT_UNSET_VALUE));
    }


    @Override
    public boolean isNotSet() {
        return mDateTimeMillis == DEFAULT_UNSET_VALUE;
    }

    public long getDate() {
        return mDateTimeMillis;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(); // Clears hour, minutes and seconds
        calendar.set(year, month, day);
        long dateTimeInMillis = calendar.getTimeInMillis();
        if (callChangeListener(dateTimeInMillis)) {
            setDate(dateTimeInMillis);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Long.parseLong(a.getString(index));
    }

    @Override
    public CharSequence getSummary() {
        if (isNotSet()) {
            return super.getSummary();
        } else {
            return mDateFormat.format(new Date(mDateTimeMillis));
        }
    }

    private void setDate(long dateTimeMillis) {
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