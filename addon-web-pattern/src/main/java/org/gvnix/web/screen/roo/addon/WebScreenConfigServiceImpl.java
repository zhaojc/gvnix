/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.screen.roo.addon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Service offering some configuration operations
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public class WebScreenConfigServiceImpl implements WebScreenConfigService {

    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private MetadataService metadataService;

    private ComponentContext context;

    /** webmvc-config.xml path */
    private String springMvcConfigFile;

    /** src/main/webapp/WEB-INF/tags/pattern */
    private String patternTagsFolder;

    /** {@inheritDoc} */
    public void setup() {
    	Validate.isTrue(isSpringMvcProject(),
                "Project must be Spring MVC project");
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Add addon repository and dependency to get annotations
        addAnnotations(configuration);

        // Add resources
        addResources();

    }

    /**
     * Adds resource to project
     */
    private void addResources() {
        // TODO

    }

    /** {@inheritDoc} */
    public boolean isSpringMvcProject() {
        return fileManager.exists(getSpringMvcConfigFile());
    }

    /** {@inheritDoc} */
    public String getSpringMvcConfigFile() {
        // resolve path for spring-mvc.xml if it hasn't been resolved yet
        if (springMvcConfigFile == null) {
            springMvcConfigFile = projectOperations.getPathResolver()
                    .getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                            "WEB-INF/spring/webmvc-config.xml");
        }
        return springMvcConfigFile;
    }

    /** {@inheritDoc} */
    public boolean arePattrenArtifactsInstalled() {
        return fileManager.exists(getPatternTagsFolder());
    }

    public String getPatternTagsFolder() {
        if (patternTagsFolder == null) {
            patternTagsFolder = projectOperations
                    .getPathResolver()
                    .getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), "WEB-INF/tags/pattern");
        }
        return patternTagsFolder;
    }

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    /**
     * Add addon repository and dependency to get annotations.
     * 
     * @param configuration
     *            Configuration element
     */
    private void addAnnotations(Element configuration) {

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {

            projectOperations.addRepository(projectOperations.getFocusedModuleName(), new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }

    /**
     * Install plugins defined in external XML file
     * 
     * @param configuration
     */
    @SuppressWarnings("unused")
    private void updatePlugins(Element configuration) {
        List<Element> jasperReportsPlugins = XmlUtils.findElements(
                "/configuration/gvnix/jasperReports/plugins/plugin",
                configuration);
        for (Element pluginElement : jasperReportsPlugins) {
            projectOperations.addBuildPlugin(projectOperations.getFocusedModuleName(), new Plugin(pluginElement));
        }
    }

    /**
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath
     *            the source path
     * @param targetDirectory
     *            the target directory
     */
    @SuppressWarnings("unused")
    private void copyDirectoryContents(String sourceAntPath,
            String targetDirectory) {
    	StringUtils.isNotBlank(sourceAntPath);
    	StringUtils.isNotBlank(targetDirectory);

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        String path = FileUtils.getPath(getClass(), sourceAntPath);
        Collection<URL> urls = OSGiUtils.findEntriesByPattern(
                context.getBundleContext(), path);
        Validate.notNull(urls,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            if (!fileManager.exists(targetDirectory + fileName)) {
                try {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    try {
                    	inputStream = url.openStream();
    	                outputStream = fileManager.createFile(targetDirectory + fileName).getOutputStream();
    	                IOUtils.copy(inputStream, outputStream);
                    }
                    finally {
                    	IOUtils.closeQuietly(inputStream);
                    	IOUtils.closeQuietly(outputStream);
                    }
                } catch (IOException e) {
                    new IllegalStateException(
                            "Encountered an error during copying of resources for MVC JSP addon.",
                            e);
                }
            }
        }
    }
}
