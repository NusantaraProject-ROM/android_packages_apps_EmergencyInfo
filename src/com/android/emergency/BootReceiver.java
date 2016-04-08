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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.android.emergency.edit.EditEmergencyInfoFragment;
import com.android.emergency.view.ViewEmergencyContactsFragment;
import com.android.emergency.view.ViewInfoActivity;

/**
 * Broadcast receiver which handles the BOOT_COMPLETED intent and enables or disables
 * {@link ViewInfoActivity} depending on information being available.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Enable ViewInfoActivity if the user input some info. Otherwise, disable it.
        PackageManager pm = context.getPackageManager();
        if (ViewEmergencyContactsFragment.hasAtLeastOneEmergencyContact(context)
                || EditEmergencyInfoFragment.hasAtLeastOnePreferenceSet(context)) {
            pm.setComponentEnabledSetting(new ComponentName(context, ViewInfoActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(new ComponentName(context, ViewInfoActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
