package me.ford.biomeremap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;

public class MainTests {
	private BiomeRemap plugin;
	
	@Before
	public void setUp() {
	    MockBukkit.mock();
	    plugin = MockBukkit.load(BiomeRemap.class);
	}

	@After
	public void tearDown() {
	    MockBukkit.unload();
	}
	
	@Test
	public void settings_not_null() {
		Assert.assertNotNull(plugin.getSettings());
	}
	
	@Test
	public void messages_not_null() {
		Assert.assertNotNull(plugin.getMessages());
	}
	


}
