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
package org.tomitribe.lieutenant.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.NotFoundException;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Docker {

    private final static Logger log = Logger.getLogger(Docker.class.getName());

    private DockerClientConfig dockerClientConfig;
    protected DockerClient dockerClient;

    private Docker(DockerClientConfig dockerClientConfig) {
        this.dockerClientConfig = dockerClientConfig;

        this.dockerClient = DockerClientBuilder.getInstance(this.dockerClientConfig)
                .build();
    }

    public void push(final String image) {

        String finalImageName = image;
        if (image.indexOf('/') < 0) {
            finalImageName = this.dockerClientConfig.getUsername() + "/" + image;
        }

        final PushImage pushImage = PushImage.valueOf(finalImageName);
        log.log(Level.INFO, String.format("Pushing Image %s", image));
        final PushImageCmd pushImageCmd = this.dockerClient.pushImageCmd(pushImage.getName());
        if (pushImage.getTag() != null && !"".equals(pushImage.getTag())) {
            pushImageCmd.withTag(pushImage.getTag());
        }
        pushImageCmd.exec(new PushImageResultCallback()).awaitSuccess();
        log.log(Level.INFO, String.format("Pushed Image %s", image));

    }

    public void build(File dockerFileLocationOrFile, boolean noCache, String image, String tag) {
        log.log(Level.INFO, String.format("Building and tagging Image %s:%s", image, tag));

        final String id = this.dockerClient.buildImageCmd(dockerFileLocationOrFile)
                .withNoCache(noCache)
                .withTag(getTag(image, tag)).exec(new BuildImageResultCallback()).awaitImageId();

        log.log(Level.INFO, String.format("Built Image %s:%s with id %s", image, tag, id));
    }

    public void tag(String image, String origin, String tag) {
        log.log(Level.INFO, String.format("Tagging image %s:%s as %s:%s", image, origin, image, tag));

        this.dockerClient.tagImageCmd(getTag(image, origin), image, tag).withForce(true).exec();
    }

    public Set<String> listImages(String imageName) {
        log.log(Level.INFO, String.format("Finding image %s", imageName));

        Set<Image> matchImages = new HashSet<>();

        final List<Image> images = this.dockerClient.listImagesCmd().withShowAll(false).exec();
        for (Image currentImage : images) {
            final String[] repoTags = currentImage.getRepoTags();

            for (String repoTag : repoTags) {
                if (getImageTag(repoTag).image.equals(imageName)) {
                    matchImages.add(currentImage);
                }
            }
        }

        final Set<String> currentTags = new HashSet<>();
        for (Image image : matchImages) {
            currentTags.addAll(Arrays.asList(image.getRepoTags()));
        }
        return Collections.unmodifiableSet(currentTags);
    }

    private DockerConfig.ImageTag getImageTag(String tag) {
        return DockerConfig.ImageTag.fromRepoTag(tag);
    }

    public String imageId(String image, String tag) {

        log.log(Level.FINER, String.format("Finding image %s:%s", image, tag));

        final List<Image> images = this.dockerClient.listImagesCmd().exec();
        for (Image currentImage : images) {
            if (isImagedTagged(currentImage, image, tag)) {
                return currentImage.getId();
            }
        }

        return null;
    }

    public boolean imageExists(String image, String tag) {

        log.log(Level.FINER, String.format("Finding existance of image %s:%s", image, tag));

        try {
            this.dockerClient.inspectImageCmd(getTag(image, tag)).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public void remove(String image, String tag) {

        log.log(Level.INFO, String.format("Removing image %s:%s", image, tag));
        try {
            this.dockerClient.removeImageCmd(getTag(image, tag)).exec();
        } catch (NotFoundException e) {
            log.log(Level.FINER, String.format("Image %s:%s already removed.", image, tag));
        }
    }

    public void remove(String imageWithTag) {

        log.log(Level.INFO, String.format("Removing image %s", imageWithTag));
        try {
            this.dockerClient.removeImageCmd(imageWithTag).exec();
        } catch (NotFoundException e) {
            log.log(Level.FINER, String.format("Image %s already removed.", imageWithTag));
        }
    }

    private boolean isImagedTagged(Image currentImage, String image, String tag) {
        final String[] repoTags = currentImage.getRepoTags();
        Arrays.sort(repoTags);
        int index = Arrays.binarySearch(repoTags, getTag(image, tag));

        if (index > -1) {
            return true;
        } else {
            return false;
        }
    }

    private String getTag(String image, String origin) {
        return image + ":" + origin;
    }

    public static class DockerConfig {

        private final DockerClientConfig.DockerClientConfigBuilder configBuilder;

        public DockerConfig() {
            this.configBuilder = DockerClientConfig.createDefaultConfigBuilder();
        }

        public DockerConfig withProperties(Properties p) {
            this.configBuilder.withProperties(p);
            return this;
        }

        public final DockerConfig withUri(String uri) {
            this.configBuilder.withUri(uri);
            return this;
        }

        public final DockerConfig withVersion(String version) {
            this.configBuilder.withVersion(version);
            return this;
        }

        public final DockerConfig withUsername(String username) {
            this.configBuilder.withUsername(username);
            return this;
        }

        public final DockerConfig withPassword(String password) {
            this.configBuilder.withPassword(password);
            return this;
        }

        public final DockerConfig withEmail(String email) {
            this.configBuilder.withEmail(email);
            return this;
        }

        public DockerConfig withRegistryAddress(String serverAddress) {
            this.configBuilder.withServerAddress(serverAddress);
            return this;
        }

        public final DockerConfig withDockerCertPath(String dockerCertPath) {
            this.configBuilder.withDockerCertPath(dockerCertPath);
            return this;
        }

        public final DockerConfig withDockerCfgPath(String dockerCfgPath) {
            this.configBuilder.withDockerCfgPath(dockerCfgPath);
            return this;
        }

        public final DockerConfig withSSLConfig(SSLConfig config) {
            this.configBuilder.withSSLConfig(config);
            return this;
        }

        public final Docker build() {
            return new Docker(this.configBuilder.build());
        }

        private static class ImageTag {
            private String image;
            private String tag;

            private ImageTag(String image, String tag) {
                this.image = image;
                this.tag = tag;
            }

            public static ImageTag fromRepoTag(String repoTag) {
                final int endIndex = repoTag.indexOf(':');
                if (endIndex > -1) {
                    return new ImageTag(repoTag.substring(0, endIndex), repoTag.substring(endIndex + 1, repoTag.length()));
                } else {
                    return new ImageTag(repoTag, "");
                }
            }
        }
    }

    private static class PushImage {
        private String name;
        private String tag;

        public PushImage(String name, String tag) {
            this.name = name;
            this.tag = tag;
        }

        public String getName() {
            return name;
        }

        public String getTag() {
            return tag;
        }

        public String toImageRef() {
            return toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((tag == null) ? 0 : tag.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PushImage other = (PushImage) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (tag == null) {
                if (other.tag != null)
                    return false;
            } else if (!tag.equals(other.tag))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return name + (tag != null ? ":" + tag : "");
        }

        public static PushImage valueOf(String image) {
            String name = null;
            String tag = null;

            // <repositoryurl>:<port>/<organization_namespace>/<image_name>:<tag>
            String[] parts = image.split("/");

            switch(parts.length) {
                case 1: // <image_name>[:<tag>]
                case 2: // <organization_namespace>/<image_name>[:tag]
                {
                    String imageName = image;
                    final int colonIndex = imageName.indexOf(':');
                    if (colonIndex > -1) {
                        name = imageName.substring(0, colonIndex);
                        tag = imageName.substring(colonIndex + 1);
                    } else {
                        name = imageName;
                    }
                    break;
                }
                case 3:  // <repositoryurl>[:<port>]/<organization_namespace>/<image_name>[:<tag>]
                {
                    String imageName = parts[2];
                    final int colonIndex = imageName.indexOf(':');
                    if (colonIndex > -1) {
                        name = parts[0] + "/" + parts[1] + "/" + imageName.substring(0, colonIndex);
                        tag = imageName.substring(colonIndex + 1);
                    } else {
                        name = image;
                    }
                }
            }

            return new PushImage(name, tag);
        }
    }


}
