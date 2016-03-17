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
package com.android.emergency.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.emergency.EmergencyTabActivity;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.preferences.DatePreference;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Activity for viewing emergency information.
 */
public class ViewInfoActivity extends EmergencyTabActivity {
    private TextView mPersonalCardLargeItem;
    private TextView mPersonalCardSmallItem;
    private SharedPreferences mSharedPreferences;
    private LinearLayout mPersonalCard;

    private final DateFormat mDateFormat = DateFormat.getDateInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity_layout);
        // TODO: investigate encryption mode b/27577600
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPersonalCard = (LinearLayout) findViewById(R.id.name_and_dob_linear_layout);
        mPersonalCardLargeItem = (TextView) findViewById(R.id.personal_card_large);
        mPersonalCardSmallItem = (TextView) findViewById(R.id.personal_card_small);

        MetricsLogger.visible(this, MetricsEvent.ACTION_VIEW_EMERGENCY_INFO);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileCard();
    }

    private void loadProfileCard() {
        String name = mSharedPreferences.getString(PreferenceKeys.KEY_NAME, "");
        long dateOfBirthTimeMillis = mSharedPreferences.getLong(PreferenceKeys.KEY_DATE_OF_BIRTH,
                DatePreference.DEFAULT_UNSET_VALUE);
        boolean nameEmpty = TextUtils.isEmpty(name);
        boolean dateOfBirthNotSet = dateOfBirthTimeMillis == DatePreference.DEFAULT_UNSET_VALUE;
        if (nameEmpty && dateOfBirthNotSet) {
            mPersonalCard.setVisibility(View.GONE);
        } else {
            mPersonalCard.setVisibility(View.VISIBLE);
            if (!dateOfBirthNotSet) {
                int age = computeAge(dateOfBirthTimeMillis);
                String localizedDob = mDateFormat.format(new Date(dateOfBirthTimeMillis));
                if (nameEmpty) {
                    // Display date of birth info in two lines: age and then date of birth
                    mPersonalCardLargeItem.setText(String.format(getString(R.string.age),
                            age));
                    mPersonalCardSmallItem.setText(localizedDob);
                } else {
                    mPersonalCardLargeItem.setText(name);
                    mPersonalCardSmallItem.setText(String.format(getString(R.string.dob_and_age),
                            age,
                            localizedDob));
                }
            } else {
                mPersonalCardLargeItem.setText(name);
                mPersonalCardSmallItem.setText("");
            }
        }
    }

    private int computeAge(long dateOfBirthTimeMillis) {
        // Today is in the default time zone of the phone
        Calendar today = Calendar.getInstance();
        Calendar dateOfBirthCalendar = Calendar.getInstance();
        dateOfBirthCalendar.setTimeInMillis(dateOfBirthTimeMillis);
        int age = today.get(Calendar.YEAR) - dateOfBirthCalendar.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dateOfBirthCalendar.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dateOfBirthCalendar.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) <
                                dateOfBirthCalendar.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        // Return 0 if the user specifies a date of birth in the future.
        return Math.max(0, age);
    }

    @Override
    public boolean isInViewMode() {
        return true;
    }

    @Override
    public String getActivityTitle() {
        return getString(R.string.app_label);
    }
}