package com.lmmessage.config;

import com.lmmessage.rule.RuleDefinition;
import com.lmmessage.rule.RuleTriggerType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class LmMessageConfigLoader {
    private final JavaPlugin plugin;
    private final Logger logger;

    public LmMessageConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public LmMessageSettings load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        boolean debug = plugin.getConfig().getBoolean("debug", false);
        PlayerChatSettings playerChatSettings = new PlayerChatSettings(
                plugin.getConfig().getString("player-chat.default-channel", "default"),
                plugin.getConfig().getString("player-chat.source", "LmMessage")
        );
        ArcartXSettings arcartXSettings = new ArcartXSettings(
                plugin.getConfig().getString("arcartx.chat-ui-id", "LmMessage:chat"),
                plugin.getConfig().getString("arcartx.hud-ui-id", "LmMessage:hud"),
                plugin.getConfig().getString("arcartx.chat-resource", "arcartx/ui/lmmessage_chat.yml"),
                plugin.getConfig().getString("arcartx.hud-resource", "arcartx/ui/lmmessage_hud.yml"),
                plugin.getConfig().getBoolean("arcartx.auto-open-chat-on-join", true),
                plugin.getConfig().getInt("arcartx.hud-visible-ticks", 80)
        );
        RuleSettings ruleSettings = new RuleSettings(
                loadRules("rules.message-rules", RuleTriggerType.START),
                loadRules("rules.action-rules", RuleTriggerType.CONTAIN),
                plugin.getConfig().getBoolean("rules.execute-actions-on-playerchat-events", false)
        );
        SecuritySettings securitySettings = new SecuritySettings(
                loadAllowedPrefixes(),
                plugin.getConfig().getBoolean("security.enable-op-prefix", false),
                plugin.getConfig().getBoolean("security.log-command-actions", true)
        );
        return new LmMessageSettings(debug, playerChatSettings, arcartXSettings, ruleSettings, securitySettings);
    }

    private List<RuleDefinition> loadRules(String path, RuleTriggerType triggerType) {
        List<RuleDefinition> rules = new ArrayList<RuleDefinition>();
        List<Map<?, ?>> rawList = plugin.getConfig().getMapList(path);
        if (rawList == null || rawList.isEmpty()) {
            return rules;
        }
        int index = 0;
        for (Map<?, ?> item : rawList) {
            index++;
            Map<String, Object> section = normalizeMap(item);
            String triggerText = triggerType == RuleTriggerType.START
                    ? stringValue(section, "start", "")
                    : stringValue(section, "contain", "");
            RuleDefinition rule = RuleDefinition.builder(triggerType)
                    .name(stringValue(section, "name", path + "#" + index))
                    .enabled(booleanValue(section, "enabled", true))
                    .triggerText(triggerText)
                    .replacePrefix(stringValue(section, "replacePrefix", stringValue(section, "replace", "")))
                    .channel(stringValue(section, "channel", ""))
                    .sound(stringValue(section, "sound", ""))
                    .cancelOriginal(booleanValue(section, "cancelOriginal", false))
                    .commands(stringListValue(section, "commands"))
                    .build();
            if (!rule.isValid()) {
                logger.warning("忽略空触发文本规则: " + rule.name());
                continue;
            }
            rules.add(rule);
        }
        return rules;
    }

    private Map<String, Object> normalizeMap(Map<?, ?> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> normalized = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() != null) {
                normalized.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return normalized;
    }

    private String stringValue(Map<String, Object> section, String key, String fallback) {
        Object value = section.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private boolean booleanValue(Map<String, Object> section, String key, boolean fallback) {
        Object value = section.get(key);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private List<String> stringListValue(Map<String, Object> section, String key) {
        Object value = section.get(key);
        if (!(value instanceof Iterable<?>)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (Object item : (Iterable<?>) value) {
            if (item != null && !String.valueOf(item).trim().isEmpty()) {
                result.add(String.valueOf(item).trim());
            }
        }
        return result;
    }

    private Set<String> loadAllowedPrefixes() {
        Set<String> prefixes = new LinkedHashSet<String>();
        List<String> configured = plugin.getConfig().getStringList("security.allowed-command-prefixes");
        if (configured.isEmpty()) {
            prefixes.add("console");
            prefixes.add("player");
            prefixes.add("op");
            return prefixes;
        }
        prefixes.addAll(configured);
        return prefixes;
    }
}
