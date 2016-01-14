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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tomitribe.lieutenant.docker.Docker;
import org.tomitribe.lieutenant.git.Git;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LieutenantTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    Docker docker;

    @Mock
    Git git;

    @Mock
    UsernameResolver usernameResolver;

    @Before
    public void prepare() throws IOException {
        this.folder.newFolder(".git");
        when(usernameResolver.username()).thenReturn("alex");
    }



    @Test
    public void shouldPurgeOldImages() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        Set<String> taggedImages = new HashSet<>();
        taggedImages.add("myimage/hello-world:foo_11111111_bar");
        taggedImages.add("myimage/hello-world:foo_22222222_bar");
        taggedImages.add("myimage/hello-world:foo_mytag1_bar");
        taggedImages.add("myimage/hello-world:foo_mytag2_bar");
        taggedImages.add("myimage/hello-world:foo_myfeature1_bar");
        taggedImages.add("myimage/hello-world:foo_myfeature2_bar");

        when(this.docker.listImages("myimage/hello-world")).thenReturn(taggedImages);
        when(this.docker.listImages("myimage/hello-world-test")).thenReturn(new HashSet<String>());

        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("22222222");
        Set<String> branches = new HashSet<>();
        branches.add("myfeature2");
        when(this.git.branchList()).thenReturn(branches);

        Set<String> tags = new HashSet<>();
        tags.add("mytag2");
        when(this.git.tagList("22222222")).thenReturn(tags);

        lieutenant.purge(Config.readFile(configFile));

        verify(this.docker, times(1)).remove("myimage/hello-world:foo_11111111_bar");
        verify(this.docker, times(1)).remove("myimage/hello-world:foo_mytag1_bar");
        verify(this.docker, times(1)).remove("myimage/hello-world:foo_myfeature1_bar");

        verify(this.docker, times(0)).remove("myimage/hello-world:foo_22222222_bar");
        verify(this.docker, times(0)).remove("myimage/hello-world:foo_mytag2_bar");
        verify(this.docker, times(0)).remove("myimage/hello-world:foo_myfeature2_bar");

    }

    @Test
    public void shouldPushImagesFromConfigFile() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        when(this.docker.imageExists("myimage/hello-world", "1234567")).thenReturn(false);
        when(this.docker.imageExists("myimage/hello-world-test", "1234567")).thenReturn(false);

        final Config config = Config.readFile(configFile);
        lieutenant.push(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "1234567");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "1234567");

        verify(this.docker).tag("myimage/hello-world", "1234567", "master");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "master");

        verify(this.docker).tag("myimage/hello-world", "1234567", "first_release");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "first_release");

        verify(this.docker).push("myimage/hello-world:1234567");
        verify(this.docker).push("myimage/hello-world:master");
        verify(this.docker).push("myimage/hello-world:first_release");

        verify(this.docker).push("myimage/hello-world-test:1234567");
        verify(this.docker).push("myimage/hello-world-test:master");
        verify(this.docker).push("myimage/hello-world-test:first_release");
    }

    @Test
    public void shouldPushOnlyImagesMatchesRegExpFromConfigFile() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        when(this.docker.imageExists("myimage/hello-world", "1234567")).thenReturn(false);
        when(this.docker.imageExists("myimage/hello-world-test", "1234567")).thenReturn(false);

        final Config config = Config.readFile(configFile);
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setExclusionImagesPattern(".*test.*");
        config.setLieutenantConfig(lieutenantConfig);
        lieutenant.push(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "1234567");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "1234567");

        verify(this.docker).tag("myimage/hello-world", "1234567", "master");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "master");

        verify(this.docker).tag("myimage/hello-world", "1234567", "first_release");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "first_release");

        verify(this.docker).push("myimage/hello-world:1234567");
        verify(this.docker).push("myimage/hello-world:master");
        verify(this.docker).push("myimage/hello-world:first_release");

        verify(this.docker, times(0)).push("myimage/hello-world-test:1234567");
        verify(this.docker, times(0)).push("myimage/hello-world-test:master");
        verify(this.docker, times(0)).push("myimage/hello-world-test:first_release");
    }

    @Test
    public void shouldBuildImageFromConfigFile() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        when(this.docker.imageExists("myimage/hello-world", "1234567")).thenReturn(false);
        when(this.docker.imageExists("myimage/hello-world-test", "1234567")).thenReturn(false);

        final Config config = Config.readFile(configFile);
        lieutenant.build(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "1234567");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "1234567");

        verify(this.docker).tag("myimage/hello-world", "1234567", "master");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "master");

        verify(this.docker).tag("myimage/hello-world", "1234567", "first_release");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "first_release");

    }

    @Test
    public void shouldBuildImageFromConfigFileWithSuffixAndPrefix() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        when(this.docker.imageExists("myimage/hello-world", "1234567")).thenReturn(false);
        when(this.docker.imageExists("myimage/hello-world-test", "1234567")).thenReturn(false);

        final Config config = Config.readFile(configFile);
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setSuffix("foo");
        lieutenantConfig.setPrefix("bar");
        config.setLieutenantConfig(lieutenantConfig);

        lieutenant.build(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "bar_1234567_foo");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "bar_1234567_foo");

        verify(this.docker).tag("myimage/hello-world", "1234567", "bar_master_foo");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "bar_master_foo");

        verify(this.docker).tag("myimage/hello-world", "1234567", "bar_first_release_foo");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "bar_first_release_foo");

    }

    @Test
    public void shouldBuildImageFromConfigFileNoGit() {
        new File(this.folder.getRoot(), ".git").delete();

        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        final Config config = Config.readFile(configFile);
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setSuffix("foo");
        lieutenantConfig.setPrefix("bar");
        config.setLieutenantConfig(lieutenantConfig);

        lieutenant.build(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "bar_latest_foo");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "bar_latest_foo");

    }

    @Test
    public void shouldBuildImageFromConfigFileDirty() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(true);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        final Config config = Config.readFile(configFile);
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setSuffix("foo");
        lieutenantConfig.setPrefix("bar");
        config.setLieutenantConfig(lieutenantConfig);

        lieutenant.build(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "bar_latest_foo");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "bar_latest_foo");

    }

    @Test
    public void shouldNotBuildImageIfImageExistsAndNoForce() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        when(this.docker.imageExists("myimage/hello-world", "bar_1234567_foo")).thenReturn(true);
        when(this.docker.imageExists("myimage/hello-world-test", "bar_1234567_foo")).thenReturn(true);

        final Config config = Config.readFile(configFile);
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setSuffix("foo");
        lieutenantConfig.setPrefix("bar");
        config.setLieutenantConfig(lieutenantConfig);

        lieutenant.build(config);

        verify(this.docker, times(0)).build(new File(this.folder.getRoot(), "Dockerfile"), false, "myimage/hello-world", "bar_1234567_foo");
        verify(this.docker, times(0)).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), false, "myimage/hello-world-test", "bar_1234567_foo");

        verify(this.docker, times(0)).tag("myimage/hello-world", "1234567", "bar_master_foo");
        verify(this.docker, times(0)).tag("myimage/hello-world-test", "1234567", "bar_master_foo");

        verify(this.docker, times(0)).tag("myimage/hello-world", "1234567", "bar_first_release_foo");
        verify(this.docker, times(0)).tag("myimage/hello-world-test", "1234567", "bar_first_release_foo");

    }

    @Test
    public void shouldBuildImageIfImageExistsAndForce() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        when(this.docker.imageExists("myimage/hello-world", "bar_1234567_foo")).thenReturn(true);
        when(this.docker.imageExists("myimage/hello-world-test", "bar_1234567_foo")).thenReturn(true);

        final Config config = Config.readFile(configFile);
        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setSuffix("foo");
        lieutenantConfig.setPrefix("bar");
        lieutenantConfig.setForce(true);
        config.setLieutenantConfig(lieutenantConfig);

        lieutenant.build(config);

        verify(this.docker).build(new File(this.folder.getRoot(), "Dockerfile"), true, "myimage/hello-world", "bar_1234567_foo");
        verify(this.docker).build(new File(this.folder.getRoot(), "mytest/Dockerfile.test"), true, "myimage/hello-world-test", "bar_1234567_foo");

        verify(this.docker).tag("myimage/hello-world", "1234567", "bar_master_foo");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "bar_master_foo");

        verify(this.docker).tag("myimage/hello-world", "1234567", "bar_first_release_foo");
        verify(this.docker).tag("myimage/hello-world-test", "1234567", "bar_first_release_foo");

    }

    @Test
    public void shouldBuildImageWithoutLieutenantConfigFile() throws IOException {

        final File myproject = this.folder.newFolder("myproject");
        final File dockerfile = new File(myproject, "Dockerfile.test");
        if (!dockerfile.createNewFile()) {
            fail();
        }

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;
        lieutenant.usernameResolver = usernameResolver;

        when(this.git.isDirty()).thenReturn(false);
        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");

        Set<String> tags = new HashSet<>();
        tags.add("first release");
        when(this.git.tagList("1234567")).thenReturn(tags);

        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenantConfig.setForce(false);
        lieutenantConfig.setSuffix("foo");
        lieutenantConfig.setPrefix("bar");
        lieutenant.build(lieutenantConfig, new Docker.DockerConfig());

        verify(this.docker).build(new File(this.folder.getRoot(), "myproject/Dockerfile.test"), false, "alex/myproject.test", "bar_1234567_foo");
        verify(this.docker).tag("alex/myproject.test", "1234567", "bar_master_foo");
    }

    @Test
    public void shouldPurgeImageWithoutLieutenantConfigFile() throws IOException {

        final File myproject = this.folder.newFolder("myproject");
        final File dockerfile = new File(myproject, "Dockerfile.test");
        if (!dockerfile.createNewFile()) {
            fail();
        }

        Lieutenant lieutenant = new Lieutenant(this.folder.getRoot());
        lieutenant.docker = this.docker;
        lieutenant.git = this.git;
        lieutenant.usernameResolver = usernameResolver;

        Set<String> taggedImages = new HashSet<>();
        taggedImages.add("alex/myproject.test:foo_11111111_bar");
        taggedImages.add("alex/myproject.test:foo_1234567_bar");
        taggedImages.add("alex/myproject.test:foo_mytag1_bar");
        taggedImages.add("alex/myproject.test:foo_mytag2_bar");

        when(this.docker.listImages("alex/myproject.test")).thenReturn(taggedImages);

        when(this.git.currentBranch()).thenReturn("master");
        when(this.git.trunkedLatestCommit("master")).thenReturn("1234567");
        Set<String> branches = new HashSet<>();
        when(this.git.branchList()).thenReturn(branches);

        Set<String> tags = new HashSet<>();
        tags.add("mytag2");
        when(this.git.tagList("1234567")).thenReturn(tags);


        LieutenantConfig lieutenantConfig = new LieutenantConfig();
        lieutenant.purge(lieutenantConfig, new Docker.DockerConfig());

        verify(this.docker, times(1)).remove("alex/myproject.test:foo_11111111_bar");
        verify(this.docker, times(1)).remove("alex/myproject.test:foo_mytag1_bar");

        verify(this.docker, times(0)).remove("alex/myproject.test:foo_1234567_bar");
        verify(this.docker, times(0)).remove("alex/myproject.test:foo_mytag2_bar");
    }
}
