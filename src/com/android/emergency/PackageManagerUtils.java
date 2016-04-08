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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.android.emergency.edit.EditEmergencyInfoFragment;
import com.android.emergency.view.ViewEmergencyContactsFragment;
import com.android.emergency.view.ViewInfoActivity;

/**
 * Utils related to {@link PackageManager}.
 */
public class PackageManagerUtils {

    /**
     * Disables {@link ViewInfoActivity} if no emergency information or emergency contacts were
     * input by the user. Otherwise, it enables it.
     */
    public static void disableViewInfoActivityIfNoInfoAvailable(Context context) {
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

    private PackageManagerUtils() {
        // Prevent instantiation
        throw new UnsupportedOperationException();
    }
}
