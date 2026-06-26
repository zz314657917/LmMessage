package com.lmmessage;

import com.lmmessage.action.CommandActionDispatcher;
import com.lmmessage.command.LmMessageCommand;
import com.lmmessage.config.LmMessageConfigLoader;
import com.lmmessage.config.LmMessageSettings;
import com.lmmessage.integration.arcartx.ArcartXMessageUiBridge;
import com.lmmessage.integration.arcartx.ArcartXRuntime;
import com.lmmessage.integration.playerchat.PlayerChatGateway;
import com.lmmessage.integration.protocollib.ProtocolLibStatus;
import com.lmmessage.listener.PlayerChatEventListener;
import com.lmmessage.listener.PlayerLifecycleListener;
import com.lmmessage.service.ChatSubmissionService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class LmMessagePlugin extends JavaPlugin {
    private LmMessageSettings settings;
    private PlayerChatGateway playerChatGateway;
    private ProtocolLibStatus protocolLibStatus;
    private ArcartXMessageUiBridge uiBridge;
    private ChatSubmissionService chatSubmissionService;
    private PlayerLifecycleListener lifecycleListener;

    @Override
    public void onEnable() {
        settings = new LmMessageConfigLoader(this).load();
        playerChatGateway = new PlayerChatGateway(this);
        protocolLibStatus = new ProtocolLibStatus(this);
        if (!playerChatGateway.available()) {
            getLogger().severe("PlayerChat 未启用，LmMessage 需要 PlayerChat 作为跨服聊天后端。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        uiBridge = new ArcartXMessageUiBridge(this, new ArcartXRuntime());
        CommandActionDispatcher commandActionDispatcher = new CommandActionDispatcher(this);
        chatSubmissionService = new ChatSubmissionService(
                this,
                playerChatGateway,
                uiBridge,
                commandActionDispatcher,
                settings
        );
        boolean axReady = uiBridge.initialize(settings.arcartX(), new ArcartXMessageUiBridge.SubmitHandler() {
            @Override
            public void onSubmit(Player player, String input) {
                chatSubmissionService.handleAxSubmit(player, input);
            }
        });
        if (!axReady) {
            getLogger().severe("ArcartX 初始化失败，LmMessage 将禁用: " + uiBridge.unavailableReason());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new PlayerChatEventListener(chatSubmissionService), this);
        lifecycleListener = new PlayerLifecycleListener(uiBridge, settings);
        Bukkit.getPluginManager().registerEvents(lifecycleListener, this);
        registerCommand();
        if (settings.arcartX().autoOpenChatOnJoin()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                uiBridge.openChat(player);
            }
        }
        getLogger().info("LmMessage enabled. PlayerChat backend=true, " + protocolLibStatus.statusLine());
    }

    @Override
    public void onDisable() {
        if (uiBridge != null) {
            uiBridge.shutdown();
        }
    }

    public void reloadLmMessage() {
        LmMessageSettings reloaded = new LmMessageConfigLoader(this).load();
        settings = reloaded;
        if (chatSubmissionService != null) {
            chatSubmissionService.updateSettings(settings);
        }
        if (lifecycleListener != null) {
            lifecycleListener.updateSettings(settings);
        }
        if (uiBridge != null) {
            uiBridge.reload(settings.arcartX());
        }
    }

    public LmMessageSettings settings() {
        return settings;
    }

    public ArcartXMessageUiBridge uiBridge() {
        return uiBridge;
    }

    public String playerChatStatus() {
        return playerChatGateway != null && playerChatGateway.available() ? "available" : "missing";
    }

    public String arcartXStatus() {
        if (uiBridge == null) {
            return "not initialized";
        }
        return uiBridge.available() ? "available" : uiBridge.unavailableReason();
    }

    public String protocolLibStatus() {
        return protocolLibStatus == null ? "unknown" : protocolLibStatus.statusLine();
    }

    private void registerCommand() {
        PluginCommand command = getCommand("lmmessage");
        if (command == null) {
            getLogger().warning("plugin.yml 缺少 lmmessage 命令。");
            return;
        }
        LmMessageCommand executor = new LmMessageCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
