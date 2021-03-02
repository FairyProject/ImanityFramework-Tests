package org.imanity.framework.bukkit.test.menu;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.pagination.PaginatedMenu;
import org.imanity.framework.bukkit.test.TestList;
import org.imanity.framework.bukkit.test.TestPluginList;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.util.CC;

import java.util.Map;

@RequiredArgsConstructor
public class TestGroupMenu extends PaginatedMenu {

    private final TestPluginList pluginList;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&eRun Tests - Find Groups";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final ImmutableMap.Builder<Integer, Button> map = this.newMap();

        int slot = 0;
        for (TestList list : this.pluginList.getLists()) {
            map.put(slot++, new ListButton(list));
        }

        return map.build();
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        final ImmutableMap.Builder<Integer, Button> map = this.newMap();

        map.put(1, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.BOOK_AND_QUILL)
                        .name("&eUngrouped Tests")
                        .build();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                new TestListMenu(pluginList.getDefaultList()).openMenu(player);
            }
        });

        return map.build();
    }

    @RequiredArgsConstructor
    private class ListButton extends Button {

        private final TestList testList;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ItemBuilder itemBuilder = new ItemBuilder(this.testList.getConfig().getDisplayMaterial())
                    .name(CC.YELLOW + this.testList.getName())
                    .lore(CC.SB_BAR, "&7Tests: &f" + this.testList.getList().size());
            if (this.testList.getConfig().isRunInGroup()) {
                itemBuilder.lore("&7Type: &fRun In Group");
            } else {
                itemBuilder.lore("&7Type: &fNormal");
            }
            itemBuilder.lore("&7Status: " + this.testList.getStatus().getDisplayName(), CC.SB_BAR);
            return itemBuilder.build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new TestListMenu(this.testList).openMenu(player);
        }
    }

}
