package org.imanity.framework.bukkit.test.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.ButtonBuilder;
import org.imanity.framework.bukkit.menu.Menu;
import org.imanity.framework.bukkit.menu.buttons.DisplayButton;
import org.imanity.framework.bukkit.test.TestInfo;
import org.imanity.framework.bukkit.test.TestList;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.util.CC;

@RequiredArgsConstructor
public class TestRunConfirmMenu extends Menu {

    private final TestList testList;
    private final TestInfo testInfo;

    @Override
    public String getTitle() {
        if (testInfo.isCalled()) {
            return "&eRun Test - " + this.testInfo.getName();
        } else {
            return "&6Re-Run Test - " + this.testInfo.getName();
        }
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public void draw(boolean firstInitial) {
        if (!firstInitial) {
            return;
        }

        this.set(8, ButtonBuilder.builder()
                .item(new ItemBuilder(Material.REDSTONE_BLOCK).name("&cCancel"))
                .callback((player, slot, clickType, hotbarButton) -> new TestListMenu(testList).open(player))
                .build());

        this.set(13, new DisplayButton(new ItemBuilder(this.testInfo.getType())
                .name(CC.YELLOW + this.testInfo.getName()).build(), true));

        if (this.testInfo.hasCondition()) {
            this.set(38, new RunNormallyButton());
            this.set(42, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemBuilder(Material.WOOL)
                            .data(4)
                            .name("&aRun But Ignoring Condition")
                            .build();
                }

                @Override
                public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                    player.closeInventory();
                    testInfo.run(player, false, false);
                }
            });
        } else {
            this.set(40, new RunNormallyButton());
        }
    }

    private class RunNormallyButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WOOL)
                    .data(5)
                    .name("&aRun Normally")
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.closeInventory();
            testInfo.run(player, false, true);
        }
    }
}
