package com.elytraforce.bungeesuite.config;

import com.elytraforce.aUtils.config.BConfig;

public class TestConfig extends BConfig {
    @Override
    public String filePosition() {
        return "shitass.yml";
    }

    @ConfigField(location = "please.help")
    public String cumGod = "hi";
}
