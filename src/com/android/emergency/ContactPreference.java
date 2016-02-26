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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settingslib.drawable.CircleFramedDrawable;


/**
 * A {@link Preference} to display a contact using the specified URI string.
 */
public class ContactPreference extends Preference {

    private final Uri mUri;
    private final String mName;
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
     * the Uri, name and phone number of the contact and a listener to be informed when clicking on
     * the delete icon.
     */
    public ContactPreference(Context context,
                             @NonNull Uri contactUri,
                             @NonNull String contactName) {
        super(context);
        setOrder(DEFAULT_ORDER);
        mUri = contactUri;
        mName = contactName;
        setTitle(mName);
        setWidgetLayoutResource(R.layout.preference_user_delete_widget);
        setPersistent(false);

        //TODO: Consider doing the following in a non-UI thread.
        Bitmap photo = EmergencyContactManager.getContactPhoto(context, mUri);
        if (photo == null) {
            photo = convertToBitmap(context.getResources().getDrawable(
                    R.drawable.ic_account_circle));
        }
        Drawable icon = new CircleFramedDrawable(photo,
                (int) context.getResources().getDimension(R.dimen.circle_avatar_size));
        setIcon(icon);
    }

    /** Listener to be informed when a contact preference should be deleted. */
    public void setRemoveContactPreferenceListener(
            RemoveContactPreferenceListener removeContactListener) {
        mRemoveContactPreferenceListener = removeContactListener;
    }

    /**
     * Converts a given drawable icon to a bitmap.
     */
    private static Bitmap convertToBitmap(Drawable icon) {
        if (icon == null) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return bitmap;
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
                            .getString(R.string.remove_contact), mName));
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
        return mUri;
    }

    /**
     * Calls the contact.
     */
    public void callContact(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            MetricsLogger.action(getContext(), MetricsEvent.ACTION_CALL_EMERGENCY_CONTACT);
            getContext().startActivity(callIntent);
        }
    }

    /**
     * Displays a dialog with the contact's name and phone numbers.
     */
    public void showCallContactDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setTitle(mName);
        final String[] phoneNumbers = EmergencyContactManager.getPhoneNumbers(getContext(), mUri);
        //TODO: Discuss with UX the possibility of using a custom list adapter for the phone numbers
        if (phoneNumbers != null && phoneNumbers.length > 0) {
            builderSingle.setItems(phoneNumbers, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    callContact(phoneNumbers[i]);
                }
            });
        } else {
            builderSingle.setMessage(getContext().getResources()
                    .getString(R.string.phone_number_error));
            builderSingle.setPositiveButton(getContext().getString(R.string.ok), null);
        }
        builderSingle.show();
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