package com.stratio.maven.sphinx;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import java.io.File;
import java.util.List;
import java.util.Locale;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.SITE, requiresReports = true)
public class SphinxMojo extends AbstractMojo implements MavenReport {

    /**
     * The directory containing the sphinx doc source.
     */
    @Parameter(property = "sphinx.sourceDirectory", defaultValue = "${basedir}/src/site/sphinx", required = true)
    private File sourceDirectory;

    /**
     * The output directory for the report.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}", readonly = true, required = true)
    protected File outputDirectory;

    /**
     * Specifies the input encoding.
     */
    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true)
    private String inputEncoding;

    /**
     * The directory for sphinx' source.
     */
    @Parameter(property = "sphinx.sphinxSourceDirectory", defaultValue = "${project.build.directory}/sphinx", required = true, readonly = true)
    private File sphinxSourceDirectory;

    /**
     * The temporal directory for documentation source.
     */
    @Parameter(property = "sphinx.temporalDirectory", defaultValue = "${project.build.directory}/temporalDocs", required = true, readonly = true)
    private File temporalDirectory;

    /**
     * The builder to use. See <a href="http://sphinx.pocoo.org/man/sphinx-build.html?highlight=command%20line">sphinx-build</a>
     * for a list of supported builders.
     */
    @Parameter(property = "sphinx.builders", required = true, alias = "builders")
    private List<String> builders;

    /**
     * The <a href="http://sphinx.pocoo.org/markup/misc.html#tags">tags</a> to pass to the sphinx build.
     */
    @Parameter(property = "sphinx.tags", alias = "tags")
    private List<String> tags;

    /**
     * Resources to be filtered.
     */
    @Parameter
    protected List<Resource> resources;

    /**
     * Whether Sphinx should generate verbose output.
     */
    @Parameter(property = "sphinx.verbose", defaultValue = "true", required = true, alias = "verbose")
    private boolean verbose;

    /**
     * Whether Sphinx should treat warnings as errors.
     */
    @Parameter(property = "sphinx.warningsAsErrors", defaultValue = "false", required = true, alias = "warningsAsErrors")
    private boolean warningsAsErrors;

    /**
     * Whether Sphinx should generate output for all files instead of only the changed ones.
     */
    @Parameter(property = "sphinx.force", defaultValue = "false", required = true, alias = "force")
    private boolean force;

    @Component(role = org.apache.maven.shared.filtering.MavenResourcesFiltering.class, hint = "default")
    protected MavenResourcesFiltering mavenResourcesFiltering;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    @Override
    public void execute() throws MojoExecutionException {
        try {

            getLog().info("Filtering resources [" + resources.toString() + "]");

            FileUtils.deleteDirectory(temporalDirectory);

            FileUtils.copyDirectory(sourceDirectory, temporalDirectory);

            filter();

            getLog().info("Resources filtered. Executing Sphinx.");

            SphinxService ss = new SphinxService(sphinxSourceDirectory, getLog());

            for (String builder : builders) {
                getLog().info("Generating results from builder " + builder);
                String[] args = new SphinxCommandLineBuilder(
                        temporalDirectory.getAbsolutePath(),
                        outputDirectory.getAbsolutePath()
                ).isForce(this.force).isVerbose(verbose).isWarningAsErrors(warningsAsErrors)
                        .setTags(tags).build(builder);

                ss.runSphinx(args);

                getLog().info("Results generated to builder " + builder);
            }

            getLog().info("Dropping temporal directory... ");

            FileUtils.deleteDirectory(temporalDirectory);

        } catch (Exception e) {
            throw new MojoExecutionException("Error executing sphinx maven plugin", e);
        }
    }

    @Override
    public void generate(org.codehaus.doxia.sink.Sink sink, Locale locale) throws MavenReportException {
        try {
            this.execute();
        } catch (MojoExecutionException e) {
            throw new MavenReportException("Error generating report", e);
        }
    }

    private void filter() throws MavenFilteringException {

        MavenResourcesExecution mre = new MavenResourcesExecution(
                resources,
                temporalDirectory,
                project,
                inputEncoding,
                project.getFilters(),
                mavenResourcesFiltering.getDefaultNonFilteredFileExtensions(),
                session);
        mavenResourcesFiltering.filterResources(mre);
    }

    @Override
    public String getOutputName() {
        return "Sphinx";
    }

    @Override
    public String getCategoryName() {
        return "Sphinx";
    }

    @Override
    public String getName(Locale locale) {
        return "Sphinx";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Sphinx";
    }

    @Override
    public void setReportOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public File getReportOutputDirectory() {
        return this.outputDirectory;
    }

    @Override
    public boolean isExternalReport() {
        return true;
    }

    @Override
    public boolean canGenerateReport() {
        return true;
    }
}
