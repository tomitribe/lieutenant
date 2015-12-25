package org.tomitribe.lieutenant;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigTest {

    @Test
    public void shouldReadConfigurationFile() {
        File configFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("lieutenant.yml").getFile());

        final Config config = Config.readFile(configFile);
        assertThat(config.getApplication("hello-world"), is(new Application("myimage/hello-world", "Dockerfile")));
        assertThat(config.getApplication("hello-world-test"), is(new Application("myimage/hello-world-test", "Dockerfile.test")));
    }

}
