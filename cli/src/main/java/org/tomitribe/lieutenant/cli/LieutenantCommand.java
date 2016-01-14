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
package org.tomitribe.lieutenant.cli;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.lieutenant.Config;
import org.tomitribe.lieutenant.Lieutenant;
import org.tomitribe.lieutenant.LieutenantConfig;
import org.tomitribe.lieutenant.docker.Docker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LieutenantCommand {

    @Command
    public void purge(@Option("withBranch") @Default("true") boolean withBranch,
                      @Option("withTags") @Default("true") boolean withTags,
                      @Option("dockerproperties") File dockerProperties) throws IOException {

        final File currentDir = new File(".");
        final File lieutenantFile = new File(currentDir, "lieutenant.yml");

        Docker.DockerConfig dockerConfig = loadDockerProperties(dockerProperties);

        Lieutenant lieutenant = new Lieutenant(currentDir);

        if (lieutenantFile.exists()) {

            Config config = Config.readFile(lieutenantFile);
            LieutenantConfig lieutenantConfig = getLieutenantConfig(null, null, withBranch, withTags,
                    null);

            config.setDockerConfig(dockerConfig);
            config.setLieutenantConfig(lieutenantConfig);

            lieutenant.purge(config);

        } else {
            LieutenantConfig lieutenantConfig = getLieutenantConfig(null, null, withBranch, withTags,
                    null);

            lieutenant.purge(lieutenantConfig, dockerConfig);
        }

    }

    @Command
    public void push(@Option("force") @Default("false") boolean force,
                      @Option("prefix") String prefix, @Option("suffix") String suffix,
                      @Option("withBranch") @Default("true") boolean withBranch,
                      @Option("withTags") @Default("true") boolean withTags,
                      @Option("exclusionImages") String exclusionImages,
                      @Option("dockerproperties") File dockerProperties) throws IOException {

        final File currentDir = new File(".");
        final File lieutenantFile = new File(currentDir, "lieutenant.yml");

        Docker.DockerConfig dockerConfig = loadDockerProperties(dockerProperties);

        Lieutenant lieutenant = new Lieutenant(currentDir);

        if (lieutenantFile.exists()) {

            Config config = Config.readFile(lieutenantFile);
            LieutenantConfig lieutenantConfig = getLieutenantConfig(prefix, suffix, withBranch, withTags,
                    exclusionImages);

            config.setDockerConfig(dockerConfig);
            config.setLieutenantConfig(lieutenantConfig);

            lieutenant.push(config);

        } else {
            LieutenantConfig lieutenantConfig = getLieutenantConfig(prefix, suffix, withBranch, withTags,
                    exclusionImages);

            lieutenant.push(lieutenantConfig, dockerConfig);
        }

    }

    @Command
    public void build(@Option("force") @Default("false") boolean force,
                      @Option("prefix") String prefix, @Option("suffix") String suffix,
                      @Option("withBranch") @Default("true") boolean withBranch,
                      @Option("withTags") @Default("true") boolean withTags,
                      @Option("dockerproperties") File dockerProperties) throws IOException {

        final File currentDir = new File(".");
        final File lieutenantFile = new File(currentDir, "lieutenant.yml");

        Docker.DockerConfig dockerConfig = loadDockerProperties(dockerProperties);

        Lieutenant lieutenant = new Lieutenant(currentDir);

        if (lieutenantFile.exists()) {

            Config config = Config.readFile(lieutenantFile);
            LieutenantConfig lieutenantConfig = getLieutenantConfig(prefix, suffix, withBranch, withTags,
                    null);

            config.setDockerConfig(dockerConfig);
            config.setLieutenantConfig(lieutenantConfig);

            lieutenant.build(config);

        } else {
            LieutenantConfig lieutenantConfig = getLieutenantConfig(prefix, suffix, withBranch, withTags,
                    null);

            lieutenant.build(lieutenantConfig, dockerConfig);
        }

    }

    private LieutenantConfig getLieutenantConfig(String prefix, String suffix,
                                                 boolean withBranch, boolean withTags, String exclusionImages) {
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setExclusionImagesPattern(exclusionImages);
        lieutenantConfig.setWithBranches(withBranch);
        lieutenantConfig.setWithTags(withTags);
        lieutenantConfig.setSuffix(suffix);
        lieutenantConfig.setPrefix(prefix);
        return lieutenantConfig;
    }

    private Docker.DockerConfig loadDockerProperties(File dockerProperties) throws IOException {
        Docker.DockerConfig dockerConfig = new Docker.DockerConfig();
        if (dockerProperties != null) {
            if (!dockerProperties.isFile()) {
                throw new IllegalStateException("Not a file: " + dockerProperties.getAbsolutePath());
            }
            if (!dockerProperties.exists()) {
                throw new IllegalStateException("File does not exist: " + dockerProperties.getAbsolutePath());
            }
            if (!dockerProperties.canRead()) {
                throw new IllegalStateException("Not readable: " + dockerProperties.getAbsolutePath());
            }
            final Properties p = new Properties();
            p.load(new FileInputStream(dockerProperties));
            dockerConfig.withProperties(p);
        }

        return dockerConfig;
    }
}
