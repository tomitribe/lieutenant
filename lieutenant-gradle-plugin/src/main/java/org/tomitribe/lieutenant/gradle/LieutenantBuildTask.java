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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.tomitribe.lieutenant.Config;
import org.tomitribe.lieutenant.Lieutenant;
import org.tomitribe.lieutenant.LieutenantConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LieutenantBuildTask extends DefaultTask {

    private LieutenantExtension extension;

    @TaskAction
    public void build() {
        extension = getProject().getExtensions().findByType(LieutenantExtension.class);

        if (extension == null) {
            extension = new LieutenantExtension();
        }

        final File projectRoot = ConfigUtil.projectRoot(getProject(), extension);
        final Lieutenant lieutenant = new Lieutenant(projectRoot);

        final File lieutenantFile = new File(projectRoot, "lieutenant.yml");
        final List<String> build = new ArrayList<>();

        if (lieutenantFile.exists()) {

            Config config = Config.readFile(lieutenantFile);
            LieutenantConfig lieutenantConfig = ConfigUtil.toLieutenantConfig(extension);

            config.setDockerConfig(ConfigUtil.toDockerConfig(extension));
            config.setLieutenantConfig(lieutenantConfig);

            build.addAll(lieutenant.build(config));

        } else {
            LieutenantConfig lieutenantConfig = ConfigUtil.toLieutenantConfig(extension);
            build.addAll(lieutenant.build(lieutenantConfig, ConfigUtil.toDockerConfig(extension)));
        }

        getProject().getExtensions().getExtraProperties().set("lieutenantImages", Collections.unmodifiableList(build));
    }

}
