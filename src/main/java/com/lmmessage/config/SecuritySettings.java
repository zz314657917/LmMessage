package com.lmmessage.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class SecuritySettings {
    private final Set<String> allowedCommandPrefixes;
    private final boolean opPrefixEnabled;
    private final boolean logCommandActions;

    public SecuritySettings(Set<String> allowedCommandPrefixes, boolean opPrefixEnabled, boolean logCommandActions) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        if (allowedCommandPrefixes != null) {
            for (String prefix : allowedCommandPrefixes) {
                if (prefix != null && !prefix.trim().isEmpty()) {
                    normalized.add(prefix.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        this.allowedCommandPrefixes = Collections.unmodifiableSet(normalized);
        this.opPrefixEnabled = opPrefixEnabled;
        this.logCommandActions = logCommandActions;
    }

    public Set<String> allowedCommandPrefixes() {
        return allowedCommandPrefixes;
    }

    public boolean opPrefixEnabled() {
        return opPrefixEnabled;
    }

    public boolean logCommandActions() {
        return logCommandActions;
    }
}
