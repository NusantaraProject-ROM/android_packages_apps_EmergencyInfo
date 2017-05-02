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

import static com.google.common.truth.Truth.assertThat;

import android.app.Fragment;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.emergency.ContactTestUtils;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.edit.EditInfoActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Tests for {@link ViewInfoActivity}.
 */
@LargeTest
public class ViewInfoActivityTest extends ActivityInstrumentationTestCase2<ViewInfoActivity> {
    private ArrayList<Pair<String, Fragment>> mFragments;
    private LinearLayout mPersonalCard;
    private TextView mPersonalCardLargeItem;
    private ViewFlipper mViewFlipper;
    private int mNoInfoIndex;
    private int mTabsIndex;

    public ViewInfoActivityTest() {
        super(ViewInfoActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPersonalCard = (LinearLayout)  getActivity().findViewById(R.id.name_and_dob_linear_layout);
        mPersonalCardLargeItem = (TextView)  getActivity().findViewById(R.id.personal_card_large);
        mViewFlipper = (ViewFlipper) getActivity().findViewById(R.id.view_flipper);
        mNoInfoIndex = mViewFlipper
                .indexOfChild(getActivity().findViewById(R.id.no_info));
        mTabsIndex = mViewFlipper.indexOfChild(getActivity().findViewById(R.id.tabs));

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
    }

    @Override
    protected void tearDown() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
        super.tearDown();
    }

    public void testInitialState() throws Throwable {
        onPause();
        onResume();

        mFragments = getActivity().getFragments();
        assertThat(mFragments).isEmpty();
        assertThat(mPersonalCard.getVisibility()).isEqualTo(View.GONE);
        assertThat(getActivity().getTabLayout().getVisibility()).isEqualTo(View.GONE);
        assertThat(mViewFlipper.getDisplayedChild()).isEqualTo(mNoInfoIndex);
    }

    public void testNameSet() throws Throwable {
        onPause();

        final String name = "John";
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit().putString(PreferenceKeys.KEY_NAME, name).commit();

        onResume();

        mFragments = getActivity().getFragments();
        assertThat(mFragments).isEmpty();
        assertThat(getActivity().getTabLayout().getVisibility()).isEqualTo(View.GONE);
        assertThat(getActivity().findViewById(R.id.no_info).getVisibility())
                .isEqualTo(View.VISIBLE);
        assertThat(mViewFlipper.getDisplayedChild()).isEqualTo(mNoInfoIndex);
        assertThat(mPersonalCardLargeItem.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mPersonalCardLargeItem.getText()).isEqualTo(name);
    }

    public void testEmergencyInfoSet() throws Throwable {
        onPause();

        final String allergies = "Peanuts";
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit().putString(PreferenceKeys.KEY_ALLERGIES, allergies).commit();

        onResume();

        mFragments = getActivity().getFragments();
        assertThat(getActivity().getTabLayout().getVisibility()).isEqualTo(View.GONE);
        assertThat(mViewFlipper.getDisplayedChild()).isEqualTo(mTabsIndex);
        assertThat(mFragments.size()).isEqualTo(1);
        ViewEmergencyInfoFragment viewEmergencyInfoFragment =
                (ViewEmergencyInfoFragment) mFragments.get(0).second;
        assertThat(viewEmergencyInfoFragment).isNotNull();
    }

    public void testEmergencyContactSet() throws Throwable {
        onPause();

        final String emergencyContact =
                ContactTestUtils.createContact(getActivity().getContentResolver(),
                        "John", "123").toString();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit().putString(PreferenceKeys.KEY_EMERGENCY_CONTACTS, emergencyContact).commit();

        onResume();

        mFragments = getActivity().getFragments();
        assertThat(mViewFlipper.getDisplayedChild()).isEqualTo(mTabsIndex);
        assertThat(mFragments.size()).isEqualTo(1);
        assertThat(getActivity().getTabLayout().getVisibility()).isEqualTo(View.GONE);
        ViewEmergencyContactsFragment viewEmergencyContactsFragment =
                (ViewEmergencyContactsFragment) mFragments.get(0).second;
        assertThat(viewEmergencyContactsFragment).isNotNull();

        assertThat(
                ContactTestUtils.deleteContact(getActivity().getContentResolver(), "John", "123"))
                .isTrue();
    }

    public void testInfoAndEmergencyContactsSet() throws Throwable {
        onPause();

        final String emergencyContact =
                ContactTestUtils.createContact(getActivity().getContentResolver(),
                        "John", "123").toString();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(PreferenceKeys.KEY_EMERGENCY_CONTACTS, emergencyContact).commit();

                final String allergies = "Peanuts";
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit().putString(PreferenceKeys.KEY_ALLERGIES, allergies).commit();

        onResume();

        mFragments = getActivity().getFragments();
        assertThat(mViewFlipper.getDisplayedChild()).isEqualTo(mTabsIndex);
        assertThat(mFragments.size()).isEqualTo(2);
        assertThat(getActivity().getTabLayout().getVisibility()).isEqualTo(View.VISIBLE);
        ViewEmergencyInfoFragment viewEmergencyInfoFragment =
                (ViewEmergencyInfoFragment) mFragments.get(0).second;
        assertThat(viewEmergencyInfoFragment).isNotNull();
        ViewEmergencyContactsFragment viewEmergencyContactsFragment =
                (ViewEmergencyContactsFragment) mFragments.get(1).second;
        assertThat(viewEmergencyContactsFragment).isNotNull();

        assertThat(
                ContactTestUtils.deleteContact(getActivity().getContentResolver(), "John", "123"))
                .isTrue();
    }

    public void testCanGoToEditInfoActivityFromMenu() {
        final ViewInfoActivity activity = getActivity();

        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(EditInfoActivity.class.getName(),
                        null /* result */, false /* block */);

        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        getInstrumentation().invokeMenuActionSync(activity, R.id.action_edit, 0 /* flags */);

        EditInfoActivity editInfoActivity = (EditInfoActivity)
                getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 1000 /* timeOut */);
        assertThat(editInfoActivity).isNotNull();
        assertThat(getInstrumentation().checkMonitorHit(activityMonitor, 1 /* minHits */)).isTrue();
        editInfoActivity.finish();
    }

    public void testCanGoToEditInfoActivityFromBroadcast() {
        String action = "android.emergency.EDIT_EMERGENCY_CONTACTS";
        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(new IntentFilter(action),
                        null /* result */, false /* block */);
        getActivity().startActivity(new Intent(action));

        getInstrumentation().waitForIdleSync();
        assertThat(getInstrumentation().checkMonitorHit(activityMonitor, 1 /* minHits */)).isTrue();

        EditInfoActivity editInfoActivity = (EditInfoActivity)
                getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 1000 /* timeOut */);
        assertThat(editInfoActivity).isNotNull();
        // The contacts tab index is 1
        assertThat(editInfoActivity.getSelectedTabPosition()).isEqualTo(1);
        editInfoActivity.finish();
    }

    private void onPause() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnPause(getActivity());
            }
        });
    }

    private void onResume() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnResume(getActivity());
            }
        });
        getInstrumentation().waitForIdleSync();
    }
}

