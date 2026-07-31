package gg.moonflower.etched.api.util;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class M3uParser {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String EXTENDED_HEADER = "#EXTM3U";
    private static final Pattern INFO_PATTERN = Pattern.compile("#EXTINF:(?<seconds>[-]?\\d+).*,(?<title>.+)");

    public static List<URL> parse(InputStreamReader reader) throws IOException {
        Iterator<String> filtered = IOUtils.readLines(reader).stream().filter(line -> !line.isEmpty()).map(String::trim).iterator();

        if (!filtered.hasNext()) {
            return Collections.emptyList();
        }

        List<URL> entries = new LinkedList<>();

        String currentLine;
        while (filtered.hasNext()) {
            currentLine = filtered.next();
            if (currentLine.equals(EXTENDED_HEADER) || INFO_PATTERN.matcher(currentLine).matches()) {
                continue;
            }

            while (currentLine.startsWith("#")) {
                if (!filtered.hasNext()) {
                    return entries;
                }
                currentLine = filtered.next();
            }

            try {
                entries.add(new URL(currentLine));
            } catch (Exception e) {
                LOGGER.warn("Could not parse as location: " + currentLine, e);
            }
        }

        return entries;
    }
}
