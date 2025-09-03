package org.pm4knime.util;

import java.nio.file.Path;

import org.knime.node.parameters.widget.choices.Label;


 // webui classes
public final class NodeSettingsUtils {
    private NodeSettingsUtils() {
    }

    /**
     * Enum that can be used for a {@link ValueSwitchWidget} to let the user decide whether an existing output file
     * should be overwritten oder lead to node failure.
     */
    public enum ExistingOutputFileHandlingMode {
            /** Fail if output file already exists */
            @Label("Fail") //
            FAIL, //
            /** Overwrite the output file if it already exists */
            @Label("Overwrite") //
            OVERWRITE;
    }

    /**
     * Get path to a file of the given filename in the user's home directory
     *
     * @param filename the filename that should be appended to the user's home directory path
     * @return The absolute file path
     */
    public static String getPathInUserHomeDir(final String filename) {
        var userHome = System.getProperty("user.home");
        if (userHome == null) {
            userHome = "."; // Use current working directory in case "user.home" could not be found (should never happen)
        }
        return Path.of(userHome).resolve(filename).toAbsolutePath().toString();
    }


}

