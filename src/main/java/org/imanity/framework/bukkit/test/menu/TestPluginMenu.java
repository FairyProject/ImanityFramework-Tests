package org.imanity.framework.bukkit.test.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.pagination.PaginatedListMenu;
import org.imanity.framework.bukkit.test.TestService;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.plugin.PluginManager;
import org.imanity.framework.util.CC;

import java.util.List;

public class TestPluginMenu extends PaginatedListMenu {

    @Override
    public String getPrePaginatedTitle() {
        return "&eRun Tests - Find Plugins";
    }

    @Override
    public List<Button> getButtons() {
        return this.transformToButtons(PluginManager.INSTANCE.getPlugins(), PluginButton::new);
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
            new TestGroupMenu(TestService.INSTANCE.getTestPlugnList(plugin)).open(player);
        }
    }
}
