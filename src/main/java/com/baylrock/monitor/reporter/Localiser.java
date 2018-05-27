package com.baylrock.monitor.reporter;

public interface Localiser {

    Localiser DEFAULT = (key, args) -> key;

    String localise(String key, Object... args);

    default String localise(String key) {
        return localise(key, (Object[]) null);
    }
}
