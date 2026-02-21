import me.champeau.gradle.igp.gitRepositories

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("me.champeau.includegit") version "0.3.2"
}

rootProject.name = "Mypy PyCharm Plugin"

gitRepositories {
    include("pycharm-plugin-base") {
        uri.set("https://github.com/szabope/pycharm-plugin-base.git")
        tag.set("latest")
    }
}
