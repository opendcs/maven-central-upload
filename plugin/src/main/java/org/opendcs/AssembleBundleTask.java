package org.opendcs;

import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Task;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.bundling.Zip;

public class AssembleBundleTask extends Zip
{
    @Inject
    public AssembleBundleTask(List<MavenPublication> publications)
    {
        super();
        var spec = new Spec<Task>() {
                        @Override
                        public boolean isSatisfiedBy(Task arg0) {
                            return false;
                        }
                    };
                    
        this.getOutputs().upToDateWhen(spec);
        final var localUrl = "file:///home/mike/.m2/repository)";
        final var group = (String)getProject().getGroup();
        final var bundleBase = String.format("%s%s",localUrl, group.replaceAll("\\.","/"));
        final var version = (String)getProject().getVersion();
        this.getArchiveBaseName().set("central-api-bundle");
        this.getDestinationDirectory().set(getProject().getLayout().getBuildDirectory());
        this.from(bundleBase).filesMatching(".*" + version + ".*", null);
        this.doLast(s ->
        {
            var archiveFile = this.getArchiveFile().get().getAsFile();
            System.out.println("Base" + bundleBase + " version " + version);
            System.out.println(archiveFile.getAbsolutePath());
            System.out.println(archiveFile.length()/1024.0/1024.0 + " MB");
        });
    }
}
