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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.android.settingslib.drawable.CircleFramedDrawable;
/**
 * Utility class to handle user photos.
 */
public class PhotoUtils {

    /** Encircles {@code photo} it it's not null. Otherwise it returns a default circle user icon. */
    public static Drawable encircleUserPhoto(Bitmap photo, Context context) {
        if (photo == null) {
            photo = convertToBitmap(context.getResources().getDrawable(
                    R.drawable.ic_person_black_24dp));
        }
        return new CircleFramedDrawable(photo,
                (int) context.getResources().getDimension(R.dimen.circle_avatar_size));
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

    // Private constructor to prevent instantiation
    private PhotoUtils() {
    }
}
