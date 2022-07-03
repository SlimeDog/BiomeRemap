package me.ford.biomeremap.settings;

import java.util.List;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.factory.SDCDoubleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCVoidContextMessageFactory;
import dev.ratas.slimedogcore.impl.messaging.MessagesBase;
import dev.ratas.slimedogcore.impl.messaging.factory.MsgUtil;

public class Messages extends MessagesBase {
	private static final String FILE_NAME = "messages.yml";
	private SDCDoubleContextMessageFactory<String, List<String>> biomeRemapInfo;
	private SDCVoidContextMessageFactory biomeRemapListHeader;
	private SDCSingleContextMessageFactory<String> biomeRemapNoMap;
	private SDCSingleContextMessageFactory<String> biomeRemapListItem;
	private SDCVoidContextMessageFactory reloaded;
	private SDCTripleContextMessageFactory<String, Integer, Integer> chunkRemapStarted;
	private SDCVoidContextMessageFactory remapInProgress;
	private SDCTripleContextMessageFactory<String, Integer, Integer> regionRemapStarted;
	private SDCVoidContextMessageFactory remapComplete;
	private SDCSingleContextMessageFactory<String> remapProgress;
	private SDCTripleContextMessageFactory<Integer, Long, Integer> biomeRemapSummary;
	private SDCVoidContextMessageFactory infoConfigLoaded;
	private SDCDoubleContextMessageFactory<String, String> infoWorldMapped;
	private SDCVoidContextMessageFactory scanInProgress;
	private SDCTripleContextMessageFactory<Integer, Integer, String> infoChunkRemapFloor;
	private SDCTripleContextMessageFactory<Integer, Integer, String> infoFloorWithDefault;
	private SDCTripleContextMessageFactory<String, Integer, Integer> scanChunkStart;
	private SDCTripleContextMessageFactory<String, Integer, Integer> scanRegionStart;
	private SDCSingleContextMessageFactory<String> scanProgress;
	private SDCTripleContextMessageFactory<String, Integer, Integer> scanChunkHeader;
	private SDCTripleContextMessageFactory<String, Integer, Integer> scanRegionHeader;
	private SDCTripleContextMessageFactory<String, String, Integer> scanListItem;
	private SDCVoidContextMessageFactory warnConfigRecreated;
	private SDCSingleContextMessageFactory<String> errorBiomeMapIncomplete;
	private SDCSingleContextMessageFactory<String> errorBiomeNotFound;
	private SDCSingleContextMessageFactory<String> errorBiomeMapNotFound;
	private SDCVoidContextMessageFactory scanComplete;
	private SDCVoidContextMessageFactory errorNoPermissions;
	private SDCSingleContextMessageFactory<String> errorWorldNotFound;
	private SDCSingleContextMessageFactory<String> errorNotInteger;
	private SDCSingleContextMessageFactory<String> errorDuplicateBiomeMapsForWorld;
	private SDCVoidContextMessageFactory errorConfigUnreadable;
	private SDCDoubleContextMessageFactory<String, String> errorConfigMapIncomplete;
	private SDCSingleContextMessageFactory<String> errorBiomeMapNotAssigned;
	private SDCDoubleContextMessageFactory<String, Integer> errorIncompatibleFloor;
	private SDCSingleContextMessageFactory<String> newVersionAvailable;
	private SDCVoidContextMessageFactory currentVersion;
	private SDCVoidContextMessageFactory updateUnavailable;

	public Messages(SlimeDogPlugin plugin) {
		super(plugin.getCustomConfigManager().getConfig(FILE_NAME));
		loadMessages();
	}

