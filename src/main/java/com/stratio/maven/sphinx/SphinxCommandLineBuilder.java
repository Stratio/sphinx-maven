package com.stratio.maven.sphinx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SphinxCommandLineBuilder {
    private List<String> arguments;
    private final String absoluteOutputDirectory;
    private final String absoluteSourceDirectory;

    public SphinxCommandLineBuilder(String absoluteSourceDirectory, String absoluteOutputDirectory) {
        this.absoluteSourceDirectory = absoluteSourceDirectory;
        this.absoluteOutputDirectory = absoluteOutputDirectory;
        this.arguments = new ArrayList<>();
    }

    public SphinxCommandLineBuilder isWarningAsErrors(boolean warningAsErrors) {
        if (warningAsErrors) {
            arguments.add("-W");
        }
        return this;
    }

    public SphinxCommandLineBuilder isForce(boolean force) {
        if (force) {
            arguments.add("-a");
            arguments.add("-E");
        }
        return this;
    }

    public SphinxCommandLineBuilder setTags(List<String> tags) {
        for (String tag : tags) {
            arguments.add("-t");
            arguments.add(tag);
        }
        return this;
    }

    public SphinxCommandLineBuilder isVerbose(boolean verbose) {
        if (verbose) {
            arguments.add("-v");
        } else {
            arguments.add("-Q");
        }
        return this;
    }

    public String[] build(String builder) {
        arguments.add("-b");
        arguments.add(builder);

        arguments.add("-n");
        arguments.add(absoluteSourceDirectory);
        arguments.add(absoluteOutputDirectory + File.separator + builder);
        return arguments.toArray(new String[]{});
    }
}
