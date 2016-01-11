/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.lieutenant;

import com.github.dockerjava.api.model.Image;
import org.tomitribe.lieutenant.docker.Docker;
import org.tomitribe.lieutenant.docker.DockerfileFinder;
import org.tomitribe.lieutenant.git.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Lieutenant {

    private static Logger logger = Logger.getLogger(Lieutenant.class.getName());

    private File home;
    Git git;
    Docker docker;
    UsernameResolver usernameResolver;


    public Lieutenant(File home) {
        this.home = home;
        this.usernameResolver = new UsernameResolver();
    }

    public void purge(LieutenantConfig lieutenantConfig, Docker.DockerConfig dockerConfig) {
        Config config = new Config();
        config.setLieutenantConfig(lieutenantConfig);
        config.setDockerConfig(dockerConfig);

        final Set<Path> dockerfiles = getDockerfileLocations();

        for (Path dockerfile : dockerfiles) {
            Application application = new Application(autocreateImageName(dockerfile), dockerfile.toString());
            config.addApplication(dockerfile.toString(), application);
        }

        this.purge(config);
    }

    public void purge(Config config) {

        if (Git.isGit(this.home)) {
            // Avoid expensive operation and also useful for testing purposes
            if (this.git == null) {
                this.git = new Git(this.home);
            }

            if (this.docker == null) {
                this.docker = config.getDockerConfig().build();
            }

            final Set<String> applicationsName = config.getApplicationsName();
            for (String applicationName : applicationsName) {

                final Application application = config.getApplication(applicationName);
                String imageName = resolveImageName(application);
                final Set<String> currentTags = this.docker.listImages(imageName);

                removeExclusionsTags(application, currentTags, config.getLieutenantConfig());

            }

        }
    }

    private void removeExclusionsTags(Application application, final Set<String> currentTags, LieutenantConfig lieutenantConfig) {

        // Current commit-id
        final String currentBranch = this.git.currentBranch();
        final String currentCommit = this.git.trunkedLatestCommit(currentBranch);

        // Live branches
        final Set<String> branches = this.git.branchList();

        // Current git tags
        final Set<String> tagList = this.git.tagList(currentCommit);
        final String imageName = resolveImageName(application);

        Set<String> tagsToRemove = new HashSet<>();

        tag:
        for (Iterator<String> currentFullTagIterator = currentTags.iterator(); currentFullTagIterator.hasNext(); ) {
            String currentFullTag = currentFullTagIterator.next();
            if (isTagMatched(currentFullTag, imageName, currentCommit)) {
                continue;
            }

            if (lieutenantConfig.isWithBranches()) {
                for (String branch : branches) {
                    if (isTagMatched(currentFullTag, imageName, branch)) {
                        continue tag;
                    }
                }

            }

            if (lieutenantConfig.isWithTags()) {
                for (String tag : tagList) {
                    if (isTagMatched(currentFullTag, imageName, tag)) {
                        continue tag;
                    }
                }
            }

            // If I arrive here means that tag must be deleted because it is not in current hash, current live branches
            // or current tags
            tagsToRemove.add(currentFullTag);
        }

        for (String tagToRemove : tagsToRemove) {
            this.docker.remove(tagToRemove);
        }
    }

    private boolean isTagMatched(String currentTag, String image, String tag) {
        final Pattern pattern = Pattern.compile(image + ":.*_" + tag + "_.*");
        return pattern.matcher(currentTag).matches();
    }

    public void build(LieutenantConfig lieutenantConfig, Docker.DockerConfig dockerConfig) {

        Config config = new Config();
        config.setLieutenantConfig(lieutenantConfig);
        config.setDockerConfig(dockerConfig);

        final Set<Path> dockerfiles = getDockerfileLocations();

        for (Path dockerfile : dockerfiles) {
            Application application = new Application(autocreateImageName(dockerfile), dockerfile.toString());
            config.addApplication(dockerfile.toString(), application);
        }

        this.build(config);
    }

    private Set<Path> getDockerfileLocations() {
        Set<Path> relativeDockerfiles = new HashSet<>();
        DockerfileFinder dockerfileFinder = new DockerfileFinder(this.home);
        try {
            Set<Path> dockerfiles = dockerfileFinder.dockerfiles();

            final Path path = this.home.toPath();
            for (Path dockerfile : dockerfiles) {
                relativeDockerfiles.add(path.relativize(dockerfile));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return relativeDockerfiles;
    }

    public void build(Config config) {

        final Set<String> applicationsName = config.getApplicationsName();

        if (this.docker == null) {
            this.docker = config.getDockerConfig().build();
        }

        for (String applicationName : applicationsName) {

            final Application application = config.getApplication(applicationName);

            if (!Git.isGit(this.home)) {

                // If no Git repo exist
                logger.log(Level.INFO, "No local git repository found, just building latest");
                buildImage(this.docker, application, config, "latest");

            } else {

                // Avoid expensive operation and also useful for testing purposes
                if (this.git == null) {
                    this.git = new Git(this.home);
                }

                if (isDirty(this.git)) {

                    logger.log(Level.INFO, "No local git repository found, just building latest");
                    buildImage(this.docker, application, config, "latest");

                } else {

                    final String rev = getRevision(this.git);

                    // Skip build if there are no local changes and the commit is already built
                    if (imageExists(this.docker, application, config, rev) && !config.isForce()) {

                        logger.log(Level.INFO, String.format("Skipping build of %s:%s - image is already built", application.getImage(), rev));

                    } else {

                        logger.log(Level.INFO, "Git repo is clean and image can be built.");

                        buildImage(this.docker, application, config, rev);
                        if (config.withBranch()) {
                            tagImage(this.docker, application, rev, config, getBranch(this.git));
                        }

                        if (config.withTags()) {
                            Set<String> currentTags = currentTags(this.git);

                            for (String currentTag : currentTags) {
                                tagImage(this.docker, application, rev, config, currentTag);
                            }
                        }
                    }
                }
            }
        }
    }

    private String tagImage(Docker docker, Application application, String rev, Config config, String tag) {
        final String imageName = resolveImageName(application);
        final String finalTag = composeTag(config, tag);
        docker.tag(imageName, rev, finalTag);

        return imageName + ":" + finalTag;
    }

    private String buildImage(Docker docker, Application application, Config config, String tag) {
        final File dockerFileLocationOrFile = new File(this.home, application.getBuild());
        final String imageName = resolveImageName(application);
        final String finalTag = composeTag(config, tag);
        docker.build(dockerFileLocationOrFile, config.isForce(), imageName, finalTag);

        return imageName+":"+finalTag;
    }

    private Set<String> currentTags(Git git) {
        return git.tagList(getRevision(git));
    }

    private String getRevision(Git git) {
        String branch = git.currentBranch();
        return git.trunkedLatestCommit(branch);
    }

    private boolean isDirty(Git git) {
        return git.isDirty();
    }

    private String getBranch(Git git) {
        return git.currentBranch();
    }

    private boolean imageExists(Docker docker, Application application, Config config, String tag) {
        return docker.imageExists(resolveImageName(application), composeTag(config, tag));
    }

    private String composeTag(Config config, String tag) {
        if (config.isPrefixSet()) {
            tag = config.getPrefix() + "_" + tag;
        }

        if (config.isSuffixSet()) {
            tag = tag + "_" + config.getSuffix();
        }

        return safeTag(tag);
    }

    private String safeTag(String tag) {
        String safeTag = tag.toLowerCase();
        return safeTag.replace(' ', '_');
    }

    private String resolveImageName(Application application) {
        if (application.isImageSet()) {
            return application.getImage();
        } else {
            final Path dockerfilePath = Paths.get(application.getBuild());
            if (dockerfilePath.getParent() == null) {
                return autocreateImageName(this.home.toPath().resolve(dockerfilePath));
            } else {
                return autocreateImageName(dockerfilePath);
            }
        }
    }

    private String autocreateImageName(Path dockerfile) {
        String imageName = this.usernameResolver.username() + "/" + dockerfile.getParent().getFileName();

        final String filename = dockerfile.getFileName().toString();
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex > -1) {
            imageName = imageName + filename.substring(extensionIndex, filename.length());
        }

        return imageName;
    }

}