	private void loadMessages() {
		biomeRemapInfo = MsgUtil.doubleContext("{BIOMEREMAP_DESC}", desc -> desc, "{BIOMEREMAP_WORLDS}",
				worldNames -> worldNames.isEmpty() ? "none" : String.join(", ", worldNames),
				getRawMessage("BIOMEREMAP_INFO", "Description: {BIOMEREMAP_DESC} \nWorlds: {BIOMEREMAP_WORLDS}"));
		biomeRemapListHeader = MsgUtil.voidContext(getRawMessage("BIOMEREMAP_LIST_HEADER", "List of biomemaps:"));
		biomeRemapNoMap = MsgUtil.singleContext("{WORLD_NAME}",
				world -> world, getRawMessage("BIOMEREMAP_LIST_NO_MAP", "No biomemap found for world {WORLD_NAME}"));
		biomeRemapListItem = MsgUtil.singleContext("{BIOME_ID}", biome -> biome,
				getRawMessage("BIOMEREMAP_LIST_ITEM", " - {BIOME_ID}"));
		reloaded = MsgUtil.voidContext(getRawMessage("BIOMEREMAP_RELOAD", "BiomeRemap was reloaded successfully."));
		chunkRemapStarted = MsgUtil.tripleContext("{WORLD_NAME}", worldName -> worldName, "{X}", x -> String.valueOf(x),
				"{Z}", z -> String.valueOf(z),
				getRawMessage("BIOMEREMAP_REMAP_CHUNK_START", "Remapping chunk world:{WORLD_NAME} x:{X} z:{Z}"));
		remapInProgress = MsgUtil.voidContext(getRawMessage("BIOMEREMAP_REMAP_IN_PROGRESS",
				"A biome remap is already in progress; please try again in a few minutes"));
		regionRemapStarted = MsgUtil.tripleContext("{WORLD_NAME}", worldName -> worldName, "{X}",
				x -> String.valueOf(x), "{Z}", z -> String.valueOf(z),
				getRawMessage("BIOMEREMAP_REMAP_REGION_START", "Remapping region world:{WORLD_NAME} x:{X} z:{Z}"));
		remapComplete = MsgUtil.voidContext(getRawMessage("BIOMEREMAP_REMAP_COMPLETE", "Remap complete."));
		remapProgress = MsgUtil.singleContext("{PERCENTAGE}", progress -> progress,
				getRawMessage("BIOMEREMAP_REMAP_PROGRESS", "{PERCENTAGE}"));
		biomeRemapSummary = MsgUtil.tripleContext("{CHUNKS}", chunks -> String.valueOf(chunks), "{MILLISECONDS}",
				ms -> String.valueOf(ms), "{TICKS}", ticks -> String.valueOf(ticks),
				getRawMessage("BIOMEREMAP_REMAP_SUMMARY",
						"Remapped {CHUNKS} chunks in {MILLISECONDS} ms in a total of {TICKS} ticks"));
		infoConfigLoaded = MsgUtil.voidContext(
				getRawMessage("INFO_CONFIG_FILES_LOADED_SUCCESSFULLY", "Configuration files loaded successfully"));
		infoWorldMapped = MsgUtil.doubleContext("{WORLD_NAME}", world -> world, "{BIOMEMAP}", bm -> bm,
				getRawMessage("INFO_WORLD_BIOME_MAPPED",
						"Biomemap {BIOMEMAP} was successfully assigned to world {WORLD_NAME}"));
		scanInProgress = MsgUtil.voidContext(getRawMessage("BIOMEREMAP_SCAN_IN_PROGRESS",
				"A biome scan is already in progress; please try again in a few minutes"));
		infoChunkRemapFloor = MsgUtil.tripleContext("{FLOOR}", floor -> String.valueOf(floor), "{DEFAULT_FLOOR}",
				defFloor -> String.valueOf(defFloor), "{WORLD_NAME}", wn -> wn, getRawMessage("INFO_CHUNK_REMAP_FLOOR",
						"The floor is set to {FLOOR} in world {WORLD_NAME} (default {DEFAULT_FLOOR})"));
		infoFloorWithDefault = MsgUtil.tripleContext("{FLOOR}", floor -> String.valueOf(floor),
				"{DEFAULT_FLOOR}", defFloor -> String.valueOf(defFloor), "{WORLD_NAME}", wn -> wn,
				getRawMessage("BIOMEREMAP_INFO_FLOOR",
						"The floor is set to {FLOOR} in world {WORLD_NAME} (default {DEFAULT_FLOOR})"));
		scanChunkStart = MsgUtil.tripleContext("{WORLD_NAME}", worldName -> worldName, "{X}", x -> String.valueOf(x),
				"{Z}", z -> String.valueOf(z),
				getRawMessage("BIOMEREMAP_SCAN_CHUNK_START", "Scanning chunk world:{WORLD_NAME} x:{X} z:{Z}"));
		scanRegionStart = MsgUtil.tripleContext("{WORLD_NAME}", worldName -> worldName, "{X}", x -> String.valueOf(x),
				"{Z}", z -> String.valueOf(z),
				getRawMessage("BIOMEREMAP_SCAN_REGION_START", "Scanning region world:{WORLD_NAME} x:{X} z:{Z}"));
		scanProgress = MsgUtil.singleContext("{PERCENTAGE}", progress -> progress,
				getRawMessage("BIOMEREMAP_SCAN_PROGRESS", "{PERCENTAGE}"));
		scanChunkHeader = MsgUtil.tripleContext("{WORLD_NAME}", worldName -> worldName, "{X}", x -> String.valueOf(x),
				"{Z}", z -> String.valueOf(z),
				getRawMessage("BIOMEREMAP_SCAN_CHUNK_HEADER", "Biomes in chunk world:{WORLD_NAME} x:{X} z:{Z}"));
		scanRegionHeader = MsgUtil.tripleContext("{WORLD_NAME}", worldName -> worldName, "{X}", x -> String.valueOf(x),
				"{Z}", z -> String.valueOf(z),
				getRawMessage("BIOMEREMAP_SCAN_REGION_HEADER", "Biomes in region world:{WORLD_NAME} x:{X} z:{Z}"));
		scanListItem = MsgUtil.tripleContext("{PERCENTAGE}", percentage -> percentage, "{BIOME_ID}", biome -> biome,
				"{AMOUNT}", amount -> String.valueOf(amount),
				getRawMessage("BIOMEREMAP_SCAN_LIST_ITEM", "{PERCENTAGE} ({AMOUNT}) {BIOME_ID}"));
		warnConfigRecreated = MsgUtil.voidContext(getRawMessage("WARN_CONFIG_FILES_RECREATED",
				"Configuration files do not exist; default files were created"));
		errorBiomeMapIncomplete = MsgUtil.singleContext("{BIOMEMAP}", map -> map,
				getRawMessage("ERROR_BIOMEMAP_INCOMPLETE", "Biomemap {BIOMEMAP} definition is incomplete"));
		errorBiomeNotFound = MsgUtil.singleContext("{BIOME_ID}", biome -> biome,
				getRawMessage("ERROR_BIOME_NOT_FOUND", "Biome {BIOME_ID} does not exist"));
		errorBiomeMapNotFound = MsgUtil.singleContext("{BIOMEMAP}", mapName -> mapName,
				getRawMessage("ERROR_BIOMEMAP_NOT_FOUND", "Biomemap {BIOMEMAP} does not exist"));
		scanComplete = MsgUtil.voidContext(getRawMessage("BIOMEREMAP_SCAN_COMPLETE", "Scan complete"));
		errorNoPermissions = MsgUtil.voidContext(
				getRawMessage("ERROR_NO_PERMISSION", "You do not have permission to execute that command."));
		errorWorldNotFound = MsgUtil.singleContext("{WORLD_NAME}", worldName -> worldName,
				getRawMessage("ERROR_WORLD_NOT_FOUND", "World name {WORLD_NAME} was not found."));
		errorNotInteger = MsgUtil.singleContext("{VALUE}", value -> value,
				getRawMessage("ERROR_PARAMETER_INVALID_INTEGER", "{VALUE} is not a value integer"));
		errorDuplicateBiomeMapsForWorld = MsgUtil.singleContext("{WORLD_NAME}", worldName -> worldName,
				getRawMessage("ERROR_WORLD_DUPLICATE_ASSIGNMENT",
						"Multiple biomemaps are assigned to world {WORLD_NAME}; fix configuration and reload"));
		errorConfigUnreadable = MsgUtil.voidContext(getRawMessage("ERROR_CONFIG_FILE_UNREADABLE",
				"Cannot read config.yml; no biomemaps were assigned to worlds"));
		errorConfigMapIncomplete = MsgUtil.doubleContext("{BIOMEMAP}", map -> map, "{BIOME_ID}", biome -> biome,
				getRawMessage("ERROR_CONFIG_MAP_INCOMPLETE",
						"Biomemap {BIOMEMAP} has incomplete map for biome {BIOME_ID}; fix configuration and reload"));
		errorBiomeMapNotAssigned = MsgUtil.singleContext("{BIOMEMAP}", map -> map,
				getRawMessage("ERROR_NO_BIOMEMAP_ASSIGNMENT",
						"Errors were found in biomemap {BIOMEMAP}; biomemap was not assigned to any worlds"));
		errorIncompatibleFloor = MsgUtil.doubleContext("{BIOMEMAP}", map -> map, "{FLOOR}",
				floor -> String.valueOf(floor),
				getRawMessage("ERROR_INCOMPATIBLE_FLOOR", "Incompatible floor found for biomemap {BIOMEMAP}: {FLOOR}"));
		newVersionAvailable = MsgUtil.singleContext("{VERSION}", version -> version,
				getRawMessage("BIOMEREMAP_UPDATE_NEW_VERSION", "A new version {VERSION} is available for download"));
		currentVersion = MsgUtil
				.voidContext(getRawMessage("BIOMEREMAP_UPDATE_CURRENT_VERSION", "You are running the latest version"));
		updateUnavailable = MsgUtil.voidContext(getRawMessage("+BIOMEREMAP_UPDATE_INFO_UNAVAILABLE",
				"Version update information is not available at this time"));
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		loadMessages();
	}

