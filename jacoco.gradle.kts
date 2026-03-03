apply(plugin = "jacoco")

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

project.afterEvaluate {
    createJacocoTestReport("jacocoTestReport")
}

//Use this method to generate the HTML and XML coverage report files for Unit and UI test cases
fun createJacocoTestReport(taskName: String) {
    val androidTestCoverageTask = "createDebugCoverageReport"
    val unitTestTask = "testDebugUnitTest"

    tasks.create(name = taskName, type = JacocoReport::class) {
        group = "Reporting"
        description = "Generate Jacoco coverage reports for the Debug build."

        reports {
            html.required.set(true)
            xml.required.set(true)
        }

        val excludes = listOf(
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "android/**/*.*",
                "androidx/**/*.*",
                "**/*MembersInjector*.*",
                "**/*_Factory.*",
                "**/*_Provide*Factory*.*",
                "**/AutoValue_*.*",
                "**/R2.class",
                "**/R2$*.class",
                "**/*Directions$*"
        )

        val kClasses ="${layout.buildDirectory.get()}/tmp/kotlin-classes/debug"
        val kotlinClasses = fileTree(mapOf("dir" to kClasses, "excludes" to excludes))
        classDirectories.setFrom(files(kotlinClasses))
        val sourceDirs = listOf("${project.projectDir}/src/main/java")
        sourceDirectories.setFrom(files(sourceDirs))
        val androidTestsData = fileTree(
            mapOf(
                "dir" to "${layout.buildDirectory.get()}/outputs/code_coverage/debugAndroidTest/connected/",
                "includes" to listOf("**/*.ec")
            )
        )
        executionData(
            files(
                listOf(
                    "${project.layout.buildDirectory.get()}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                    androidTestsData
                )
            )
        )
        dependsOn(unitTestTask,androidTestCoverageTask)
    }
}