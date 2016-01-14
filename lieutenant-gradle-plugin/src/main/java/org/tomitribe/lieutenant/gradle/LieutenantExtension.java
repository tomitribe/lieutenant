/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.lieutenant.gradle;

import groovy.lang.Closure;
import org.gradle.util.ConfigureUtil;

public class LieutenantExtension {

    private String version = "1.0-SNAPSHOT";
    private boolean force = false;
    private String suffix;
    private String prefix;
    private String rootDirectory = null;
    private boolean withBranch = true;
    private boolean withTags = true;
    private String exclusionImages = null;

    private DockerRegistryCredentials registryCredentials;
    DockerConfig dockerConfig;

    public void registryCredentials(Closure closure) {
        registryCredentials = new DockerRegistryCredentials();
        ConfigureUtil.configure(closure, registryCredentials);
    }

    public void dockerConfig(Closure closure) {
        dockerConfig = new DockerConfig();
        ConfigureUtil.configure(closure, dockerConfig);
    }

    public boolean isWithBranch() {
        return withBranch;
    }

    public void setWithBranch(boolean withBranch) {
        this.withBranch = withBranch;
    }

    public boolean isWithTags() {
        return withTags;
    }

    public void setWithTags(boolean withTags) {
        this.withTags = withTags;
    }

    public String getExclusionImages() {
        return exclusionImages;
    }

    public void setExclusionImages(String exclusionImages) {
        this.exclusionImages = exclusionImages;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public DockerRegistryCredentials getRegistryCredentials() {
        return registryCredentials;
    }

    public void setRegistryCredentials(DockerRegistryCredentials registryCredentials) {
        this.registryCredentials = registryCredentials;
    }

    public DockerConfig getDockerConfig() {
        return dockerConfig;
    }

    public void setDockerConfig(DockerConfig dockerConfig) {
        this.dockerConfig = dockerConfig;
    }
}
