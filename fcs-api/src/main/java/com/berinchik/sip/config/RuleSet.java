package com.berinchik.sip.config;

import java.util.ArrayList;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface RuleSet {
    String getId();
    ArrayList<Rule> getRules();
}
