package me.ford.biomeremap.updates;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;

import me.ford.biomeremap.BiomeRemap;

public class UpdateChecker {
    private static final String SPIGOT_URL = "https://api.spigotmc.org/legacy/update.php?resource=XXX"; // TODO - ID (and use)

    private final BiomeRemap br;

    private String currentVersion;
    
    private final BiConsumer<VersionResponse, String> versionResponse;

    private UpdateChecker(BiomeRemap plugin, BiConsumer<VersionResponse, String> consumer) {
        this.br = plugin;
        this.currentVersion = br.getDescription().getVersion();
        this.versionResponse = consumer;
    }

    public void check() {
        br.getServer().getScheduler().runTaskAsynchronously(this.br, () -> {
            try {
                HttpURLConnection httpURLConnection = (HttpsURLConnection) new URL(SPIGOT_URL).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT, "Mozilla/5.0");

                String fetchedVersion = Resources.toString(httpURLConnection.getURL(), Charset.defaultCharset());

                boolean latestVersion = fetchedVersion.equalsIgnoreCase(this.currentVersion);

                br.getServer().getScheduler().runTask(this.br, () -> this.versionResponse.accept(latestVersion ? VersionResponse.LATEST : VersionResponse.FOUND_NEW, latestVersion ? this.currentVersion : fetchedVersion));
            } catch (IOException exception) {
                exception.printStackTrace();
                br.getServer().getScheduler().runTask(this.br, () -> this.versionResponse.accept(VersionResponse.UNAVAILABLE, null));
            }
        });
    }
    
    public static enum VersionResponse {
        LATEST,
        FOUND_NEW,
        UNAVAILABLE
    }

}
