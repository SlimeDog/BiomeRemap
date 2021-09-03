package me.ford.biomeremap.settings;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.ford.biomeremap.BiomeRemap;

public class MessageTests {
	private BiomeRemap plugin;

	@Before
	public void setUp() {
		MockBukkit.mock();
		plugin = MockBukkit.load(BiomeRemap.class);
	}

	@After
	public void tearDown() {
		MockBukkit.unmock();
	}

	public void assertMessage(String msg) {
		Assert.assertFalse(msg.contains("{") && msg.contains("}"));
	}

	@Test
	public void test_messages_swap_curly_brackets() {
		String msg = plugin.getMessages().getBiomeRemapInfo("desc", Arrays.asList("A", "B"));
		assertMessage(msg);
		msg = plugin.getMessages().getBiomeRemapListItem("ABC");
		assertMessage(msg);
		msg = plugin.getMessages().getBiomeRemapNoMap("world_YAY");
		assertMessage(msg);
		msg = plugin.getMessages().getBiomeRemapProgress("That progress!");
		assertMessage(msg);
		msg = plugin.getMessages().getBiomeRemapSummary(1, 1337, 42);
		assertMessage(msg);
		msg = plugin.getMessages().getChunkRemapStarted("chunkWorld", -5, 10);
		assertMessage(msg);
		msg = plugin.getMessages().getInfoWorldMapped("world", "no biome I eve heard of!");
		assertMessage(msg);
		msg = plugin.getMessages().getRegionRemapStarted("Earth", 149, -10);
		assertMessage(msg);
		msg = plugin.getMessages().getScanChunkHeader("header", 5, 50);
		assertMessage(msg);
		msg = plugin.getMessages().getScanChunkStart("start", -10, 5);
		assertMessage(msg);
		msg = plugin.getMessages().getScanProgress("I've gone far!");
		assertMessage(msg);
		msg = plugin.getMessages().getScanRegionHeader("REGION HEADS", 10, 4);
		assertMessage(msg);
		msg = plugin.getMessages().getScanRegionStart("starting a region", -11, 11);
		assertMessage(msg);
		msg = plugin.getMessages().getScanListItem("THEIS", "BIUUM", 2);
		assertMessage(msg);
		msg = plugin.getMessages().errorBiomeMapNotFound("I know you!");
		assertMessage(msg);
		msg = plugin.getMessages().errorBiomeNotFound("This isn't a biome!");
		assertMessage(msg);
		msg = plugin.getMessages().errorWorldNotFound("There's no world!");
		assertMessage(msg);
		msg = plugin.getMessages().errorNotInteger("DOUBLE!");
		assertMessage(msg);
		msg = plugin.getMessages().errorDuplicateBiomeMapsForWorld("The One, the only!!");
		assertMessage(msg);
	}

}
