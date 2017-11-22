package com.ximsfei.atrace

import com.android.build.gradle.BaseExtension
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

class ATraceClassPath {
    static def getSDKDir(Project project) {
        def localProperties = project.rootProject.file("local.properties")
        def sdkDir
        if (localProperties.exists()) {
            Properties properties = new Properties()
            properties.load(localProperties.newDataInputStream())
            sdkDir = properties.getProperty("sdk.dir")
        } else {
            sdkDir = System.getenv("ANDROID_HOME")
        }
        if (sdkDir) {
            return sdkDir
        }
        throw new InvalidUserDataException("ANDROID_HOME is not defined!")
    }

    static def getCompileSdkVersion(BaseExtension android) {
        def compileSdkVersion = android.compileSdkVersion
        if (compileSdkVersion) {
            return compileSdkVersion
        }
        throw new InvalidUserDataException("compileSdkVersion is not defined!")
    }

    static def getAndroidJarPath(Project project, BaseExtension android) {
        def sdkDir = getSDKDir(project)
        def compileSdkVersion = getCompileSdkVersion(android)
        new StringBuilder().append(sdkDir)
                .append(File.separator).append("platforms")
                .append(File.separator).append(compileSdkVersion)
                .append(File.separator).append("android.jar")
                .toString()
    }
}