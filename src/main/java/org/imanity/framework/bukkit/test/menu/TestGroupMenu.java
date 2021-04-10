package org.imanity.framework.bukkit.test.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.ButtonBuilder;
import org.imanity.framework.bukkit.menu.pagination.PaginatedListMenu;
import org.imanity.framework.bukkit.test.TestList;
import org.imanity.framework.bukkit.test.TestPluginList;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.util.CC;

import java.util.List;

@RequiredArgsConstructor
public class TestGroupMenu extends PaginatedListMenu {

    private final TestPluginList pluginList;

    @Override
    public String getPrePaginatedTitle() {
        return "&eRun Tests - Find Groups";
    }

    @Override
    public List<Button> getButtons() {
        return this.transformToButtons(this.pluginList.getLists(), ListButton::new);
    }

    @Override
    protected void drawGlobal(boolean firstInitial) {
        super.drawGlobal(firstInitial);
        if (!firstInitial) {
            return;
        }

        this.set(1, ButtonBuilder.builder()
                .item(new ItemBuilder(Material.BOOK_AND_QUILL).name("&eTests with No Group"))
                .callback((ignored, slot, clickType, hotbarButton) -> new TestListMenu(pluginList.getDefaultList()).open(player))
                .cancel().build()
        );
    }

    @RequiredArgsConstructor
    private static class ListButton extends Button {

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
            new TestListMenu(this.testList).open(player);
        }
    }

}
