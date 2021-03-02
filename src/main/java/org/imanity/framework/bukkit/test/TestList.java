package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.imanity.framework.util.CC;

import java.util.*;

@Getter
public class TestList {

    private final String name;
    private final List<TestInfo> list;
    private final TestGroup.TestGroupConfiguration config;

    private TestGroupStatus status;
    private TestRunInfo runInfo;

    public TestList(String name, TestGroup.TestGroupConfiguration config) {
        this.name = name;
        this.list = new ArrayList<>();
        this.config = config;
        this.status = TestGroupStatus.NOT_RUNNING;
    }

    public List<TestInfo> getList() {
        return Collections.unmodifiableList(this.list);
    }

    public void startRunning(CommandSender sender) {
        this.runInfo = new TestRunInfo(this, sender);
        this.reset();

        sender.sendMessage(CC.CHAT_BAR);
        sender.sendMessage(CC.YELLOW + "Start Running Group " + CC.WHITE + this.name);
        this.runInfo.current().run(sender, false, true);
    }

    // reset all the tests
    public void reset() {
        this.status = TestGroupStatus.NOT_RUNNING;
        for (TestInfo testInfo : this.list) {
            testInfo.reset();
        }
    }

    public void addTest(TestInfo testInfo) {
        this.list.add(testInfo);
        this.list.sort(Comparator.naturalOrder());
    }

    public void removeTest(TestInfo testInfo) {
        this.list.remove(testInfo);
    }

    public boolean canRun(TestInfo testInfo) {
        if (this.runInfo != null) {
            return this.runInfo.current() == testInfo;
        }

        return !this.config.isRunInGroup();
    }

    public void next() {
        if (this.runInfo != null) {
            final TestInfo next = this.runInfo.next();
            if (next == null) {
                this.stopRunning(TestGroupStatus.SUCCESS);
                return;
            }

            next.run(this.runInfo.getSender(), false, true);
        }
    }

    public void stopRunning(TestGroupStatus status) {
        if (this.runInfo == null) {
            return;
        }
        final CommandSender sender = this.runInfo.getSender();
        sender.sendMessage(CC.YELLOW + "Finished Running, Result: " + status.getDisplayName());
        sender.sendMessage(CC.CHAT_BAR);

        this.status = status;
        this.runInfo = null;
    }

}
