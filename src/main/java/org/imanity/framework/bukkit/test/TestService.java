package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.imanity.framework.*;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.util.BukkitUtil;
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

@Service(name = "test")
@Getter
public class TestService {

    public static TestService INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(TestService.class);

    @Autowired
    private BeanContext beanContext;

    private AbstractScriptParser scriptParser;
    private Map<String, TestInfo> testByName;
    private Map<AbstractPlugin, TestPluginList> testByPlugin;

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
        this.testByName = new ConcurrentHashMap<>();
        this.testByPlugin = new ConcurrentHashMap<>();

        LOGGER.info("TestService pre initialize called.");

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {TestGroup.class};
            }
        });

        PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
            @Override
            public void onPluginEnable(AbstractPlugin plugin) {
                TaskUtil.runScheduled(() -> {
                    final TestPluginList pluginList = new TestPluginList(plugin);
                    testByPlugin.put(plugin, pluginList);
                    final Collection<BeanDetails> detailsBindWith = beanContext.findDetailsBindWith(plugin);
                    detailsBindWith.forEach(beanDetails -> pluginList.checkInstance(beanDetails.getInstance()));
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
                    if (BukkitUtil.isNPC(player)) {
                        return;
                    }

                    for (TestInfo testInfo : this.getTestByPlugin()) {
                        testInfo.run(null, true, true);
                    }
                })
                .build(Imanity.PLUGIN);
    }

    public void updateTests() {
        for (TestInfo testInfo : this.getTestByPlugin()) {
            testInfo.run(null, true, true);
        }
    }

    @PostInitialize
    public void postInit() {
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
        final List<TestInfo> list = this.getTestPlugnList(plugin).getTests();
        return list == null ? Collections.emptyList() : list;
    }

    public Collection<TestInfo> getTestByPlugin() {
        return this.testByName.values();
    }

    protected boolean canAdd(String name) {
        if (this.testByName.containsKey(name)) {
            Stacktrace.print(new IllegalArgumentException("The Test with name " + name + " already exists!"));
            return false;
        }

        return true;
    }

    public void addTest(TestInfo testInfo) {
        this.testByName.put(testInfo.getName(), testInfo);
    }

    @Nullable
    public TestInfo getTestByName(String name) {
        return this.testByName.get(name);
    }

    public TestPluginList getTestPlugnList(AbstractPlugin plugin) {
        return this.testByPlugin.get(plugin);
    }
}
