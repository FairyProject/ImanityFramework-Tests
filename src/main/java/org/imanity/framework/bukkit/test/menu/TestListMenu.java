package org.imanity.framework.bukkit.test.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.ButtonBuilder;
import org.imanity.framework.bukkit.menu.pagination.PaginatedListMenu;
import org.imanity.framework.bukkit.test.TestInfo;
import org.imanity.framework.bukkit.test.TestList;
import org.imanity.framework.bukkit.test.TestStatus;
import org.imanity.framework.bukkit.util.Chat;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.util.CC;

import java.util.List;

@RequiredArgsConstructor
public class TestListMenu extends PaginatedListMenu {

    private final TestList testList;

    @Override
    public String getPrePaginatedTitle() {
        return "&eTests Run";
    }

    @Override
    public List<Button> getButtons() {
        return this.transformToButtons(this.testList.getList(), TestButton::new);
    }

    @Override
    protected void drawGlobal(boolean firstInitial) {
        super.drawGlobal(firstInitial);
        if (firstInitial) {
            this.set(1, ButtonBuilder.builder()
                    .item(new ItemBuilder(Material.EMERALD).name("&aRun Entire Group"))
                    .callback((player1, slot, clickType, hotbarButton) -> testList.startRunning(player))
                    .cancel().build());
        }
    }

    @RequiredArgsConstructor
    private class TestButton extends Button {

        private final TestInfo testInfo;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder itemBuilder = new ItemBuilder(this.testInfo.getType());

            itemBuilder.lore(CC.SB_BAR);
            itemBuilder.lore("&7Test: &e" + this.testInfo.getName());
            itemBuilder.lore("&7Condition: &f" + (this.testInfo.getCondition() != null ? this.testInfo.getCondition().toInfo() : "None"));
            itemBuilder.lore("&7Status: " + this.testInfo.getStatus().getDisplayName());
            String isErrorExpected = "";
            if (this.testInfo.getExpected() != null && this.testInfo.getStatus() == TestStatus.PASSED) {
                isErrorExpected = "&8(Expected)";
            }
            if (this.testInfo.getThrowable() != null) {
                itemBuilder.lore("&7Exception" + isErrorExpected + "&7: &f" + this.testInfo.getThrowable().getClass().getSimpleName());
            }

            itemBuilder.lore(" ");

            if (testList.getConfig().isRunInGroup()) {
                itemBuilder.lore("&cThe Test Group enabled Run-In-Group, You must run test group instead!");
            } else {
                if (this.testInfo.isCalled()) {
                    itemBuilder.lore("&eClick me to re-run test!");
                } else {
                    itemBuilder.lore("&eClick me to run the test!");
                }
            }

            itemBuilder.lore(CC.SB_BAR);

            return itemBuilder.build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (testList.getConfig().isRunInGroup()) {
                Chat.sendRaw(player, "&cThe Test Group enabled Run-In-Group, You must run test group instead!");
                return;
            }
            new TestRunConfirmMenu(testList, this.testInfo).open(player);
        }
    }
}
