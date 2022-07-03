package me.ford.biomeremap.settings;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import dev.ratas.slimedogcore.api.messaging.factory.SDCDoubleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
import me.ford.biomeremap.BiomeRemap;

public class MessageTests {
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

	public void assertMessage(String msg) {
		Assertions.assertFalse(msg.contains("{") && msg.contains("}"));
	}

	private <T> String getFilled(SDCSingleContextMessageFactory<T> factory, T t) {
		return factory.getMessage(factory.getContextFactory().getContext(t)).getFilled();
	}

	private <T1, T2> String getFilled(SDCDoubleContextMessageFactory<T1, T2> factory, T1 t1, T2 t2) {
		return factory.getMessage(factory.getContextFactory().getContext(t1, t2)).getFilled();
	}

	private <T1, T2, T3> String getFilled(SDCTripleContextMessageFactory<T1, T2, T3> factory, T1 t1, T2 t2, T3 t3) {
		return factory.getMessage(factory.getContextFactory().getContext(t1, t2, t3)).getFilled();
	}

	@Test
	public void test_messages_swap_curly_brackets() {
		String msg = getFilled(plugin.getMessages().getBiomeRemapInfo(), "desc", Arrays.asList("A", "B"));
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getBiomeRemapListItem(), "ABC");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getBiomeRemapNoMap(), "world_YAY");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getBiomeRemapProgress(), "That progress!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getBiomeRemapSummary(), 1, 1337L, 42);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getChunkRemapStarted(), "chunkWorld", -5, 10);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getInfoWorldMapped(), "world", "no biome I eve heard of!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getRegionRemapStarted(), "Earth", 149, -10);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getScanChunkHeader(), "header", 5, 50);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getScanChunkStart(), "start", -10, 5);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getScanProgress(), "I've gone far!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getScanRegionHeader(), "REGION HEADS", 10, 4);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getScanRegionStart(), "starting a region", -11, 11);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().getScanListItem(), "THEIS", "BIUUM", 2);
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().errorBiomeMapNotFound(), "I know you!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().errorBiomeNotFound(), "This isn't a biome!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().errorWorldNotFound(), "There's no world!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().errorNotInteger(), "DOUBLE!");
		assertMessage(msg);
		msg = getFilled(plugin.getMessages().errorDuplicateBiomeMapsForWorld(), "The One, the only!!");
		assertMessage(msg);
	}

}