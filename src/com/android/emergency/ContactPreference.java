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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;


/**
 * A {@link Preference} to display a contact using the specified URI string.
 */
public class ContactPreference extends Preference {

    private final Uri mUri;
    private final DeleteContactListener mDeleteContactListener;
    private final String mName;

    /**
     * Listener for deleting a contact.
     */
    public interface DeleteContactListener {
        /**
         * Callback to delete a contact.
         */
        void onContactDelete(Uri contactUri);
    }

    /**
     * Instantiates a ContactPreference that displays an emergency contact, taking in a Context and
     * the Uri, name and phone number of the contact and a listener to be informed when clicking on
     * the delete icon.
     */
    public ContactPreference(Context context,
                             @NonNull Uri contactUri,
                             @NonNull String contactName,
                             @NonNull DeleteContactListener deleteContactListener) {
        super(context);
        mUri = contactUri;
        mName = contactName;
        mDeleteContactListener = deleteContactListener;
        setTitle(mName);
        setWidgetLayoutResource(R.layout.preference_user_delete_widget);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View deleteContactIcon = view.findViewById(R.id.delete_contact);
        if (deleteContactIcon != null) {
            deleteContactIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(String.format(getContext()
                            .getString(R.string.remove_contact), mName));
                    builder.setPositiveButton(getContext().getString(R.string.remove),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    mDeleteContactListener.onContactDelete(mUri);
                                }
                            }).setNegativeButton(getContext().getString(R.string.cancel), null);
                    builder.create().show();
                }
            });
        }
    }

    /**
     * Calls the contact.
     */
    public void callContact() {
        String phoneNumber = EmergencyContactManager.getNumber(getContext(), mUri);
        if (phoneNumber != null) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                MetricsLogger.action(getContext(), MetricsEvent.ACTION_CALL_EMERGENCY_CONTACT);
                getContext().startActivity(callIntent);
            }
        } else {
            // TODO: Show dialog saying that there is no number.
        }
    }

    /**
     * Displays a contact card for the contact.
     */
    public void displayContact() {
        Intent contactIntent = new Intent(Intent.ACTION_VIEW);
        contactIntent.setData(mUri);
        getContext().startActivity(contactIntent);
    }
}
