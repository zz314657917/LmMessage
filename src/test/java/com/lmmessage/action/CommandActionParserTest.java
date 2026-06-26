package com.lmmessage.action;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CommandActionParserTest {
    private final CommandActionParser parser = new CommandActionParser();

    @Test
    void parsesConsoleCommandAndStripsSlash() {
        CommandActionParseResult result = parser.parse(
                "console:/say hello",
                allowed("console", "player", "op"),
                true
        );

        assertTrue(result.accepted());
        assertEquals(CommandPrefix.CONSOLE, result.action().prefix());
        assertEquals("say hello", result.action().command());
    }

    @Test
    void rejectsDisabledOpPrefix() {
        CommandActionParseResult result = parser.parse(
                "op:gamemode creative",
                allowed("console", "player", "op"),
                false
        );

        assertFalse(result.accepted());
        assertEquals("op command prefix is disabled", result.rejectionReason());
    }

    @Test
    void rejectsUnknownPrefixAndLineBreaks() {
        assertFalse(parser.parse("shell:whoami", allowed("console"), true).accepted());
        assertFalse(parser.parse("console:say hi\nstop", allowed("console"), true).accepted());
    }

    private Set<String> allowed(String... prefixes) {
        Set<String> allowed = new LinkedHashSet<String>();
        for (String prefix : prefixes) {
            allowed.add(prefix);
        }
        return allowed;
    }
}
