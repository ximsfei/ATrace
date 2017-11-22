package com.ximsfei.atrace

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

class ATraceTransform extends Transform {
    private Project project
    private AppExtension android
    private ATraceInject inject

    ATraceTransform(Project project, AppExtension android) {
        this.project = project
        this.android = android
        this.inject = new ATraceInject(project, android)
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
        inject.prepare()
        transformInvocation.inputs.forEach() {
            it.directoryInputs.each {
                inject.injectDir(it.file.absolutePath)
            }
            it.jarInputs.each {
                inject.injectJar(it.file.absolutePath)
            }
        }
    }
}