	public String getPrefix() {
		return getRawMessage("BIOMEREMAP_PREFIX", "[BiomeRemap] ");
	}

	public SDCDoubleContextMessageFactory<String, List<String>> getBiomeRemapInfo() {
		return biomeRemapInfo;
	}

	public SDCVoidContextMessageFactory getBiomeRemapListHeaders() {
		return biomeRemapListHeader;
	}

	public SDCSingleContextMessageFactory<String> getBiomeRemapNoMap() {
		return biomeRemapNoMap;
	}

	public SDCSingleContextMessageFactory<String> getBiomeRemapListItem() {
		return biomeRemapListItem;
	}

	public SDCVoidContextMessageFactory getBiomeRemapReload() {
		return reloaded;
	}

	public SDCTripleContextMessageFactory<String, Integer, Integer> getChunkRemapStarted() {
		return chunkRemapStarted;
	}

	public SDCVoidContextMessageFactory getBiomeRemapInPrgoress() {
		return remapInProgress;
	}

	public SDCTripleContextMessageFactory<String, Integer, Integer> getRegionRemapStarted() {
		return regionRemapStarted;
	}

	public SDCVoidContextMessageFactory getBiomeRemapComplete() {
		return remapComplete;
	}

	public SDCSingleContextMessageFactory<String> getBiomeRemapProgress() {
		return remapProgress;
	}

