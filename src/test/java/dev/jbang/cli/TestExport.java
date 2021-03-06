package dev.jbang.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFileOrDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.jbang.BaseTest;

public class TestExport extends BaseTest {

	@BeforeEach
	void clearJars() throws IOException {
		for (File file : new File(".").listFiles()) {
			if (file.getName().endsWith(".jar")) {
				file.delete();
			}

			if (file.getName().equals("libs")) {
				Files	.walk(file.toPath())
						.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(File::delete);

			}
		}
	}

	@Test
	void testExportFile() throws IOException {
		ExecutionResult result = checkedRun(null, "export", "itests/helloworld.java");
		assertThat(result.err, matchesPattern("(?s).*Exported to.*helloworld.jar.*"));
	}

	@Test
	void testExportPortableNoclasspath() throws IOException {
		ExecutionResult result = checkedRun(null, "export", "--portable", "itests/helloworld.java");
		assertThat(result.err, matchesPattern("(?s).*Exported to.*helloworld.jar.*"));
		assertThat(new File("libs"), not(anExistingFileOrDirectory()));

	}

	@Test
	void testExportPortableWithClasspath() throws IOException {
		ExecutionResult result = checkedRun(null, "export", "--portable", "itests/classpath_log.java");
		assertThat(result.err, matchesPattern("(?s).*Exported to.*classpath_log.jar.*"));
		assertThat(new File("libs"), anExistingDirectory());
		assertThat(new File("libs").listFiles().length, Matchers.equalTo(1));

		File jar = new File("classpath_log.jar");

		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jar))) {
			Manifest mf = jarStream.getManifest();

			String cp = mf.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
			assertThat(cp, not(containsString("m2")));
		}

		Files.delete(jar.toPath());
	}

	@Test
	void testExportWithClasspath() throws IOException {
		ExecutionResult result = checkedRun(null, "export", "itests/classpath_log.java");
		assertThat(result.err, matchesPattern("(?s).*Exported to.*classpath_log.jar.*"));
		assertThat(new File("libs"), not(anExistingDirectory()));

		File jar = new File("classpath_log.jar");

		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jar))) {
			Manifest mf = jarStream.getManifest();

			String cp = mf.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
			assertThat(cp, containsString("m2"));
		}
		Files.delete(jar.toPath());

	}

}
