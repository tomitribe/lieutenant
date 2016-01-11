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

import com.github.dockerjava.api.model.Image;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@Ignore("Needs Docker installation")
public class DockerTest {

    private static Docker docker;

    @BeforeClass
    public static void createDockerClient() {
        Properties properties = new Properties();
        properties.put("docker.io.url", "https://192.168.99.100:2376");
        final String certPath = System.getProperty("user.home") + "/.docker/machine/machines/dev";
        properties.put("docker.io.dockerCertPath", certPath);

        Docker.DockerConfig dockerConfig = new Docker.DockerConfig();
        docker = dockerConfig.withProperties(properties).build();
    }

    @Test
    public void shouldBuildDockerContainers() {

        try {
            File dockerFile = new File(Thread.currentThread().getContextClassLoader()
                    .getResource("simple/TestDockerFile").getFile());

            docker.build(dockerFile, false, "lieutenant/test", "mytag");
            assertThat(docker.imageId("lieutenant/test", "mytag"), notNullValue());
        } finally {
            docker.remove("lieutenant/test", "mytag");
        }
    }

    @Test
    public void shouldTagAnImage() {

        try {
            File dockerFile = new File(Thread.currentThread().getContextClassLoader()
                    .getResource("simple/TestDockerFile").getFile());

            docker.build(dockerFile, false, "lieutenant/test", "mytag");
            docker.tag("lieutenant/test", "mytag", "myothertag");

            assertThat(docker.imageId("lieutenant/test", "myothertag"), notNullValue());
        } finally {
            docker.remove("lieutenant/test", "mytag");
            docker.remove("lieutenant/test", "myothertag");
        }
    }

    @Test
    public void shouldReturnListOfImagesByName() {
        try {
            File dockerFile = new File(Thread.currentThread().getContextClassLoader()
                    .getResource("simple/TestDockerFile").getFile());

            File dockerFile2 = new File(Thread.currentThread().getContextClassLoader()
                    .getResource("simple/Test2DockerFile").getFile());

            docker.build(dockerFile, false, "lieutenant/test", "mytag");
            docker.build(dockerFile2, false, "lieutenant/test", "mytag2");

            final Set<String> images = docker.listImages("lieutenant/test");
            assertThat(images.size(), is(2));

        } finally {
            docker.remove("lieutenant/test", "mytag");
            docker.remove("lieutenant/test", "mytag2");
        }
    }

    @Test
    public void shouldReturnFalseInCaseOfNotExistingImage() {
        assertThat(docker.imageExists("alex", "soto"), is(false));
    }

    @Test
    public void shouldReturnListOfImages() {
        docker.listImages("");
    }
}
