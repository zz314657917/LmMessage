package com.lmmessage.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ResourceSync {
    private final JavaPlugin plugin;

    public ResourceSync(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public File sync(String resourcePath, boolean replace) {
        File target = new File(plugin.getDataFolder(), resourcePath);
        if (target.isFile() && !replace) {
            return target;
        }
        File parent = target.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            plugin.getLogger().warning("无法创建资源目录: " + parent.getPath());
            return target;
        }
        try (InputStream input = plugin.getResource(resourcePath)) {
            if (input == null) {
                plugin.getLogger().warning("插件内缺少资源: " + resourcePath);
                return target;
            }
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            plugin.getLogger().warning("同步资源失败: " + resourcePath + ", reason=" + exception.getMessage());
        }
        return target;
    }
}
