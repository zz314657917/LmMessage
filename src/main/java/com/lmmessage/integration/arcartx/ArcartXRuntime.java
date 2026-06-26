package com.lmmessage.integration.arcartx;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ArcartXRuntime {
    private static final String API_CLASS = "priv.seventeen.artist.arcartx.api.ArcartXAPI";
    private static final String CALLBACK_TYPE_CLASS = "priv.seventeen.artist.arcartx.core.ui.adapter.CallBackType";
    private static final String UI_CALLBACK_CLASS = "priv.seventeen.artist.arcartx.util.collections.UICallBack";
    private static final String[] PLUGIN_NAMES = new String[]{"ArcartX", "ArcartXPlugin"};

    private Object uiRegistry;
    private String unavailableReason = "ArcartX API not initialized";
    private final Map<String, String> registeredUiIds = new ConcurrentHashMap<String, String>();
    private final Map<String, CallbackRegistration> packetCallbacks = new ConcurrentHashMap<String, CallbackRegistration>();

    public interface PacketCallback {
        void onPacket(Player player, String uiId, String identifier, List<String> data);
    }

    public boolean initialize() {
        registeredUiIds.clear();
        packetCallbacks.clear();
        uiRegistry = null;
        Plugin arcartXPlugin = findArcartXPlugin();
        if (arcartXPlugin == null && !isApiClassVisible()) {
            unavailableReason = "ArcartX/ArcartXPlugin 未启用。";
            return false;
        }
        try {
            Class<?> apiType = arcartXPlugin == null
                    ? Class.forName(API_CLASS)
                    : Class.forName(API_CLASS, true, arcartXPlugin.getClass().getClassLoader());
            Method method = apiType.getMethod("getUIRegistry");
            uiRegistry = method.invoke(null);
            if (uiRegistry == null) {
                unavailableReason = "ArcartXAPI.getUIRegistry() 返回 null。";
                return false;
            }
            unavailableReason = "";
            return true;
        } catch (Throwable throwable) {
            unavailableReason = "无法初始化 ArcartX UIRegistry: " + safeMessage(throwable);
            return false;
        }
    }

    public boolean isAvailable() {
        return uiRegistry != null || initialize();
    }

    public String getUnavailableReason() {
        return unavailableReason == null || unavailableReason.trim().isEmpty()
                ? "ArcartX UIRegistry 不可用。" : unavailableReason;
    }

    public boolean register(String uiId, File file) {
        if (!isAvailable() || isBlank(uiId) || file == null || !file.isFile()) {
            unavailableReason = "ArcartX UI 注册失败: uiId=" + safeUiId(uiId)
                    + ", file=" + (file == null ? "null" : file.getPath());
            return false;
        }
        for (String candidateUiId : candidateUiIds(uiId, file)) {
            if (isRegisteredRuntimeId(candidateUiId)) {
                invokeAny(uiRegistry, new String[]{"unregister", "unRegister"}, candidateUiId);
            }
            if (invokeAny(uiRegistry, new String[]{"register"}, candidateUiId, file)
                    && isRegisteredRuntimeId(candidateUiId)) {
                registeredUiIds.put(uiId.trim(), candidateUiId);
                unavailableReason = "";
                return true;
            }
        }
        unavailableReason = "ArcartX UI register 失败: uiId=" + safeUiId(uiId)
                + ", candidates=" + candidateUiIds(uiId, file);
        return false;
    }

    public boolean reload(String uiId, File file) {
        if (!isAvailable() || isBlank(uiId) || file == null || !file.isFile()) {
            unavailableReason = "ArcartX UI reload 失败: uiId=" + safeUiId(uiId)
                    + ", file=" + (file == null ? "null" : file.getPath());
            return false;
        }
        String runtimeUiId = resolveRuntimeUiId(uiId);
        if (isRegisteredRuntimeId(runtimeUiId)
                && invokeAny(uiRegistry, new String[]{"reload"}, runtimeUiId, file)
                && isRegisteredRuntimeId(runtimeUiId)) {
            registeredUiIds.put(uiId.trim(), runtimeUiId);
            unavailableReason = "";
            return true;
        }
        return register(uiId, file);
    }

    public boolean unregister(String uiId) {
        if (!isAvailable() || isBlank(uiId)) {
            return false;
        }
        removePacketCallback(uiId.trim());
        String runtimeUiId = resolveRuntimeUiId(uiId);
        boolean removed = false;
        for (String candidate : unregisterCandidateUiIds(uiId, runtimeUiId)) {
            if (invokeAny(uiRegistry, new String[]{"unregister", "unRegister"}, candidate)) {
                removed = true;
            }
        }
        registeredUiIds.remove(uiId.trim());
        return removed;
    }

    public boolean isRegistered(String uiId) {
        if (!isAvailable() || isBlank(uiId)) {
            return false;
        }
        return isRegisteredRuntimeId(resolveRuntimeUiId(uiId)) || isRegisteredRuntimeId(uiId.trim());
    }

    public boolean open(Player player, String uiId) {
        if (!isAvailable() || player == null || isBlank(uiId) || !requireRegistered(uiId)) {
            return false;
        }
        boolean opened = invokeAny(uiRegistry, new String[]{"openUnsafe", "open"}, player, resolveRuntimeUiId(uiId));
        if (opened) {
            unavailableReason = "";
        }
        return opened;
    }

    public boolean sendPacket(Player player, String uiId, String handlerName, Object packet) {
        if (!isAvailable() || player == null || isBlank(uiId) || handlerName == null || !requireRegistered(uiId)) {
            return false;
        }
        String runtimeUiId = resolveRuntimeUiId(uiId);
        if (invokeAny(uiRegistry, new String[]{"sendPacketUnsafe"}, player, runtimeUiId, handlerName, packet)
                || invokeAny(uiRegistry, new String[]{"sendPacket"}, player, runtimeUiId, handlerName, packet)) {
            unavailableReason = "";
            return true;
        }
        unavailableReason = "调用 ArcartX sendPacket 失败: uiId=" + safeUiId(uiId)
                + ", handler=" + handlerName;
        return false;
    }

    public boolean registerPacketCallback(String uiId, PacketCallback callback) {
        if (!isAvailable() || isBlank(uiId) || callback == null || !requireRegistered(uiId)) {
            return false;
        }
        String configuredUiId = uiId.trim();
        String runtimeUiId = resolveRuntimeUiId(configuredUiId);
        Object ui = getRegisteredUi(runtimeUiId);
        if (ui == null) {
            unavailableReason = "ArcartX UI 未注册: uiId=" + safeUiId(uiId);
            return false;
        }
        removePacketCallback(configuredUiId);
        try {
            Object packetType = enumConstant(CALLBACK_TYPE_CLASS, "PACKET");
            Class<?> uiCallbackType = loadArcartXClass(UI_CALLBACK_CLASS);
            Object proxy = Proxy.newProxyInstance(
                    uiCallbackType.getClassLoader(),
                    new Class<?>[]{uiCallbackType},
                    new PacketInvocationHandler(configuredUiId, runtimeUiId, callback)
            );
            Method registerMethod = findMethod(ui.getClass(), "registerCallBack", new Object[]{packetType, proxy});
            if (registerMethod == null) {
                unavailableReason = "ArcartX UI 不支持 registerCallBack(PACKET): uiId=" + safeUiId(uiId);
                return false;
            }
            registerMethod.setAccessible(true);
            registerMethod.invoke(ui, packetType, proxy);
            packetCallbacks.put(configuredUiId, new CallbackRegistration(runtimeUiId, proxy));
            unavailableReason = "";
            return true;
        } catch (Throwable throwable) {
            unavailableReason = "注册 ArcartX UI packet callback 失败: uiId=" + safeUiId(uiId)
                    + ", reason=" + safeMessage(throwable);
            return false;
        }
    }

    private boolean removePacketCallback(String uiId) {
        CallbackRegistration previous = packetCallbacks.remove(uiId);
        if (previous == null || uiRegistry == null) {
            return false;
        }
        Object ui = getRegisteredUi(previous.runtimeUiId);
        if (ui == null) {
            return false;
        }
        try {
            Object packetType = enumConstant(CALLBACK_TYPE_CLASS, "PACKET");
            Object callbacks = invokeValue(ui, "getCallbacks");
            if (!(callbacks instanceof Map<?, ?>)) {
                return false;
            }
            Object callbackList = ((Map<?, ?>) callbacks).get(packetType);
            if (callbackList instanceof List<?>) {
                ((List<?>) callbackList).remove(previous.proxy);
                return true;
            }
        } catch (Throwable throwable) {
            unavailableReason = "移除 ArcartX packet callback 失败: uiId=" + safeUiId(uiId)
                    + ", reason=" + safeMessage(throwable);
        }
        return false;
    }

    private Object getRegisteredUi(String uiId) {
        Method getMethod = findMethod(uiRegistry.getClass(), "get", new Object[]{uiId});
        if (getMethod != null) {
            try {
                getMethod.setAccessible(true);
                return getMethod.invoke(uiRegistry, uiId);
            } catch (Throwable throwable) {
                unavailableReason = "检查 ArcartX UI 注册状态失败: uiId=" + safeUiId(uiId)
                        + ", reason=" + safeMessage(throwable);
            }
        }
        Method getRegisteredMethod = findMethod(uiRegistry.getClass(), "getRegisteredUI", new Object[0]);
        if (getRegisteredMethod == null) {
            return null;
        }
        try {
            getRegisteredMethod.setAccessible(true);
            Object result = getRegisteredMethod.invoke(uiRegistry);
            return result instanceof Map<?, ?> ? ((Map<?, ?>) result).get(uiId) : null;
        } catch (Throwable throwable) {
            unavailableReason = "读取 ArcartX UI 注册表失败: " + safeMessage(throwable);
            return null;
        }
    }

    private boolean requireRegistered(String uiId) {
        if (isRegistered(uiId)) {
            return true;
        }
        unavailableReason = "ArcartX UI 未注册: uiId=" + safeUiId(uiId);
        return false;
    }

    private boolean invokeAny(Object target, String[] names, Object... args) {
        if (target == null || names == null) {
            return false;
        }
        String lastFailure = "";
        for (String name : names) {
            Method method = findMethod(target.getClass(), name, args);
            if (method == null) {
                lastFailure = "未找到兼容的 ArcartX 方法: " + name;
                continue;
            }
            try {
                method.setAccessible(true);
                Object result = method.invoke(target, args);
                if (result instanceof Boolean && !((Boolean) result).booleanValue()) {
                    lastFailure = "ArcartX " + name + " 返回 false。";
                    continue;
                }
                return true;
            } catch (Throwable throwable) {
                lastFailure = "调用 ArcartX " + name + " 失败: " + safeMessage(throwable);
            }
        }
        unavailableReason = lastFailure;
        return false;
    }

    private Object invokeValue(Object target, String name) throws Exception {
        Method method = findMethod(target.getClass(), name, new Object[0]);
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        return method.invoke(target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object enumConstant(String className, String name) throws ClassNotFoundException {
        Class<?> type = loadArcartXClass(className);
        return Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), name);
    }

    private Method findMethod(Class<?> type, String name, Object[] args) {
        Method[] publicMethods = type.getMethods();
        Method best = null;
        int bestScore = -1;
        for (Method method : publicMethods) {
            int score = methodScore(method, name, args);
            if (score > bestScore) {
                best = method;
                bestScore = score;
            }
        }
        Method[] declaredMethods = type.getDeclaredMethods();
        for (Method method : declaredMethods) {
            int score = methodScore(method, name, args);
            if (score > bestScore) {
                best = method;
                bestScore = score;
            }
        }
        return bestScore < 0 ? null : best;
    }

    private int methodScore(Method method, String name, Object[] args) {
        if (!method.getName().equals(name) || method.getParameterTypes().length != args.length) {
            return -1;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        int total = 0;
        for (int index = 0; index < parameterTypes.length; index++) {
            Object arg = args[index];
            if (arg == null) {
                if (parameterTypes[index].isPrimitive()) {
                    return -1;
                }
                total++;
                continue;
            }
            Class<?> parameterType = wrap(parameterTypes[index]);
            Class<?> argumentType = wrap(arg.getClass());
            if (parameterType.equals(argumentType)) {
                total += 8;
                continue;
            }
            if (!parameterType.isAssignableFrom(argumentType)) {
                return -1;
            }
            total += 4;
        }
        return total;
    }

    private Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        return type;
    }

    private Plugin findArcartXPlugin() {
        try {
            for (String pluginName : PLUGIN_NAMES) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin != null && plugin.isEnabled()) {
                    return plugin;
                }
            }
        } catch (Throwable ignored) {
            return null;
        }
        return null;
    }

    private boolean isApiClassVisible() {
        try {
            Class.forName(API_CLASS);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Class<?> loadArcartXClass(String className) throws ClassNotFoundException {
        Plugin arcartXPlugin = findArcartXPlugin();
        return arcartXPlugin == null
                ? Class.forName(className)
                : Class.forName(className, true, arcartXPlugin.getClass().getClassLoader());
    }

    private boolean isRegisteredRuntimeId(String uiId) {
        return !isBlank(uiId) && getRegisteredUi(uiId.trim()) != null;
    }

    private String resolveRuntimeUiId(String uiId) {
        if (isBlank(uiId)) {
            return uiId;
        }
        String trimmed = uiId.trim();
        String mapped = registeredUiIds.get(trimmed);
        if (!isBlank(mapped) && isRegisteredRuntimeId(mapped)) {
            return mapped;
        }
        return trimmed;
    }

    private Set<String> candidateUiIds(String configuredUiId, File file) {
        LinkedHashSet<String> candidates = new LinkedHashSet<String>();
        if (!isBlank(configuredUiId)) {
            String normalized = configuredUiId.trim();
            candidates.add(normalized);
            int namespaceSeparator = normalized.lastIndexOf(':');
            if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
                candidates.add(normalized.substring(namespaceSeparator + 1));
            }
        }
        String fileBaseName = fileBaseName(file);
        if (!isBlank(fileBaseName)) {
            candidates.add(fileBaseName);
        }
        return candidates;
    }

    private Set<String> unregisterCandidateUiIds(String requestedUiId, String runtimeUiId) {
        LinkedHashSet<String> candidates = new LinkedHashSet<String>();
        if (!isBlank(runtimeUiId)) {
            candidates.add(runtimeUiId.trim());
        }
        if (!isBlank(requestedUiId)) {
            candidates.add(requestedUiId.trim());
        }
        return candidates;
    }

    private String fileBaseName(File file) {
        if (file == null) {
            return null;
        }
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeUiId(String uiId) {
        return isBlank(uiId) ? "<blank>" : uiId.trim();
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        Throwable current = throwable.getCause() == null ? throwable : throwable.getCause();
        String message = current.getMessage();
        return message == null || message.trim().isEmpty() ? current.getClass().getSimpleName() : message.trim();
    }

    private static final class CallbackRegistration {
        private final String runtimeUiId;
        private final Object proxy;

        private CallbackRegistration(String runtimeUiId, Object proxy) {
            this.runtimeUiId = runtimeUiId;
            this.proxy = proxy;
        }
    }

    private final class PacketInvocationHandler implements InvocationHandler {
        private final String configuredUiId;
        private final String runtimeUiId;
        private final PacketCallback callback;

        private PacketInvocationHandler(String configuredUiId, String runtimeUiId, PacketCallback callback) {
            this.configuredUiId = configuredUiId;
            this.runtimeUiId = runtimeUiId;
            this.callback = callback;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method != null && method.getDeclaringClass() == Object.class) {
                if ("equals".equals(method.getName())) {
                    return Boolean.valueOf(proxy == (args == null || args.length == 0 ? null : args[0]));
                }
                if ("hashCode".equals(method.getName())) {
                    return Integer.valueOf(System.identityHashCode(proxy));
                }
                if ("toString".equals(method.getName())) {
                    return "LmMessageArcartXPacketCallback[" + configuredUiId + "->" + runtimeUiId + "]";
                }
            }
            if (method == null || !"call".equals(method.getName())) {
                return null;
            }
            try {
                Object callData = args == null || args.length == 0 ? null : args[0];
                Player player = readPlayer(callData);
                String identifier = readString(callData, "identifier", "getIdentifier", "component2");
                Object dataValue = readValue(callData, "data", "getData", "component3");
                callback.onPacket(player, configuredUiId, identifier, toStringList(dataValue));
            } catch (Throwable throwable) {
                unavailableReason = "处理 ArcartX packet callback 失败: uiId=" + safeUiId(configuredUiId)
                        + ", reason=" + safeMessage(throwable);
            }
            return null;
        }

        private Player readPlayer(Object callData) {
            Object value = readValue(callData, "player", "getPlayer", "component1");
            return value instanceof Player ? (Player) value : null;
        }

        private String readString(Object target, String... names) {
            Object value = readValue(target, names);
            return value == null ? null : String.valueOf(value);
        }

        private Object readValue(Object target, String... names) {
            if (target == null || names == null) {
                return null;
            }
            for (String name : names) {
                try {
                    Method method = target.getClass().getMethod(name);
                    method.setAccessible(true);
                    Object value = method.invoke(target);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        private List<String> toStringList(Object value) {
            List<String> result = new ArrayList<String>();
            if (value instanceof Iterable<?>) {
                for (Object item : (Iterable<?>) value) {
                    result.add(item == null ? "" : String.valueOf(item));
                }
                return result;
            }
            if (value != null) {
                result.add(String.valueOf(value));
            }
            return result;
        }
    }
}
