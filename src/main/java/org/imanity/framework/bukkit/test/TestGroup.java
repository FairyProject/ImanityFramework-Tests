package org.imanity.framework.bukkit.test;

import lombok.Getter;
import org.bukkit.Material;

public interface TestGroup {

    String name();

    default TestGroupConfiguration config() {
        return new TestGroupConfiguration();
    }

    @Getter
    class TestGroupConfiguration {

        private boolean runInGroup = false;
        private boolean requireExecutor = false;
        private Material displayMaterial = Material.BOOK;

        public TestGroupConfiguration runInGroup() {
            this.runInGroup = true;
            return this;
        }

        public TestGroupConfiguration requireExecutor() {
            this.requireExecutor = true;
            return this;
        }

        public TestGroupConfiguration displayMaterial(Material material) {
            this.displayMaterial = material;
            return this;
        }

    }

}
