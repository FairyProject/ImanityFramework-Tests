package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.bukkit.Material;
import org.imanity.framework.Autowired;
import org.imanity.framework.BeanContext;
import org.imanity.framework.bukkit.test.condition.TestConditionSpEL;
import org.imanity.framework.details.constructor.BeanParameterDetailsMethod;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.util.Stacktrace;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPluginList {

    @Autowired
    private static BeanContext BEAN_CONTEXT;

    private final AbstractPlugin plugin;
    @Getter
    private final TestList defaultList;
    private final Map<String, TestList> customListMap;

    public TestPluginList(AbstractPlugin plugin) {
        this.plugin = plugin;
        this.defaultList = new TestList("Default List", new TestGroup.TestGroupConfiguration());
        this.customListMap = new ConcurrentHashMap<>();
    }

    public List<TestInfo> getTests() {
        final Collection<TestList> list = this.customListMap.values();
        return Stream.of(list, Collections.singleton(this.defaultList))
                .flatMap(Collection::stream)
                .flatMap(testList -> testList.getList().stream())
                .collect(Collectors.toList());
    }

    public TestList getListByName(String name) {
        return this.customListMap.get(name);
    }

    public void checkInstance(Object instance) {
        TestList testList;

        if (instance instanceof TestGroup) {
            TestGroup group = (TestGroup) instance;

            String name = group.name();
            if (this.customListMap.containsKey(name)) {
                throw new IllegalArgumentException("The TestGroup with name " + name + " already exists!");
            }

            testList = new TestList(name, group.config());
            this.customListMap.put(name, testList);
        } else {
            testList = this.defaultList;
        }

        for (Method method : instance.getClass().getDeclaredMethods()) {
            try {
                Test test = method.getAnnotation(Test.class);

                if (test == null) {
                    continue;
                }

                final String name = test.value();
                if (!TestService.INSTANCE.canAdd(name)) {
                    continue;
                }

                Class<? extends Throwable> expectedClass = null;
                Expected expected = method.getAnnotation(Expected.class);
                if (expected != null) {
                    expectedClass = expected.value();
                }

                BeanParameterDetailsMethod parameterDetailsMethod = new BeanParameterDetailsMethod(method, BEAN_CONTEXT);

                final int order = test.order();
                final boolean requireExecutor = test.requireExecutor();
                final TestConditionSpEL condition = new TestConditionSpEL(test.condition());
                final Material displayMaterial = test.display();

                TestInfo testInfo = new TestInfo(name,
                        displayMaterial,
                        parameterDetailsMethod,
                        instance,
                        condition,
                        requireExecutor,
                        expectedClass,
                        order,
                        testList
                );
                testList.addTest(testInfo);
                TestService.INSTANCE.addTest(testInfo);

                TestService.LOGGER.info("Found Test " + test.value() + " from Plugin " + plugin.getName());
            } catch (Throwable throwable) {
                Stacktrace.print(throwable);
            }
        }
    }

    public Collection<TestList> getLists() {
        return Collections.unmodifiableCollection(this.customListMap.values());
    }
}
