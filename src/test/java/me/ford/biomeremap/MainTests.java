package me.ford.biomeremap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;

public class MainTests {
	private BiomeRemap plugin;

	@BeforeEach
	public void setUp() {
		MockBukkit.mock();
		plugin = MockBukkit.load(BiomeRemap.class);
	}

	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	public void settings_not_null() {
		Assertions.assertNotNull(plugin.getSettings());
	}

	@Test
	public void messages_not_null() {
		Assertions.assertNotNull(plugin.getMessages());
	}

}
