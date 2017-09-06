
package sim.android.mtkcit.util;

import android.os.SystemProperties;

public final class FeatureOption {

    /**
     * check if GEMINI is turned on or not
     */
    public static final boolean MTK_GEMINI_SUPPORT = SystemProperties.get("ro.mtk_gemini_support").equals("1");

    public static final boolean MTK_EMMC_SUPPORT = SystemProperties.get("ro.mount.fs").equals("EXT4");

    public static final boolean MTK_2SDCARD_SWAP = SystemProperties.get("ro.mount.swap").equals("1");
}
