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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class DockerfileFinder {

    private File startingDir;

    public DockerfileFinder(File startingDir) {
        if (startingDir == null) {
            throw new IllegalArgumentException("Starting Directory should contain none null value.");
        }
        this.startingDir = startingDir;
    }

    public Set<Path> dockerfiles() throws IOException {
        final DockfileFileVisitor finder = new DockfileFileVisitor();
        Files.walkFileTree(this.startingDir.toPath(), finder);

        return finder.getDockerfiles();
    }

    private static class DockfileFileVisitor extends SimpleFileVisitor<Path> {

        private final Set<Path> dockerfiles = new HashSet<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

            if (file.getFileName().toString().startsWith("Dockerfile")) {
                dockerfiles.add(file);
            }

            return FileVisitResult.CONTINUE;
        }

        public Set<Path> getDockerfiles() {
            return dockerfiles;
        }
    }
}
