/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.cappu.music.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * JellyBean specific AccessibilityNodeInfo API implementation.
 */
@SuppressLint("NewApi")
class AccessibilityNodeInfoCompatJellyBean {

    @SuppressLint("NewApi")
    public static void addChild(Object info, View child, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).addChild(child, virtualDescendantId);
    }

    @SuppressLint("NewApi")
    public static void setSource(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setSource(root, virtualDescendantId);
    }

    @SuppressLint("NewApi")
    public static boolean isVisibleToUser(Object info) {
        return ((AccessibilityNodeInfo) info).isVisibleToUser();
    }

    @SuppressLint("NewApi")
    public static void setVisibleToUser(Object info, boolean visibleToUser) {
        ((AccessibilityNodeInfo) info).setVisibleToUser(visibleToUser);
    }

    @SuppressLint("NewApi")
    public static boolean performAction(Object info, int action, Bundle arguments) {
        return ((AccessibilityNodeInfo) info).performAction(action, arguments);
    }

    @SuppressLint("NewApi")
    public static void setMovementGranularities(Object info, int granularities) {
        ((AccessibilityNodeInfo) info).setMovementGranularities(granularities);
    }

    @SuppressLint("NewApi")
    public static int getMovementGranularities(Object info) {
        return ((AccessibilityNodeInfo) info).getMovementGranularities();
    }

    @SuppressLint("NewApi")
    public static Object obtain(View root, int virtualDescendantId) {
        return AccessibilityNodeInfo.obtain(root, virtualDescendantId);
    }

    public static Object findFocus(Object info, int focus) {
        return ((AccessibilityNodeInfo) info).findFocus(focus);
    }

    public static Object focusSearch(Object info, int direction) {
        return ((AccessibilityNodeInfo) info).focusSearch(direction);
    }

    public static void setParent(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setParent(root, virtualDescendantId);
    }

    public static boolean isAccessibilityFocused(Object info) {
        return ((AccessibilityNodeInfo) info).isAccessibilityFocused();
    }

    public static void setAccesibilityFocused(Object info, boolean focused) {
        ((AccessibilityNodeInfo) info).setAccessibilityFocused(focused);
    }
}
