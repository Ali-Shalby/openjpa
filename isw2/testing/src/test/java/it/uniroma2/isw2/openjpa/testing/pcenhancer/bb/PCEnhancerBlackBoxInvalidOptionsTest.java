package it.uniroma2.isw2.openjpa.testing.pcenhancer.bb;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.util.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Manual black-box test for invalid PCEnhancer tool options.
 *
 * Category Partition frame: TBB-026.
 *
 * F8 - Tool invocation validity:
 * invalid value for a documented configuration option.
 */
class PCEnhancerBlackBoxInvalidOptionsTest {

    @Test
    void tbb026InvalidRuntimeUnenhancedClassesValueIsRejected(
            @TempDir Path outputDirectory) {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        try {
            configuration.setLog(
                    "File=stdout, DefaultLevel=WARN"
            );

            configuration.setMetaDataFactory("jpa");

            Options options =
                    new Options();

            /*
             * Valid support options keep the invocation isolated.
             */
            options.setProperty(
                    "directory",
                    outputDirectory.toString()
            );

            options.setProperty(
                    "tmpClassLoader",
                    "false"
            );

            /*
             * RuntimeUnenhancedClasses is a documented OpenJPA
             * configuration option. "definitely-invalid" lies
             * outside its documented value domain.
             */
            options.setProperty(
                    "RuntimeUnenhancedClasses",
                    "definitely-invalid"
            );

            assertThrows(
                    ParseException.class,
                    () -> PCEnhancer.run(
                            configuration,
                            new String[]{
                                    PCEnhancerBlackBoxInvalidOptionsTarget.class
                                            .getName()
                            },
                            options
                    ),
                    "An invalid value for a documented configuration "
                            + "option must be rejected"
            );

        } finally {
            configuration.close();
        }
    }
}

/**
 * Disposable purpose-built target for TBB-026.
 */
class PCEnhancerBlackBoxInvalidOptionsTarget {

    private String value;

    protected PCEnhancerBlackBoxInvalidOptionsTarget() {
    }
}