	public SDCTripleContextMessageFactory<Integer, Long, Integer> getBiomeRemapSummary() {
		return biomeRemapSummary;
	}

	public SDCVoidContextMessageFactory getInfoConfigLoaded() {
		return infoConfigLoaded;
	}

	public SDCDoubleContextMessageFactory<String, String> getInfoWorldMapped() {
		return infoWorldMapped;
	}

	public SDCTripleContextMessageFactory<Integer, Integer, String> getInfoChunkRemapFloor() {
		return infoChunkRemapFloor;
	}

	public SDCTripleContextMessageFactory<Integer, Integer, String> getInfoFloorWithDefault() {
		return infoFloorWithDefault;
	}

	public SDCVoidContextMessageFactory getScanInProgress() {
		return scanInProgress;
	}

	public SDCTripleContextMessageFactory<String, Integer, Integer> getScanChunkStart() {
		return scanChunkStart;
	}

	public SDCTripleContextMessageFactory<String, Integer, Integer> getScanRegionStart() {
		return scanRegionStart;
	}

	public SDCSingleContextMessageFactory<String> getScanProgress() {
		return scanProgress;
	}

	public SDCVoidContextMessageFactory getScanComplete() {
		return scanComplete;
	}

	public SDCTripleContextMessageFactory<String, Integer, Integer> getScanChunkHeader() {
		return scanChunkHeader;
	}

	public SDCTripleContextMessageFactory<String, Integer, Integer> getScanRegionHeader() {
		return scanRegionHeader;
	}

	public SDCTripleContextMessageFactory<String, String, Integer> getScanListItem() {
		return scanListItem;
	}

	public SDCVoidContextMessageFactory warnConfigRecreated() {
		return warnConfigRecreated;
	}

	public SDCSingleContextMessageFactory<String> errorBiomeMapIncomplete() {
		return errorBiomeMapIncomplete;
	}

	public SDCSingleContextMessageFactory<String> errorBiomeNotFound() {
		return errorBiomeNotFound;
	}

	public SDCSingleContextMessageFactory<String> errorBiomeMapNotFound() {
		return errorBiomeMapNotFound;
	}

	public SDCVoidContextMessageFactory errorNoPermissions() {
		return errorNoPermissions;
	}

	public SDCSingleContextMessageFactory<String> errorWorldNotFound() {
		return errorWorldNotFound;
	}

	public SDCSingleContextMessageFactory<String> errorNotInteger() {
		return errorNotInteger;
	}

	public SDCSingleContextMessageFactory<String> errorDuplicateBiomeMapsForWorld() {
		return errorDuplicateBiomeMapsForWorld;
	}

	public SDCVoidContextMessageFactory errorConfigUnreadable() {
		return errorConfigUnreadable;
	}

	public SDCDoubleContextMessageFactory<String, String> errorConfigMapincomplete() {
		return errorConfigMapIncomplete;
	}

	public SDCSingleContextMessageFactory<String> errorNoBiomeMapAssigned() {
		return errorBiomeMapNotAssigned;
	}

	public SDCDoubleContextMessageFactory<String, Integer> errorIncompatibleFloor() {
		return errorIncompatibleFloor;
	}

	// Update messages

	public SDCSingleContextMessageFactory<String> updateNewVersionAvailable() {
		return newVersionAvailable;
	}

	public SDCVoidContextMessageFactory updateCurrentVersion() {
		return currentVersion;
	}

	public SDCVoidContextMessageFactory updateInfoUnavailable() {
		return updateUnavailable;
	}

}
