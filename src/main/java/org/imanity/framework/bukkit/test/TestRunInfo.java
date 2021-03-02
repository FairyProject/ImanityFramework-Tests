package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayDeque;
import java.util.Queue;

@Getter
public class TestRunInfo {

    private final TestList testList;
    private final CommandSender sender;
    private final Queue<TestInfo> queue;

    public TestRunInfo(TestList testList, CommandSender sender) {
        this.testList = testList;
        this.sender = sender;
        this.queue = new ArrayDeque<>();
        this.queue.addAll(testList.getList());
    }

    public TestInfo current() {
        return this.queue.peek();
    }

    public TestInfo next() {
        this.queue.remove();
        return this.current();
    }

}
