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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.Assert.assertThat;

public class DockerfileFinderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldFindAllDockerfiles() throws IOException {
        final File dockerfile = folder.newFile("Dockerfile");
        final File newFolder = folder.newFolder("newFolder");
        final File dockerfile2 = new File(newFolder, "Dockerfile.suffix");
        if (!dockerfile2.createNewFile()) {
            throw new IOException("Dockerfile.suffix could not be created");
        }

        DockerfileFinder dockerfileFinder = new DockerfileFinder(folder.getRoot());
        Set<Path> dockerfiles = dockerfileFinder.dockerfiles();

        assertThat(dockerfiles.size(), CoreMatchers.is(2));
        assertThat(dockerfiles, CoreMatchers.hasItems(dockerfile.toPath(), dockerfile2.toPath()));

    }

}
