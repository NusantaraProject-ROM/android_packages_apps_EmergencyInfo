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

/**
 * Contains the keys of the preferences used in this app.
 */
public interface PreferenceKeys {

    /** Key for emergency contacts preference */
    public static final String KEY_EMERGENCY_CONTACTS = "emergency_contacts";

    /** Key for the add contact preference */
    public static final String KEY_ADD_CONTACT = "add_contact";


    /** Keys for all editable emergency info preferences */
    public static final String[] KEYS_EMERGENCY_INFO = {"name", "address", "date_of_birth",
            "blood_type", "allergies", "medications", "medical_conditions", "organ_donor"};
}
