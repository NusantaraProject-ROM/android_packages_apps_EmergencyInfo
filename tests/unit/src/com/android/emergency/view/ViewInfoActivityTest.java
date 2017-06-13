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
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.emergency.ContactTestUtils;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.edit.EditInfoActivity;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ViewInfoActivity}. */
@RunWith(AndroidJUnit4.class)
public class ViewInfoActivityTest {
    private Instrumentation mInstrumentation;
    private Context mTargetContext;
    private ViewInfoActivity mActivity;

    @Before
    public void setUp() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
        mTargetContext = mInstrumentation.getTargetContext();
    }

    @After
    public void tearDown() {
        PreferenceManager.getDefaultSharedPreferences(mTargetContext).edit().clear().commit();
    }

    @Test
    public void testInitialState() {
        ViewInfoActivity activity = startViewInfoActivity();

        assertThat(activity.getFragments()).isEmpty();
        assertThat(activity.findViewById(R.id.name_and_dob_linear_layout).getVisibility())
                .isEqualTo(View.GONE);
        assertThat(activity.getTabLayout().getVisibility()).isEqualTo(View.GONE);

        ViewFlipper viewFlipper = (ViewFlipper) activity.findViewById(R.id.view_flipper);
        int noInfoIndex = viewFlipper.indexOfChild(activity.findViewById(R.id.no_info));
        assertThat(viewFlipper.getDisplayedChild()).isEqualTo(noInfoIndex);
    }

    @Test
    public void testNameSet() {
        final String name = "John";
        PreferenceManager.getDefaultSharedPreferences(mTargetContext)
                .edit().putString(PreferenceKeys.KEY_NAME, name).commit();

        ViewInfoActivity activity = startViewInfoActivity();

        assertThat(activity.getFragments()).isEmpty();
        assertThat(activity.getTabLayout().getVisibility()).isEqualTo(View.GONE);
        assertThat(activity.findViewById(R.id.no_info).getVisibility())
                .isEqualTo(View.VISIBLE);

        ViewFlipper viewFlipper = (ViewFlipper) activity.findViewById(R.id.view_flipper);
        int noInfoIndex = viewFlipper.indexOfChild(activity.findViewById(R.id.no_info));
        assertThat(viewFlipper.getDisplayedChild()).isEqualTo(noInfoIndex);

        TextView personalCardLargeItem = (TextView) activity.findViewById(R.id.personal_card_large);
        assertThat(personalCardLargeItem.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(personalCardLargeItem.getText()).isEqualTo(name);
    }

    @Test
    public void testMedicalInfoSet() {
        final String allergies = "Peanuts";
        PreferenceManager.getDefaultSharedPreferences(mTargetContext)
                .edit().putString(PreferenceKeys.KEY_ALLERGIES, allergies).commit();

        ViewInfoActivity activity = startViewInfoActivity();

        assertThat(activity.getTabLayout().getVisibility()).isEqualTo(View.GONE);
        ViewFlipper viewFlipper = (ViewFlipper) activity.findViewById(R.id.view_flipper);
        int tabsIndex = viewFlipper.indexOfChild(activity.findViewById(R.id.tabs));
        assertThat(viewFlipper.getDisplayedChild()).isEqualTo(tabsIndex);

        ArrayList<Pair<String, Fragment>> fragments = activity.getFragments();
        assertThat(fragments).hasSize(1);
        ViewEmergencyInfoFragment viewEmergencyInfoFragment =
                (ViewEmergencyInfoFragment) fragments.get(0).second;
        assertThat(viewEmergencyInfoFragment).isNotNull();
    }

    @Test
    public void testEmergencyContactsSet() {
        final String emergencyContact =
                ContactTestUtils.createContact(mTargetContext.getContentResolver(),
                        "John", "123").toString();
        PreferenceManager.getDefaultSharedPreferences(mTargetContext)
                .edit().putString(PreferenceKeys.KEY_EMERGENCY_CONTACTS, emergencyContact).commit();

        ViewInfoActivity activity = startViewInfoActivity();

        ViewFlipper viewFlipper = (ViewFlipper) activity.findViewById(R.id.view_flipper);
        int tabsIndex = viewFlipper.indexOfChild(activity.findViewById(R.id.tabs));
        assertThat(viewFlipper.getDisplayedChild()).isEqualTo(tabsIndex);

        ArrayList<Pair<String, Fragment>> fragments = activity.getFragments();
        assertThat(fragments).hasSize(1);
        ViewEmergencyContactsFragment viewEmergencyContactsFragment =
                (ViewEmergencyContactsFragment) fragments.get(0).second;
        assertThat(viewEmergencyContactsFragment).isNotNull();

        assertThat(
                ContactTestUtils.deleteContact(mTargetContext.getContentResolver(), "John", "123"))
                .isTrue();
    }

    @Test
    public void testMedicalInfoAndEmergencyContactsSet() {
        final String emergencyContact =
                ContactTestUtils.createContact(mTargetContext.getContentResolver(),
                        "John", "123").toString();
        PreferenceManager.getDefaultSharedPreferences(mTargetContext)
                .edit().putString(PreferenceKeys.KEY_EMERGENCY_CONTACTS, emergencyContact).commit();
        final String allergies = "Peanuts";
        PreferenceManager.getDefaultSharedPreferences(mTargetContext)
                .edit().putString(PreferenceKeys.KEY_ALLERGIES, allergies).commit();

        ViewInfoActivity activity = startViewInfoActivity();

        ViewFlipper viewFlipper = (ViewFlipper) activity.findViewById(R.id.view_flipper);
        int tabsIndex = viewFlipper.indexOfChild(activity.findViewById(R.id.tabs));
        assertThat(viewFlipper.getDisplayedChild()).isEqualTo(tabsIndex);

        ArrayList<Pair<String, Fragment>> fragments = activity.getFragments();
        assertThat(fragments).hasSize(2);
        ViewEmergencyInfoFragment viewEmergencyInfoFragment =
                (ViewEmergencyInfoFragment) fragments.get(0).second;
        assertThat(viewEmergencyInfoFragment).isNotNull();
        ViewEmergencyContactsFragment viewEmergencyContactsFragment =
                (ViewEmergencyContactsFragment) fragments.get(1).second;
        assertThat(viewEmergencyContactsFragment).isNotNull();

        assertThat(
                ContactTestUtils.deleteContact(mTargetContext.getContentResolver(), "John", "123"))
                .isTrue();
    }

    @Test
    public void testCanGoToEditInfoActivityFromMenu() {
        ViewInfoActivity activity = startViewInfoActivity();

        Instrumentation.ActivityMonitor activityMonitor =
                mInstrumentation.addMonitor(EditInfoActivity.class.getName(),
                        null /* result */, false /* block */);

        activity.getMenu().performIdentifierAction(R.id.action_edit, 0 /* flags */);

        EditInfoActivity editInfoActivity = (EditInfoActivity)
                mInstrumentation.waitForMonitorWithTimeout(activityMonitor, 1000 /* timeOut */);
        assertThat(editInfoActivity).isNotNull();
        assertThat(mInstrumentation.checkMonitorHit(activityMonitor, 1 /* minHits */)).isTrue();
        editInfoActivity.finish();
    }

    private ViewInfoActivity startViewInfoActivity() {
        final Intent viewActivityIntent = new Intent(mTargetContext, ViewInfoActivity.class);
        return (ViewInfoActivity) mInstrumentation.startActivitySync(viewActivityIntent);
    }
}
