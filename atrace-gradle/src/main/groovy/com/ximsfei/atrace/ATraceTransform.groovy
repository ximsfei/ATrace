package com.ximsfei.atrace

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Format
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project
import org.apache.commons.io.FileUtils

class ATraceTransform extends Transform {
    private Project project
    private AppExtension android
    private ATraceExtension atrace
    private ATraceInject inject

    ATraceTransform(Project project, AppExtension android, ATraceExtension atrace) {
        this.project = project
        this.android = android
        this.atrace = atrace
        this.inject = new ATraceInject(project, android, atrace)
    }

    @Override
    String getName() {
        return "ATrace"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        inject.prepare(transformInvocation)

        transformInvocation.inputs.each {
            it.directoryInputs.each {
                def dest = transformInvocation.outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                println "dir src = ${it.file.absolutePath} dest = $dest"
                FileUtils.copyDirectory(it.file, dest)
                inject.injectDir(dest.absolutePath)
            }
            it.jarInputs.each {
                def dest = transformInvocation.outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                println "src = ${it.file.absolutePath} dest = $dest"
                FileUtils.copyFile(it.file, dest)
//                inject.injectJar(dest.absolutePath)
            }
        }
    }
}