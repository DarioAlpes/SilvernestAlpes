package co.smartobjects.interno.aspectj.weaver

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class AspectJWeaverInterno implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def configuracion = project.extensions.create('aspectJInterno', ConfiguracionAspectJWeaverInterno)
        SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets")

        project.task("cargarTareasAspectJ") {
            doLast {
                ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties",
                        classpath: sourceSets.main.compileClasspath.asPath)
            }
        }

        project.task("compileAspect") {
            dependsOn project.getTasks().getByName("cargarTareasAspectJ")
            dependsOn project.getTasks().getByName("classes").dependsOn.toArray()
            doLast {
                compilarSourceSetAspectJ(project, sourceSets.main, configuracion.rutaAspectos)
            }
        }

        project.task("compileAspectTest") {
            dependsOn project.getTasks().getByName("cargarTareasAspectJ")
            dependsOn project.getTasks().getByName("testClasses").dependsOn.toArray()
            doLast {
                compilarSourceSetAspectJ(project, sourceSets.test, configuracion.rutaAspectos)
            }
        }
        project.getTasks().getByName("classes").dependsOn(project.getTasks().getByName("compileAspect"))
        project.getTasks().getByName("testClasses").dependsOn(project.getTasks().getByName("compileAspectTest"))
    }


    static def compilarSourceSetAspectJ(Project project, sourceSet, rutaAspectos){
        sourceSet.output.classesDirs.forEach { directorio ->
            if(directorio.exists()) {
                def sinAspectos = new File("${directorio.toPath()}_sin_aspectos")
                directorio.renameTo(sinAspectos)
                project.getAnt().iajc(
                        maxmem: "1024m", fork: "true", Xlint: "warning",
                        showWeaveInfo: "true",
                        debugLevel: "lines,vars,source",
                        destDir: directorio,
                        aspectPath: rutaAspectos,
                        inpath: sinAspectos,
                        classpath: sourceSet.compileClasspath.asPath,
                        source: project.sourceCompatibility,
                        target: project.targetCompatibility
                )
                sinAspectos.deleteDir()
            }
        }
    }
}

class ConfiguracionAspectJWeaverInterno {
    String rutaAspectos
}
