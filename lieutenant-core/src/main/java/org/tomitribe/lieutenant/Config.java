package org.tomitribe.lieutenant;

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

    private boolean force;
    private Docker.DockerConfig dockerConfig;
    private Map<String, Application> applications = new HashMap<String, Application>();

    public Config() {
        this.dockerConfig = new Docker.DockerConfig();
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

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isForce() {
        return force;
    }

    public static Config readFile(File configFile) {

        final String BUILD_FIELD = "build";
        final String IMAGE_FIELD = "image";

        Yaml yaml = new Yaml();
        try {

            Config applicationConfigs = new Config();

            Map<String, Object> config = (Map<String, Object>) yaml.load(new FileInputStream(configFile));
            final Set<String> names = config.keySet();

            for (String name : names) {
                Map<String, String> applicationConfig = (Map<String, String>) config.get(name);
                applicationConfigs.addApplication(name, new Application(applicationConfig.get(IMAGE_FIELD), applicationConfig.get(BUILD_FIELD)));
            }

            return applicationConfigs;
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
