package com.ximsfei.atrace

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.CtMethod
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ATraceInject {
    private Project project
    private AppExtension android
    private ATraceExtension atrace
    private ClassPool pool
    private HashSet<String> includePath = []

    ATraceInject(Project project, BaseExtension android, ATraceExtension atrace) {
        this.project = project
        this.android = android
        this.atrace = atrace
    }

    def prepare(TransformInvocation transformInvocation) {
        pool = ClassPool.getDefault()
        pool.appendClassPath(ATraceClassPath.getAndroidJarPath(project, android))
        transformInvocation.inputs.each {
            it.directoryInputs.each {
                pool.insertClassPath(it.file.absolutePath)
            }
            it.jarInputs.each {
                pool.insertClassPath(it.file.absolutePath)
            }
        }
        for (def include : atrace.include) {
            includePath.add(include.replace(".", "/"))
        }
    }

    def injectDir(String path) {
        def dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse {
                String filePath = it.absolutePath
                if (filePath.endsWith(".class")
                        && !filePath.contains('/R$')
                        && !filePath.contains('/R.class')
                        && !filePath.contains("/BuildConfig.class")
                        && !filePath.contains('com/ximsfei/atrace/ATrace')) {
                    for (String include : includePath) {
                        if (filePath.contains(include)) {
                            String className = filePath.replace(path + "/", "").replace("/", '.').replace(".class", "")
                            try {
                                injectClass(className, path)
                            } catch (Exception e) {
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    def injectJar(String path) {
        if (path.endsWith(".jar")) {
            def jar = new File(path)
            String jarZipDir = jar.getParent() + File.separator + jar.getName().replace('.jar', '')
            unzipJar(path, jarZipDir)
            jar.delete()
            injectDir(jarZipDir)
            zipJar(jarZipDir, path)
            FileUtils.deleteDirectory(jarZipDir)
        }
    }

    private def injectClass(String className, String path) {
        def c = pool.getCtClass(className)
        if (c.isFrozen()) {
            c.defrost()
        }
        for (def method : c.declaredMethods) {
            if (null != method.methodInfo.codeAttribute) {
                try {
                    injectMethod(method)
                } catch (Exception e) {
                }
            }
        }
        c.writeFile(path)
        c.detach()
    }

    private def injectMethod(CtMethod method) {
        method.insertBefore("""com.ximsfei.atrace.ATrace.enterMethod("${
            method.longName
        }", \$args);""")
        method.insertAfter("""com.ximsfei.atrace.ATrace.exitMethod("${
            method.longName
        }", (Object) (\$w) \$_);""")
    }

    private def unzipJar(String jarPath, String destDirPath) {
        if (jarPath.endsWith('.jar')) {
            def jarFile = new JarFile(jarPath)
            def jarEntries = jarFile.entries()
            while (jarEntries.hasMoreElements()) {
                def jarEntry = jarEntries.nextElement()
                if (jarEntry.directory) {
                    continue
                }
                def entryName = jarEntry.getName()
                def outFileName = destDirPath + File.separator + entryName
                def outFile = new File(outFileName)
                outFile.getParentFile().mkdirs()
                def inputStream = jarFile.getInputStream(jarEntry)
                def fileOutputStream = new FileOutputStream(outFile)
                fileOutputStream << inputStream
                fileOutputStream.close()
                inputStream.close()
            }
            jarFile.close()
        }
    }

    private def zipJar(String srcPath, String jarPath) {
        def file = new File(srcPath)
        def outputStream = new JarOutputStream(new FileOutputStream(jarPath))
        file.eachFileRecurse {
            def entryName = it.getAbsolutePath().substring(srcPath.length() + 1)
            outputStream.putNextEntry(new ZipEntry(entryName))
            if (!it.directory) {
                def inputStream = new FileInputStream(it)
                outputStream << inputStream
                inputStream.close()
            }
        }
        outputStream.close()
    }
}