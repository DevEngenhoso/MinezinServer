package com.engenhoso.serverplugin.core.module;

public interface PluginModule {

    String getName();

    void onEnable();

    default void onDisable() {
    }
}