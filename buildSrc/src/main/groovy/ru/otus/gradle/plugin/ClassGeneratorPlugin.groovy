package ru.otus.gradle.plugin;

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

import javax.inject.Inject
import javax.lang.model.element.Modifier


class ClassGeneratorPlugin implements Plugin<Project> {

    static final String PLUGIN_ID = "ru.otus.gradle.generator"
    static final String PLUGIN_TASK_NAME = "generator"
    static final String PLUGIN_EXTENSION_NAME = "generator"
    static final String PLUGIN_CONFIGURATION_NAME = "generator"

    void apply(Project project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")

        def extension = project.extensions.create(PLUGIN_EXTENSION_NAME, GeneratorExtension)

        def generatorConfiguration = project.configurations.maybeCreate(PLUGIN_CONFIGURATION_NAME)

        generatorConfiguration.defaultDependencies {
            addLater(extension.javaPoetVersion.map { project.dependencies.create("com.squareup:javapoet:$it") })
            addLater(extension.springVersion.map { project.dependencies.create("org.springframework.boot:spring-boot-autoconfigure:$it") })
            addLater(extension.springVersion.map { project.dependencies.create("org.springframework.boot:spring-boot:$it") })
        }
        String mainClassName  =  'AAa'
                //extension.mainClass.forEach({println(it)})
        Boolean enableConfigurationProperties = true//extension.mainClass.configurationProperties.get()
        String packageGroup = "A" //extension.mainClass.packageGroup.get()
        project.task(PLUGIN_TASK_NAME) {
            project.version = "" //
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement('$T.run(' + mainClassName + '.class, args)', SpringApplication.class)
                    .build()


            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(mainClassName)
            classBuilder.addAnnotation(SpringBootApplication.class)

            if (enableConfigurationProperties) {
                classBuilder.addAnnotation(EnableConfigurationProperties.class)
            }

            TypeSpec applicationClass = classBuilder
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(main)
                    .build()

            JavaFile javaFile = JavaFile.builder(packageGroup, applicationClass)
                    .build()

            javaFile.writeTo(project.layout.projectDirectory.dir("tmpDir").asFile)
            javaFile.writeTo(System.out)
        }
    }

}


interface  GeneratorExtension {
    @Input Property<String> getSpringVersion()
    @Input Property<String> getJavaPoetVersion()
    MainClassExtension getMainClass()

}

interface  MainClassExtension {
    @Input Property<String> getName()
    @Input Property<String> getPackageGroup()
    @Input Property<Boolean> getConfigurationProperties()
}


