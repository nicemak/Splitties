/*
 * Copyright 2019 Louis Cognault Ayeva Derman. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    `maven-publish`
    id("com.jfrog.bintray")
}

android {
    setDefaults()
}

kotlin {
    android()
    configure(targets) { configureMavenPublication() }
    sourceSets {
        getByName("commonMain").dependencies {
            api(splitties("experimental"))
        }
        getByName("androidMain").dependencies {
            api(splitties("views"))
            api(Libs.kotlin.stdlibJdk7)
            api(Libs.androidX.annotation)
            implementation(splitties("collections"))
            implementation(splitties("exceptions"))
        }
        matching { it.name.startsWith("android") }.all {
            languageSettings.apply {
                enableLanguageFeature("InlineClasses")
                useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
                useExperimentalAnnotation("splitties.experimental.InternalSplittiesApi")
            }
        }
    }
}

afterEvaluate {
    publishing {
        setupAllPublications(project)
    }

    bintray {
        setupPublicationsUpload(project, publishing, skipMetadataPublication = true)
    }
}
