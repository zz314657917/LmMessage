package com.lmmessage.config;

import com.lmmessage.rule.RuleDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuleSettings {
    private final List<RuleDefinition> messageRules;
    private final List<RuleDefinition> actionRules;
    private final boolean executeActionsOnPlayerChatEvents;

    public RuleSettings(
            List<RuleDefinition> messageRules,
            List<RuleDefinition> actionRules,
            boolean executeActionsOnPlayerChatEvents
    ) {
        this.messageRules = Collections.unmodifiableList(new ArrayList<RuleDefinition>(messageRules));
        this.actionRules = Collections.unmodifiableList(new ArrayList<RuleDefinition>(actionRules));
        this.executeActionsOnPlayerChatEvents = executeActionsOnPlayerChatEvents;
    }

    public List<RuleDefinition> messageRules() {
        return messageRules;
    }

    public List<RuleDefinition> actionRules() {
        return actionRules;
    }

    public boolean executeActionsOnPlayerChatEvents() {
        return executeActionsOnPlayerChatEvents;
    }
}
