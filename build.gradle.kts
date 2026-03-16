buildscript {
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }
}

subprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}