package com.ximsfei.atrace

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import org.gradle.api.Project

class ATraceInject {
    private Project project
    private AppExtension android
    private ClassPool pool

    ATraceInject(Project project, BaseExtension android) {
        this.project = project
        this.android = android
    }

    def prepare() {
        pool = ClassPool.getDefault()
        pool.appendClassPath(ATraceClassPath.getAndroidJarPath(project, android))
    }

    def injectDir(String dir) {
    }

    def injectJar(String jar) {
    }

    private def injectClass(String className) {
    }

    private def injectMethod() {
    }
}