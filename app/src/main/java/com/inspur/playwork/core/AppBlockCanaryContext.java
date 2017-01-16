package com.inspur.playwork.core;

import com.github.moduth.blockcanary.BlockCanaryContext;

import java.util.List;

/**
 * Created by fan on 16-11-1.
 */
class AppBlockCanaryContext extends BlockCanaryContext {
    private static final String TAG = "AppBlockCanaryContext";

    @Override
    public String provideQualifier() {
        return super.provideQualifier();
    }

    @Override
    public String provideUid() {
        return "uid";
    }

    @Override
    public int provideMonitorDuration() {
        return super.provideMonitorDuration();
    }

    @Override
    public int provideBlockThreshold() {
        return super.provideBlockThreshold();
    }

    @Override
    public int provideDumpInterval() {
        return super.provideDumpInterval();
    }

    @Override
    public String providePath() {
        return "/blockcancary/performance";
    }

    @Override
    public boolean displayNotification() {
        return true;
    }

    @Override
    public List<String> concernPackages() {
        return null;
    }

    @Override
    public boolean filterNonConcernStack() {
        return false;
    }
}
