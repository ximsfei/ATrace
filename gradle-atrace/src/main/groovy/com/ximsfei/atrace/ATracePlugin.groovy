package com.ximsfei.atrace

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ATracePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.getExtensions().findByName("android")
        if (android instanceof AppExtension) {
            android.registerTransform(new ATraceTransform(project, android))
        }
    }
}