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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.HashMap;
import java.util.Map;

public class LieutenantPlugin implements Plugin<Project> {

    private static final String LIEUTENANT_CORE_DEPENDENCY = "org.tomitribe:lieutenant-core:";

    @Override
    public void apply(final Project project) {
        final Configuration configuration = project.getConfigurations().create("lieutenant")
                .setVisible(false)
                .setTransitive(true)
                .setDescription("The Lieutenant Java libraries to be used for this project.");

        final LieutenantExtension lieutenantExtension = project.getExtensions().create("lieutenant", LieutenantExtension.class);

        configuration.getIncoming().beforeResolve(new Action<ResolvableDependencies>() {
            @SuppressWarnings("UnusedMethodParameter")
            public void execute(ResolvableDependencies resolvableDependencies) {
                DependencyHandler dependencyHandler = project.getDependencies();
                DependencySet dependencies = configuration.getDependencies();
                dependencies.add(dependencyHandler.create(LIEUTENANT_CORE_DEPENDENCY + lieutenantExtension.getVersion()));
            }
        });

        Map<String, Class<?>> params = new HashMap<>();
        params.put("type", LieutenantBuildTask.class);
        project.task(params, "lieutenantbuild");
    }
}
