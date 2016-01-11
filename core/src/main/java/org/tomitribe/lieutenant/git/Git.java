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
package org.tomitribe.lieutenant.git;

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Git {

    private static final int NUMBER_OF_CHARS_FOR_COMMIT_ID = 7;

    private Path rootDirectory;
    private Path _gitDirectory;

    private org.eclipse.jgit.api.Git git;

    public Git(File rootDirectory) {
        super();
        if (rootDirectory == null) {
            throw new IllegalArgumentException("Root Directory is not null.");
        }

        this.rootDirectory = Paths.get(rootDirectory.getAbsolutePath());
        this._gitDirectory = Paths.get(rootDirectory.getAbsolutePath(), ".git");
        this.initGit();

        if (!isGit(this.rootDirectory.toFile())) {
            throw new IllegalArgumentException(String.format("No local git repository found in %s.", this.rootDirectory));
        }
    }

    private void initGit() {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository repository = builder.setGitDir(_gitDirectory.toFile())
                    .readEnvironment() // scan environment GIT_* variables
                    .setMustExist(true)
                    .build();
            this.git = new org.eclipse.jgit.api.Git(repository);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String currentBranch() {

        try {
            return this.git.getRepository().getBranch();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String trunkedLatestCommit(String branch) {

        ObjectId resolve = null;
        try {
            resolve = this.git.getRepository().resolve(branch);
            return resolve.abbreviate(NUMBER_OF_CHARS_FOR_COMMIT_ID).name();
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("%s file does not exists or it is not a file.", resolve));
        }

    }

    public boolean isDirty() {

        boolean isDirty;
        try {
            final Status status = this.git.status().call();
            isDirty = !status.isClean();
        } catch (GitAPIException e) {
            throw new IllegalArgumentException(e);
        }
        return isDirty;
    }

    public Set<String> branchList() {
        final Set<String> branches = new HashSet<>();

        try {
            final List<Ref> refs = this.git.branchList().call();
            for (Ref ref : refs) {
                branches.add(getBranchName(ref));
            }
        } catch (GitAPIException e) {
            throw new IllegalArgumentException(e);
        }

        return Collections.unmodifiableSet(branches);
    }

    private String getBranchName(Ref ref) {
        String name = ref.getName();
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash > -1) {
            return name.substring(lastSlash + 1, name.length());
        } else {
            return name;
        }
    }

    public Set<String> tagList(String commit) {

        final Set<String> tags = new HashSet<String>();
        final AbbreviatedObjectId abbreviatedCommit = AbbreviatedObjectId.fromString(commit);

        try {
            List<Ref> refs = this.git.tagList().call();

            for (Ref ref : refs) {

                Ref peeledRef = this.git.getRepository().peel(ref);
                final ObjectId tagCommit;
                if (peeledRef.getPeeledObjectId() != null) {
                    tagCommit = peeledRef.getPeeledObjectId();
                } else {
                    tagCommit = ref.getObjectId();
                }

                if (tagCommit.startsWith(abbreviatedCommit)) {
                    final String name = ref.getName();
                    tags.add(name.substring(name.lastIndexOf('/') + 1, name.length()));
                }
            }

        } catch (GitAPIException e) {
            throw new IllegalArgumentException(e);
        }

        return Collections.unmodifiableSet(tags);
    }

    public static boolean isGit(File dir) {
        Path directory = Paths.get(dir.getAbsolutePath());
        if (Files.isDirectory(directory) && Files.exists(directory)) {
            Path gitDirectory = Paths.get(directory.toFile().getAbsolutePath(), ".git");
            return Files.exists(gitDirectory) && Files.isDirectory(gitDirectory);
        } else {
            return false;
        }
    }
}
