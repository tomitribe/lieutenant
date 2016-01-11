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

import org.apache.commons.lang.text.StrSubstitutor;
import org.tomitribe.lieutenant.docker.Docker;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Config {

    private LieutenantConfig lieutenantConfig;

    private Docker.DockerConfig dockerConfig;
    private Map<String, Application> applications = new HashMap<String, Application>();

    public Config() {
        this.dockerConfig = new Docker.DockerConfig();
        this.lieutenantConfig = new LieutenantConfig();
    }

    public Application getApplication(String name) {
        return this.applications.get(name);
    }

    public Set<String> getApplicationsName() {
        return Collections.unmodifiableSet(this.applications.keySet());
    }

    public void addApplication(String name, Application application) {
        this.applications.put(name, application);
    }

    public void setDockerConfig(Docker.DockerConfig dockerConfig) {
        this.dockerConfig = dockerConfig;
    }

    public Docker.DockerConfig getDockerConfig() {
        return dockerConfig;
    }

    public void setLieutenantConfig(LieutenantConfig lieutenantConfig) {
        this.lieutenantConfig = lieutenantConfig;
    }

    public LieutenantConfig getLieutenantConfig() {
        return lieutenantConfig;
    }

    public boolean withBranch() {
        return this.lieutenantConfig.isWithBranches();
    }

    public boolean withTags() {
        return this.lieutenantConfig.isWithTags();
    }

    public boolean isForce() {
        return this.lieutenantConfig.isForce();
    }

    public String getSuffix() {
        return this.lieutenantConfig.getSuffix();
    }

    public boolean isSuffixSet() {
        return this.lieutenantConfig.getSuffix() != null && !"".equals(this.lieutenantConfig.getSuffix().trim());
    }

    public String getPrefix() {
        return this.lieutenantConfig.getPrefix();
    }

    public boolean isPrefixSet() {
        return this.lieutenantConfig.getPrefix() != null && !"".equals(this.lieutenantConfig.getPrefix().trim());
    }

    public static Config readFile(File configFile) {

        final String buildField = "build";
        final String imageField = "image";

        Yaml yaml = new Yaml();
        try {

            Config applicationConfigs = new Config();

            Map<String, Object> config = (Map<String, Object>) yaml.load(new FileInputStream(configFile));
            final Set<String> names = config.keySet();

            for (String name : names) {
                Map<String, String> applicationConfig = (Map<String, String>) config.get(name);
                applicationConfigs.addApplication(name, new Application(applicationConfig.get(imageField), applicationConfig.get(buildField)));
            }

            return applicationConfigs;
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
