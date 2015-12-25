package org.tomitribe.lieutenant;

import org.tomitribe.lieutenant.docker.Docker;
import org.tomitribe.lieutenant.git.Git;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lieutenant {

    private static Logger logger = Logger.getLogger(Lieutenant.class.getName());

    private File home;

    public Lieutenant(File home) {
        this.home = home;
    }

    public void build() {

    }

    public void build(Config config) {

        final Set<String> applicationsName = config.getApplicationsName();
        final Docker docker = config.getDockerConfig().build();

        for (String applicationName : applicationsName) {

            final Application application = config.getApplication(applicationName);

            if (!Git.isGit(this.home)) {

                // If no Git repo exist
                logger.log(Level.INFO, "No local git repository found, just building latest");
                buildImage(docker, application, "latest");

            } else {
                final Git git = new Git(this.home);

                if (isDirty(git)) {

                    logger.log(Level.INFO, "No local git repository found, just building latest");
                    buildImage(docker, application, "latest");

                } else {

                    final String rev = getRevision(git);

                    // Skip build if there are no local changes and the commit is already built
                    if(imageExists(docker, application.getImage(), rev) && !config.isForce()) {

                        logger.log(Level.INFO, String.format("Skipping build of %s:%s - image is already built", application.getImage(), rev));

                    } else {

                        logger.log(Level.INFO, "Git repo is clean and image can be built.");

                        buildImage(docker, application, rev);
                        tagImage(docker, application, rev, getBranch(git));

                        Set<String> currentTags = currentTags(git);

                        for (String currentTag : currentTags) {
                            tagImage(docker, application, rev, currentTag);
                        }
                    }
                }
            }
        }
    }

    private void tagImage(Docker docker, Application application, String rev, String tag) {


        tag.toLowerCase();
        tag.replace(' ', '_');
    }

    private void buildImage(Docker docker, Application application, String tag) {
    }

    private Set<String> currentTags(Git git) {
        return git.tagList(getRevision(git));
    }

    private String getRevision(Git git) {
        String branch = git.currentBranch();
        return git.trunkedLatestCommit(branch);
    }

    private boolean isDirty(Git git) {
        return git.isDirty();
    }

    private String getBranch(Git git) {
        return git.currentBranch();
    }

    private boolean imageExists(Docker docker, String image, String tag) {
        return docker.imageExists(image, tag);
    }

}
