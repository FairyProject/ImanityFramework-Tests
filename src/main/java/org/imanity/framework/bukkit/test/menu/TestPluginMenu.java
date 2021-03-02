package org.imanity.framework.bukkit.test.menu;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.Autowired;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.pagination.PaginatedMenu;
import org.imanity.framework.bukkit.test.TestService;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.plugin.PluginManager;
import org.imanity.framework.util.CC;

import java.util.Map;

public class TestPluginMenu extends PaginatedMenu {

    @Autowired
    private static TestService TEST_SERVICE;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&eRun Tests - Find Plugins";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final ImmutableMap.Builder<Integer, Button> map = this.newMap();

        int slot = 0;
        for (AbstractPlugin plugin : PluginManager.INSTANCE.getPlugins()) {
            map.put(slot++, new PluginButton(plugin));
        }

        return map.build();
    }

    @RequiredArgsConstructor
    private static class PluginButton extends Button {

        private final AbstractPlugin plugin;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.SIGN)
                    .name(CC.YELLOW + plugin.getName())
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new TestGroupMenu(TEST_SERVICE.getTestPlugnList(plugin)).openMenu(player);
        }
    }
}
