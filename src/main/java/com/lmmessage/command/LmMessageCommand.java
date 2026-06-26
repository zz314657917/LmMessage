package com.lmmessage.command;

import com.lmmessage.LmMessagePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class LmMessageCommand implements CommandExecutor, TabCompleter {
    private static final List<String> ROOT_ARGS = Arrays.asList("reload", "debug", "open", "test");
    private static final List<String> TEST_ARGS = Arrays.asList("chat", "hud");

    private final LmMessagePlugin plugin;

    public LmMessageCommand(LmMessagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }
        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if ("reload".equals(subCommand)) {
            return handleReload(sender);
        }
        if ("debug".equals(subCommand)) {
            return handleDebug(sender);
        }
        if ("open".equals(subCommand)) {
            return handleOpen(sender, label, args);
        }
        if ("test".equals(subCommand)) {
            return handleTest(sender, label, args);
        }
        sendUsage(sender, label);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("lmmessage.reload")) {
            sender.sendMessage("§c你没有权限。");
            return true;
        }
        plugin.reloadLmMessage();
        sender.sendMessage("§aLmMessage 已重载。");
        return true;
    }

    private boolean handleDebug(CommandSender sender) {
        if (!sender.hasPermission("lmmessage.debug")) {
            sender.sendMessage("§c你没有权限。");
            return true;
        }
        sender.sendMessage("§6LmMessage Debug");
        sender.sendMessage("§7PlayerChat: §f" + plugin.playerChatStatus());
        sender.sendMessage("§7ArcartX: §f" + plugin.arcartXStatus());
        sender.sendMessage("§7ProtocolLib: §f" + plugin.protocolLibStatus());
        sender.sendMessage("§7Rules: §fmessage=" + plugin.settings().rules().messageRules().size()
                + ", action=" + plugin.settings().rules().actionRules().size());
        return true;
    }

    private boolean handleOpen(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("lmmessage.open")) {
            sender.sendMessage("§c你没有权限。");
            return true;
        }
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("lmmessage.admin")) {
                sender.sendMessage("§c你没有权限为其他玩家打开。");
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§c用法: /" + label + " open <player>");
            return true;
        }
        if (target == null) {
            sender.sendMessage("§c玩家不在线。");
            return true;
        }
        boolean opened = plugin.uiBridge().openChat(target);
        sender.sendMessage(opened ? "§a已打开 AX 聊天栏。" : "§c打开失败: " + plugin.arcartXStatus());
        return true;
    }

    private boolean handleTest(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("lmmessage.test")) {
            sender.sendMessage("§c你没有权限。");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§c用法: /" + label + " test <chat|hud> <message>");
            return true;
        }
        Player target = sender instanceof Player ? (Player) sender : null;
        if (target == null) {
            sender.sendMessage("§c控制台请先用玩家执行测试命令。");
            return true;
        }
        String type = args[1].toLowerCase(Locale.ROOT);
        String message = join(args, 2);
        if ("chat".equals(type)) {
            plugin.uiBridge().appendChat(target, "test", message);
            sender.sendMessage("§a已发送 AX chat 测试消息。");
            return true;
        }
        if ("hud".equals(type)) {
            plugin.uiBridge().showHud(target, "test", message);
            sender.sendMessage("§a已发送 AX HUD 测试消息。");
            return true;
        }
        sender.sendMessage("§c用法: /" + label + " test <chat|hud> <message>");
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("§6LmMessage");
        sender.sendMessage("§7/" + label + " reload");
        sender.sendMessage("§7/" + label + " debug");
        sender.sendMessage("§7/" + label + " open [player]");
        sender.sendMessage("§7/" + label + " test <chat|hud> <message>");
    }

    private String join(String[] args, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int index = startIndex; index < args.length; index++) {
            if (index > startIndex) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(ROOT_ARGS, args[0]);
        }
        if (args.length == 2 && "test".equalsIgnoreCase(args[0])) {
            return filter(TEST_ARGS, args[1]);
        }
        if (args.length == 2 && "open".equalsIgnoreCase(args[0]) && sender.hasPermission("lmmessage.admin")) {
            List<String> names = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String prefix) {
        String normalized = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                result.add(value);
            }
        }
        return result;
    }
}
