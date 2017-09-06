package com.android .gallery3d.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Rect;

import com.android.gallery3d.ui.ListSlotView.SlotEntry;
import com.android.gallery3d.ui.ListSlotView.SlotRenderer;
import com.android.gallery3d.ui.ListSlotView.Spec;
import com.android.gallery3d.data.MediaItem;

public class ListLayout {
    private final static String TAG = "Gallery2/listLayout";
    protected static int sActionBarHeight;
    protected static int sViewHeightWhenPortrait = -1;

    public void setActionBarHeight(int height) {
        sActionBarHeight = height;
    }

    public void setViewHeightWhenPortrait(int height) {
        sViewHeightWhenPortrait = height;
    }

    public int getViewHeightWhenPortrait() {
        return sViewHeightWhenPortrait;
    }

    public void setSlotSpec(Spec spec) {}

    public void setPaddingSpec(int paddingTop, int paddingBottom) {}

    public Spec getSlotSpec() {
        return null;
    }

    public boolean setSlotCount(int slotCount) {
        return false;
    }

    public void setViewHeight(int height) {}

    public void refreshSlotMap(int index) {}

    public void setSlotRenderer(SlotRenderer renderer) {}

    public int getSlotCount() {
        return -1;
    }

    public void updateSlotCount(int slotCount) {}

    public Rect getSlotRect(int index, Rect rect) {
        return null;
    }

    public int getSlotWidth() {
        return -1;
    }

    public int getSlotHeight() {
        return -1;
    }

    public void setSize(int width, int height) {}

    public void setScrollPosition(int position) {}

    public int getVisibleStart() {
        return -1;
    }

    public int getVisibleEnd() {
        return -1;
    }

    public int getSlotIndexByPosition(float x, float y) {
        return -1;
    }

    public int getScrollLimit() {
        return -1;
    }

    public void onDataChange(int index, MediaItem item, int size,
            boolean isCameraFolder) {
    }

    public void clearColumnArray(int index, boolean clearAll) {}
    public int getViewWidth() {
        return -1;
    }

    public int getViewHeight() {
        return -1;
    }

    public int getSlotGap() {
        return -1;
    }

    public boolean isFancyLayout() {
        return false;
    }

    public void setForceRefreshFlag(boolean needForceRefresh) {}

    public void setSlotArray(ArrayList<SlotEntry> list,
            HashMap<Integer, ArrayList<SlotEntry>> colMap) {
    }

    public boolean advanceAnimation(long animTime) {
        return false;
    }

    public static interface DataChangeListener {
        public void onDataChange(int index, MediaItem item, int size,
                boolean isCameraFolder, String albumName);
    }
}
