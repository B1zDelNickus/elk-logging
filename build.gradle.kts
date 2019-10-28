spectrumMultimodule("0.5") {
    commonDependency(project(":commons"))
    subprojects {
        publishMaven()
    }
    project(":bundle") {
        dependencies {
            "api"(project(":commons"))
            "api"(project(":slf4j-extensions"))
            "api"(project(":logback-extensions"))
            "api"(project(":templates"))
        }
    }
    project(":templates") {
        dependencies {
            "api"(project(":slf4j-extensions"))
        }
    }
}
