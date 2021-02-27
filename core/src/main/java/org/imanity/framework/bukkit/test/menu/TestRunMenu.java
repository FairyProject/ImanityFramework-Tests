package org.imanity.framework.bukkit.test.menu;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.Autowired;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.pagination.PaginatedMenu;
import org.imanity.framework.bukkit.test.TestInfo;
import org.imanity.framework.bukkit.test.TestService;
import org.imanity.framework.bukkit.test.TestStatus;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.util.CC;
import org.imanity.framework.util.Stacktrace;

import java.util.Map;

@RequiredArgsConstructor
public class TestRunMenu extends PaginatedMenu {

    @Autowired
    private static TestService TEST_SERVICE;

    private final AbstractPlugin plugin;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&eTests Run";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final ImmutableMap.Builder<Integer, Button> map = this.newMap();

        int slot = 0;
        for (TestInfo testInfo : TEST_SERVICE.getTestsBy(this.plugin)) {
            map.put(slot++, new TestButton(testInfo));
        }

        return map.build();
    }

    @RequiredArgsConstructor
    private class TestButton extends Button {

        private final TestInfo testInfo;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder itemBuilder = new ItemBuilder(this.testInfo.getType());

            itemBuilder.lore(CC.SB_BAR);
            itemBuilder.lore("&7Test: &e" + this.testInfo.getName());
            itemBuilder.lore("&7Condition: &f" + this.testInfo.getCondition());
            itemBuilder.lore("&7Status: " + this.testInfo.getStatus().getDisplayName());
            String isErrorExpected = "";
            if (this.testInfo.getExpected() != null && this.testInfo.getStatus() == TestStatus.PASSED) {
                isErrorExpected = "&8(Expected)";
            }
            if (this.testInfo.getThrowable() != null) {
                itemBuilder.lore("&7Exception" + isErrorExpected + "&7: &f" + this.testInfo.getThrowable().getClass().getSimpleName());
            }

            itemBuilder.lore(" ");

            if (this.testInfo.isCalled()) {
                itemBuilder.lore("&eClick me to re-run test!");
            } else {
                itemBuilder.lore("&eClick me to run the test!");
            }

            itemBuilder.lore(CC.SB_BAR);

            return itemBuilder.build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new TestRunConfirmMenu(plugin, this.testInfo).openMenu(player);
        }
    }
}
