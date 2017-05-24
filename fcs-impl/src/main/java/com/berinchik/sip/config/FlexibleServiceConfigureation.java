package com.berinchik.sip.config;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public class FlexibleServiceConfigureation implements ServiceConfig {

    JSONObject configuration;

    public FlexibleServiceConfigureation(JSONObject configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<Target> getTargetList() {
        return null;
    }

    @Override
    public int getDefaultPeriod() {
        return 0;
    }

    @Override
    public List<Rule> getRuleset() {
        return null;
    }
}
