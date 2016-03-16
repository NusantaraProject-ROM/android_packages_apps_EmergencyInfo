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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.android.emergency.EmergencyContactManager;
import com.android.emergency.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settingslib.drawable.CircleFramedDrawable;


/**
 * A {@link Preference} to display or call a contact using the specified URI string.
 */
public class ContactPreference extends Preference {

    private final EmergencyContactManager.Contact mContact;
    @Nullable private RemoveContactPreferenceListener mRemoveContactPreferenceListener;

    /**
     * Listener for removing a contact.
     */
    public interface RemoveContactPreferenceListener {
        /**
         * Callback to remove a contact preference.
         */
        void onRemoveContactPreference(ContactPreference preference);
    }

    /**
     * Instantiates a ContactPreference that displays an emergency contact, taking in a Context and
     * the Uri.
     */
    public ContactPreference(Context context, @NonNull Uri contactUri) {
        super(context);
        setOrder(DEFAULT_ORDER);
        // This preference is reloaded each time onResume, so it is guaranteed to have a fresh
        // representation of the contact each time we click on this preference to display or to call
        // the contact.
        mContact = EmergencyContactManager.getContact(context, contactUri);
        setTitle(mContact.getName());
        String summary = mContact.getPhoneType() == null ?
                mContact.getPhoneNumber() :
                String.format(
                        context.getResources().getString(R.string.phone_type_and_phone_number),
                        mContact.getPhoneType(),
                        mContact.getPhoneNumber());
        setSummary(summary);
        setWidgetLayoutResource(R.layout.preference_user_delete_widget);
        setPersistent(false);

        //TODO: Consider doing the following in a non-UI thread.
        Drawable icon;
        if (mContact.getPhoto() != null) {
            icon = new CircleFramedDrawable(mContact.getPhoto(),
                    (int) context.getResources().getDimension(R.dimen.circle_avatar_size));
        } else {
            icon = context.getResources().getDrawable(R.drawable.ic_person_black_24dp);
        }
        setIcon(icon);
    }

    /** Listener to be informed when a contact preference should be deleted. */
    public void setRemoveContactPreferenceListener(
            RemoveContactPreferenceListener removeContactListener) {
        mRemoveContactPreferenceListener = removeContactListener;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View deleteContactIcon = view.findViewById(R.id.delete_contact);
        if (mRemoveContactPreferenceListener == null) {
            deleteContactIcon.setVisibility(View.GONE);
        } else {
            deleteContactIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(String.format(getContext()
                            .getString(R.string.remove_contact),
                            mContact.getName()));
                    builder.setPositiveButton(getContext().getString(R.string.remove),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int which) {
                                    if (mRemoveContactPreferenceListener != null) {
                                        mRemoveContactPreferenceListener
                                                .onRemoveContactPreference(ContactPreference.this);
                                    }
                                }
                            }).setNegativeButton(getContext().getString(R.string.cancel), null);
                    builder.create().show();
                }
            });

        }
    }

    public Uri getContactUri() {
        return mContact.getContactUri();
    }

    /**
     * Calls the contact.
     */
    public void callContact() {
        Intent callIntent =
                new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mContact.getPhoneNumber()));
        MetricsLogger.action(getContext(), MetricsEvent.ACTION_CALL_EMERGENCY_CONTACT);
        getContext().startActivity(callIntent);
    }

    /**
     * Displays a contact card for the contact.
     */
    public void displayContact() {
        Intent contactIntent = new Intent(Intent.ACTION_VIEW);
        contactIntent.setData(mContact.getContactLookupUri());
        getContext().startActivity(contactIntent);
    }
}