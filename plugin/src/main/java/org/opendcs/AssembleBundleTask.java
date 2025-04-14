package org.opendcs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;


public class AssembleBundleTask extends DefaultTask
{

    @OutputDirectory
    final DirectoryProperty outputDirectory = getProject().getObjects()
                                                          .directoryProperty();

    final Property<MavenPublication> publication = getProject().getObjects().property(MavenPublication.class);

    @TaskAction
    public void assemble()
    {
        var outputDir = outputDirectory.get().getAsFile();
        getProject().delete(outputDir);
        getProject().mkdir(outputDir);
        final var pub = publication.get();
        final var groupFolder = Path.of(outputDir.getAbsolutePath(), pub.getGroupId().split("\\."));
        final var artifactFolder = new File(groupFolder.toFile(),pub.getArtifactId());
        final var versionFolder = new File(artifactFolder, pub.getVersion());
        versionFolder.mkdirs();
        pub.getArtifacts().all(artifact ->
        {
            try
            {
                var newArtifact = new File(versionFolder, artifact.getFile().toPath().getFileName().toString());
                copy(artifact.getFile().toPath(), newArtifact.toPath());
            }
            catch (IOException ex)
            {
                throw new GradleException("Unable to copy required file."+ex.getLocalizedMessage(), ex);
            }
        });

        // get the pom file.
        this.getTaskDependencies().getDependenciesForInternalUse(this).forEach(t -> {
            if (t instanceof GenerateMavenPom pomTask)
            {
                var pomFile = pomTask.getDestination();
                var pomFileCopy = new File(versionFolder, String.format("%s-%s.pom", pub.getArtifactId(), pub.getVersion()));
                try
                {
                    copy(pomFile.toPath(), pomFileCopy.toPath());
                }
                catch (IOException ex)
                {
                    throw new GradleException("Unable to copy pom."+ex.getLocalizedMessage(), ex);
                }
            }
        });
    }


    public DirectoryProperty getOutputDirectory()
    {
        return this.outputDirectory;
    }

    private void copy(Path source, Path dest) throws IOException
    {
        var name = source.getFileName();
        var ascFile = Path.of(source.getParent().toString(), name.toString()+".asc");
        // as valid bundle required the signature so don't both adding anything without one.
        if (ascFile.toFile().exists()) {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            createHashes(dest);
            var destAscFile = Path.of(dest.getParent().toString(),dest.getFileName().toString()+".asc");
            Files.copy(ascFile, destAscFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void createHashes(Path dest) throws IOException
    {
        var name = dest.getFileName();
        var parent = dest.getParent();
        var algorithms = List.of("MD5", "SHA-1", "SHA-256", "SHA-512");
        try
        {
            for(var algorithm: algorithms)
            {
                var digestAlgo = MessageDigest.getInstance(algorithm);
                digestAlgo.update(Files.readAllBytes(dest));
                var digest = digestAlgo.digest();
                var hashPath = Path.of(parent.toString(), name.toString()+"."+algorithm.replace("-","").toLowerCase());
                var hexFormatter = HexFormat.of();
                Files.write(hashPath, hexFormatter.formatHex(digest).getBytes(), StandardOpenOption.WRITE,
                                                                                 StandardOpenOption.TRUNCATE_EXISTING,
                                                                                 StandardOpenOption.CREATE);
            }
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IOException("Unable to create file hashes", ex);
        }

    }
}
