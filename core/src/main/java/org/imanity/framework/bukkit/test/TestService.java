package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.imanity.framework.*;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.cache.script.AbstractScriptParser;
import org.imanity.framework.cache.script.SpringELParser;
import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.plugin.PluginListenerAdapter;
import org.imanity.framework.plugin.PluginManager;
import org.imanity.framework.util.Stacktrace;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service(name = "test")
@Getter
public class TestService {

    public static TestService INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger(TestService.class);
    private BotFactory botFactory;

    @Autowired
    private BeanContext beanContext;

    private AbstractScriptParser scriptParser;
    private Map<UUID, Bot> bots;
    private Map<String, TestInfo> nameToTests;
    private Map<AbstractPlugin, List<TestInfo>> pluginToTests;

    @ShouldInitialize
    public boolean shouldInit() {
        final boolean shouldInit = System.getProperty("testFramework", "false").equalsIgnoreCase("true");
        if (shouldInit) {
            LOGGER.info("The Test Mode has been enabled");
        }
        return shouldInit;
    }

    @PreInitialize
    public void preInit() {
        INSTANCE = this;
        this.bots = new ConcurrentHashMap<>();
        this.nameToTests = new ConcurrentHashMap<>();
        this.pluginToTests = new ConcurrentHashMap<>();

        LOGGER.info("TestService pre initialize called.");

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { BotFactory.class };
            }

            @Override
            public void onEnable(Object instance) {
                BotFactory botFactory = (BotFactory) instance;
                if (!botFactory.canActive()) {
                    return;
                }

                TestService.this.botFactory = botFactory;
            }
        });

        PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
            @Override
            public void onPluginEnable(AbstractPlugin plugin) {
                pluginToTests.put(plugin, new ArrayList<>());
                TaskUtil.runScheduled(() -> {
                    final Collection<BeanDetails> detailsBindWith = beanContext.findDetailsBindWith(plugin);
                    detailsBindWith.forEach(beanDetails -> loadInstance(plugin, beanDetails.getInstance()));
                }, 1L);
            }

            @Override
            public int priority() {
                return 50;
            }
        });

        Events.subscribe(PlayerJoinEvent.class)
                .listen((playerJoinEventEventSubscription, event) -> {
                    final Player player = event.getPlayer();
                    if (this.isBot(player.getUniqueId())) {
                        return;
                    }

                    for (TestInfo testInfo : this.getPluginToTests()) {
                        testInfo.run(null, true, true);
                    }
                })
                .build(Imanity.PLUGIN);
    }

    public void loadInstance(AbstractPlugin plugin, Object instance) {
        if (instance == null) {
            return;
        }

        List<TestInfo> tests = this.pluginToTests.get(plugin);

        for (Method method : instance.getClass().getDeclaredMethods()) {
            try {
                Test test = method.getAnnotation(Test.class);

                if (test == null) {
                    continue;
                }

                boolean hasParameter = false;

                if (method.getParameterCount() > 0) {
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == BotFactory.class) {
                        hasParameter = true;
                    } else {
                        Stacktrace.print(new IllegalArgumentException("The Test with name " + test.value() + " has invalid parameter(s)!"));
                        continue;
                    }
                }

                final String name = test.value();
                if (this.nameToTests.containsKey(name)) {
                    Stacktrace.print(new IllegalArgumentException("The Test with name " + name + " already exists!"));
                    continue;
                }

                Class<? extends Throwable> expectedClass = null;
                Expected expected = method.getAnnotation(Expected.class);
                if (expected != null) {
                    expectedClass = expected.value();
                }

                TestInfo testInfo = new TestInfo(name, test.display(), method, instance, test.condition(), expectedClass, hasParameter);
                tests.add(testInfo);
                this.nameToTests.put(name, testInfo);

                LOGGER.info("Found Test " + test.value() + " from Plugin " + plugin.getName());
            } catch (Throwable throwable) {
                Stacktrace.print(throwable);
            }
        }

        for (TestInfo testInfo : this.getTestsBy(plugin)) {
            testInfo.run(null, true, true);
        }
    }

    @PostInitialize
    public void postInit() {
        if (this.botFactory == null) {
            LOGGER.warn("The Framework didn't detect a valid bot factory for your current server software! Some feature may not be working!");
        }

        this.scriptParser = new SpringELParser();

        try {
            Method method = this.getClass().getMethod("onlineCount");
            this.scriptParser.addFunction("onlineCount", method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static int onlineCount() {
        return Imanity.getPlayers().size();
    }

    public Collection<TestInfo> getTestsBy(AbstractPlugin plugin) {
        final List<TestInfo> list = this.pluginToTests.get(plugin);
        return list == null ? Collections.emptyList() : list;
    }

    public Collection<TestInfo> getPluginToTests() {
        return this.nameToTests.values();
    }

    public void addBot(Bot bot) {
        this.bots.put(bot.getUniqueID(), bot);
    }

    public void removeBot(UUID uuid) {
        this.bots.remove(uuid);
    }

    public boolean isBot(UUID uuid) {
        return this.bots.containsKey(uuid);
    }

    @Nullable
    public TestInfo getTestByName(String name) {
        return this.nameToTests.get(name);
    }

    @Nullable
    public Bot getBot(UUID uuid) {
        return this.bots.get(uuid);
    }

    public UUID getRandomUuid() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (this.isBot(uuid) || Bukkit.getPlayer(uuid) != null);

        return uuid;
    }

}
