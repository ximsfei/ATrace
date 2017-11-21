package com.ximsfei.atrace

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

class ATracePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.gradle.addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                task.inputs.files.files.each {
                    println("pengfeng inputs = " + it.absolutePath)
                }
                task.outputs.files.files.each {
                    println("pengfeng outputs = " + it.absolutePath)
                }
            }

            @Override
            void afterExecute(Task task, TaskState taskState) {

            }
        })
    }
}