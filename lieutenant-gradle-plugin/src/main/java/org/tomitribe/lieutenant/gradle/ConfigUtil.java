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

import org.gradle.api.Project;
import org.tomitribe.lieutenant.LieutenantConfig;
import org.tomitribe.lieutenant.docker.Docker;

import java.io.File;

public class ConfigUtil {

    private ConfigUtil() {
    }

    public static Docker.DockerConfig toDockerConfig(LieutenantExtension extension) {
        Docker.DockerConfig dockerConfig = new Docker.DockerConfig();

        final DockerRegistryCredentials registryCredentials = extension.getRegistryCredentials();
        if (registryCredentials != null) {
            dockerConfig.withRegistryAddress(registryCredentials.getUrl());
            dockerConfig.withUsername(registryCredentials.getUsername());
            dockerConfig.withEmail(registryCredentials.getEmail());
            dockerConfig.withPassword(registryCredentials.getPassword());
        }

        final DockerConfig dockerConfigExtension = extension.getDockerConfig();
        if(dockerConfigExtension != null) {
            dockerConfig.withUri(dockerConfigExtension.getUrl());
            dockerConfig.withDockerCertPath(dockerConfigExtension.getDockerCertPath());
            dockerConfig.withVersion(dockerConfigExtension.getVersion());
        }

        return dockerConfig;
    }

    public static File projectRoot(Project project, LieutenantExtension extension) {
        if (extension.getRootDirectory() == null) {
            return project.getProjectDir();
        } else {
            return new File(extension.getRootDirectory());
        }
    }

    public static LieutenantConfig toLieutenantConfig(LieutenantExtension extension) {
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setPrefix(extension.getPrefix());
        lieutenantConfig.setSuffix(extension.getSuffix());
        lieutenantConfig.setExclusionImagesPattern(extension.getExclusionImages());
        lieutenantConfig.setForce(extension.isForce());
        lieutenantConfig.setWithTags(extension.isWithTags());
        lieutenantConfig.setWithBranches(extension.isWithBranch());


        return lieutenantConfig;
    }

}
