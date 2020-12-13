package com.elytraforce.bungeesuite.punish;

import com.elytraforce.bungeesuite.config.PluginConfig;

public class LockoutController {
    public static boolean isLockdown() {
        return PluginConfig.get().getMaintenance();
    }
}
