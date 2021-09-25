package me.ford.biomeremap.settings;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.ford.biomeremap.BiomeRemap;

public class TestConfigBiomeNames {
    private BiomeRemap plugin;

    @BeforeEach
    public void setup() {
        MockBukkit.mock();
        plugin = MockBukkit.load(BiomeRemap.class, new ThrowingLogger());
    }

    @AfterEach
    public void cleanup() {
        MockBukkit.unmock();
    }

    @Test
    public void test_Enums() {
        Settings settings = new Settings(plugin); // will show severe warnings if issues
        settings.reload();
    }

    private final class ThrowingLogger extends Logger {

        protected ThrowingLogger() {
            super("[BiomeRempa TEST]", null);
        }

        @Override
        public void severe(String msg) {
            if (msg.startsWith("World") && msg.endsWith("does not exist")) {
                return; // ignore "World {world} does not exist" messages as those are expected
            }
            Assertions.assertTrue(false, "Severe message: " + msg);
        }

        @Override
        public void log(LogRecord record) {
            System.out.println(record.getLevel() + ": " + record.getMessage());
        }

    }

}