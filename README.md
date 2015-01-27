## Introduction <a name="introduction"></a>

The `sphinx-maven` plugin is a [Maven site](http://maven.apache.org/plugins/maven-site-plugin/) plugin that uses
[Sphinx](http://sphinx.pocoo.org/) to generate the main documentation. Sphinx itself is a tool to generate
documentation out of [reStructured Text](http://docutils.sf.net/rst.html) source files.

## Basic Usage <a name="basic-usage"></a>

1.  Create a folder `src/site/sphinx` (this can be changed via options should you want a different folder).
2.  Generate documentation in it. Basically what you need is
    * A configuration file called [conf.py](http://sphinx.pocoo.org/config.html) that defines the theme and other options (such as which output formats etc.)
    * The documentation files in reStructured Text format
    * Additional files such as static files (images etc.), usually in a `_static` sub directory
    * Optionally, a customized theme in a sub directory called `_theme`
    For good examples of documentation, see [Sphinx' examples page](http://sphinx.pocoo.org/examples.html). Personally, I like
    [Werkzeug](http://werkzeug.pocoo.org/docs/) (documentation source is on [github](https://github.com/mitsuhiko/werkzeug/tree/master/docs)) and
    [Celery](http://docs.celeryproject.org/en/latest/index.html) (documentation is also on [github](https://github.com/ask/celery/tree/master/docs)).
3. Add the sphinx maven plugin to your `pom.xml`:

				<build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-site-plugin</artifactId>
                                <version>3.4</version>
                                <configuration>
                                    <reportPlugins>
                                        <plugin>
                                            <groupId>org.apache.maven.plugins</groupId>
                                            <artifactId>maven-project-info-reports-plugin</artifactId>
                                            <version>2.8</version>
                                            <reportSets>
                                                <reportSet>
                                                    <reports></reports>
                                                </reportSet>
                                            </reportSets>
                                        </plugin>
                                        <plugin>
                                            <groupId>com.stratio.maven</groupId>
                                            <artifactId>sphinx-maven-plugin</artifactId>
                                            <version>2.0.0</version>
                                            <configuration>
                                                <builders>
                                                    <entry>singlehtml</entry>
                                                </builders>
                                                <resources>
                                                    <resource>
                                                        <directory>src/site/sphinx</directory>
                                                        <filtering>true</filtering>
                                                        <includes>
                                                            <include>conf.py</include>
                                                        </includes>
                                                    </resource>
                                                </resources>
                                            </configuration>
                                        </plugin>
                                    </reportPlugins>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>

    It is important that you set the `reportSet` attribute of the `project-info-reports` plugin to an empty set of `reports`. If not
    then the default `about` report will be generated which conflicts with the `sphinx-maven` plugin.

4.  Generate the documentation by running

        mvn site

    This will generate the documentation in the `target/site/[builder]` folder.

## CHANGELOG
#### 2.0.0
- Refactor all the code to easy add new improvements.
- Updated all libraries into pom file.
- Added resources filtering. Now you can put in your conf.py file this: `version = '${project.version}'`

## TODOs
- PDF generator only works with jpg not with png.
- Update Sphinx version.
- Improve Sphinx jar generator scripts.