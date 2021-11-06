package uk.co.danielrendall.saas.server

import uk.co.danielrendall.saas.interfaces.Serviceable

import java.io.File
import java.net.{URL, URLClassLoader}
import java.nio.file.{Files, StandardOpenOption}
import java.util.UUID
import java.util.jar.JarFile

object ServiceFinder:

  def getService(bytes: Array[Byte]): Serviceable = {
    val tmpFile = File.createTempFile(UUID.randomUUID().toString, ".jar")
    Files.write(tmpFile.toPath, bytes, StandardOpenOption.CREATE)
    val jarFile = new JarFile(tmpFile)
    val manifest = jarFile.getManifest
    Option(manifest.getMainAttributes.getValue("Serviceable-Class")) match {
      case Some(className) =>
        println("Class name is " + className)
        val classLoader = new URLClassLoader(Seq(tmpFile.toURI.toURL).toArray, this.getClass.getClassLoader)
        val clazz = classLoader.loadClass(className)
        try {
          clazz.newInstance().asInstanceOf[Serviceable]
        } catch {
          case ex: Exception =>
            throw new Exception("Loaded class '" + className + "' but couldn't create Serviceable new instance", ex)
        }
      case None =>
        throw new Exception("No class name found in manifest")
    }
  }
