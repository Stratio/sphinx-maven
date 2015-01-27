package com.stratio.maven.sphinx;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReportException;
import org.python.core.Py;
import org.python.core.PySystemState;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class SphinxService {
    private final Log log;
    private final File sphinxSourceDirectory;

    public SphinxService(File sphinxSourceDirectory, Log log) {
        this.log = log;
        this.sphinxSourceDirectory = sphinxSourceDirectory;
    }

    public void runSphinx(String[] args) throws MavenReportException {
        try {
            log.info("Running sphinx on " + sphinxSourceDirectory.getAbsolutePath());
            unpackSphinx();
            log.info("Sphinx correctly unpacked.");

            // this setting supposedly allows GCing of jython-generated classes but I'm
            // not sure if this setting has any effect on newer jython versions anymore
            System.setProperty("python.options.internalTablesImpl", "weak");

            PySystemState engineSys = new PySystemState();

            engineSys.path.append(Py.newString(sphinxSourceDirectory.getAbsolutePath()));
            Py.setSystemState(engineSys);

            ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

            log.info("Executing sphinx with args: " + Arrays.toString(args));

            engine.put("args", args);

            engine.eval("import sphinx");
            Integer result = (Integer) engine.eval("sphinx.main(args)");
            if (result != 0) {
                throw new IOException("Sphinx report generation failed");
            }
        } catch (Exception e) {
            throw new MavenReportException("Error executing sphinx!", e);
        }
    }


    private void unpackSphinx() throws MavenReportException {
        if (!sphinxSourceDirectory.exists() && !sphinxSourceDirectory.mkdirs()) {
            throw new MavenReportException("Could not generate the temporary directory " + sphinxSourceDirectory.getAbsolutePath() + " for the sphinx sources");
        }

        if (sphinxSourceDirectory.exists()) {
            log.info("Unpacking sphinx to " + sphinxSourceDirectory.getAbsolutePath());

            try {
                ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream("jar", SphinxService.class.getResourceAsStream("/sphinx.jar"));
                ArchiveEntry entry = input.getNextEntry();

                while (entry != null) {
                    File archiveEntry = new File(sphinxSourceDirectory, entry.getName());
                    archiveEntry.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        archiveEntry.mkdir();
                        entry = input.getNextEntry();
                        continue;
                    }
                    OutputStream out = new FileOutputStream(archiveEntry);
                    IOUtils.copy(input, out);
                    out.close();
                    entry = input.getNextEntry();
                }
                input.close();
            } catch (Exception ex) {
                throw new MavenReportException("Could not unpack the sphinx source", ex);
            }
        } else {
            log.info("Sphinx already unpacked in " + sphinxSourceDirectory.getAbsolutePath());
        }
    }


}
