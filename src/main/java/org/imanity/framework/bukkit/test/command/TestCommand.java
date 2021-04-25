package org.imanity.framework.bukkit.test.command;

import org.imanity.framework.Component;
import org.imanity.framework.DependencyType;
import org.imanity.framework.ServiceDependency;
import org.imanity.framework.ServiceDependencyType;
import org.imanity.framework.bukkit.command.event.BukkitCommandEvent;
import org.imanity.framework.bukkit.test.TestInfo;
import org.imanity.framework.bukkit.test.menu.TestPluginMenu;
import org.imanity.framework.command.annotation.Command;
import org.imanity.framework.command.annotation.CommandHolder;
import org.imanity.framework.command.annotation.Parameter;

@Component
@ServiceDependency(dependencies = "test", type = @DependencyType(ServiceDependencyType.SUB_DISABLE))
public class TestCommand implements CommandHolder {

    @Command(names = "tests", permissionNode = "framework.admin")
    public void tests(BukkitCommandEvent event) {
        if (event.isPlayer()) {
            new TestPluginMenu().open(event.getPlayer());
        }
    }

    @Command(names = "tests run", permissionNode = "framework.admin")
    public void runTests(BukkitCommandEvent event, @Parameter(name = "Test Name") TestInfo test, @Parameter(name = "mustCheckCondition", defaultValue = "true") boolean mustCheckCondition) {
        test.run(event.getSender(), false, mustCheckCondition);
    }

}
