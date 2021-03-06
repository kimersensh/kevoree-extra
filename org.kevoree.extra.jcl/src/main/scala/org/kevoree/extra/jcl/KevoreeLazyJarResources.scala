package org.kevoree.extra.jcl

import java.io._
import java.util.jar.JarInputStream
import org.xeustechnologies.jcl.exception.JclException
import java.net.URL
import org.xeustechnologies.jcl.ClasspathResources
import java.lang.String
import org.slf4j.LoggerFactory
import java.util.ArrayList
;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 19:13
 */

class KevoreeLazyJarResources/*(classExtension : String)*/ extends ClasspathResources {

  protected val jarContentURL = new java.util.HashMap[String, URL]
  private val logger = LoggerFactory.getLogger(classOf[KevoreeLazyJarResources].getName);
  var lastLoadedJar: String = ""

  def getLastLoadedJar = lastLoadedJar

  //  def getContentURL(name: String) = jarContentURL.get(name)

  var lazyload = true

  def setLazyLoad(_lazyload: Boolean) {
    lazyload = _lazyload
  }


  override def loadJar(jarStream: InputStream) {
    loadJar(jarStream, null)
  }

  override def loadJar(url: URL) {
    var in: InputStream = null;
    try {
      in = url.openStream();
      loadJar(in, url);
    } catch {
      case e: IOException => throw new JclException(e);
    } finally {
      if (in != null)
        try {
          in.close();
        } catch {
          case e: IOException => throw new JclException(e);
        }
    }
  }

  override def loadJar(jarFile: String) {
    lastLoadedJar = jarFile
    var fis: FileInputStream = null;
    try {
      val f = new File(jarFile)
      fis = new FileInputStream(jarFile);
      loadJar(fis, new URL("file:" + f.getAbsolutePath))
    } catch {
      case e: IOException => throw new JclException(e);
    } finally {
      if (fis != null)
        try {
          fis.close();
        } catch {
          case e: IOException => throw new JclException(e);
        }
    }

  }

  def loadJar(jarStream: InputStream, baseurl: URL) {
    var bis: BufferedInputStream = null;
    var jis: JarInputStream = null;
    try {
      bis = new BufferedInputStream(jarStream);
      jis = new JarInputStream(bis);
      var jarEntry = jis.getNextJarEntry
      while (jarEntry != null) {
        if (!jarEntry.isDirectory) {
          if (jarContentURL.containsKey(jarEntry.getName)) {
            if (!collisionAllowed) {
              throw new JclException("Class/Resource " + jarEntry.getName() + " already loaded");
            }
          } else {
            if (baseurl != null && lazyload) {
              if (jarEntry.getName.endsWith(".class")) {
                jarContentURL.put(jarEntry.getName, new URL("jar:" + baseurl + "!/" + jarEntry.getName))
              } else {
                if (!detectedResourcesURL.containsKey(jarEntry.getName)) {
                  detectedResourcesURL.put(jarEntry.getName, new ArrayList[URL]())
                }
                detectedResourcesURL.get(jarEntry.getName).add(new URL("jar:" + baseurl + "!/" + jarEntry.getName))
              }
            } else {
              val b = new Array[Byte](2048)
              val out = new ByteArrayOutputStream();
              var len = 0;
              while (jis.available() > 0) {
                len = jis.read(b);
                if (len > 0) {
                  out.write(b, 0, len);
                }
              }
              out.flush()
              out.close()
              val key_url = "file:kclstream:" + jarStream.hashCode()+jarEntry.getName
              if (jarEntry.getName.endsWith(".class")) {
                jarContentURL.put(jarEntry.getName, new URL(key_url))
              } else {
                if (!detectedResourcesURL.containsKey(jarEntry.getName)) {
                  detectedResourcesURL.put(jarEntry.getName, new ArrayList[URL]())
                }
                detectedResourcesURL.get(jarEntry.getName).add(new URL(key_url))
              }
              if (jarEntry.getName.endsWith(".jar")) {
                logger.debug("KCL Found sub Jar => " + jarEntry.getName)
                loadJar(new ByteArrayInputStream(out.toByteArray))
              } else {
                if (jarEntry.getName.endsWith(".class")) {
                  jarEntryContents.put(jarEntry.getName, out.toByteArray)
                } else {
                  detectedResources.put(new URL(key_url), out.toByteArray)
                }
              }
            }
          }
        }
        jarEntry = jis.getNextJarEntry
      }
    } catch {
      case e: IOException => new JclException(e)
      case e: NullPointerException =>
    } finally {
      if (jis != null)
        try {
          jis.close();
        } catch {
          case _@e => throw new JclException(e);
        }
      if (bis != null)
        try {
          bis.close();
        } catch {
          case _@e => throw new JclException(e);
        }
    }
  }

  protected override def getJarEntryContents(name: String): Array[Byte] = {
    if (jarContentURL.containsKey(name)) {
      if (jarEntryContents.containsKey(name)) {
        jarEntryContents.get(name)
      } else {
        if (jarContentURL.get(name) != null) {
          val b = new Array[Byte](2048)
          val out = new ByteArrayOutputStream();
          var len = 0;
          val stream = jarContentURL.get(name).openStream()
          while (stream.available() > 0) {
            len = stream.read(b);
            if (len > 0) {
              out.write(b, 0, len);
            }
          }
          stream.close()
          out.flush()
          out.close()
          jarEntryContents.put(name, out.toByteArray)
          out.toByteArray
        } else {
          null
        }
      }
    } else {
      null
    }
  }


  private val detectedResourcesURL = new java.util.HashMap[String, java.util.List[URL]]()
  private val detectedResources = new java.util.HashMap[URL, Array[Byte]]()

  def getResourceURLS(name: String): java.util.List[URL] = {
    if (containResource(name)) {
      detectedResourcesURL.get(name)
    } else {
      new ArrayList[URL]()
    }
  }

  def containResource(name: String): Boolean = {
    if (detectedResourcesURL.get(name) != null) {
      !detectedResourcesURL.get(name).isEmpty
    } else {
      false
    }
  }

  def getResourceURL(name: String): URL = {
    if (containResource(name)) {
      detectedResourcesURL.get(name).get(0)
    } else {
      null
    }
  }

  def getResourceContent(resUrl: URL): Array[Byte] = {
    if (detectedResources.containsKey(resUrl)) {
      detectedResources.get(resUrl)
    } else {
      if (!resUrl.toString.startsWith("file:kclstream:")) {
        val b = new Array[Byte](2048)
        val out = new ByteArrayOutputStream();
        var len = 0;
        val stream = resUrl.openStream()
        while (stream.available() > 0) {
          len = stream.read(b);
          if (len > 0) {
            out.write(b, 0, len);
          }
        }
        stream.close()
        out.flush()
        out.close()
        detectedResources.put(resUrl, out.toByteArray)
        out.toByteArray
      } else {
        null
      }
    }
  }

  /*
  def getResourceContent(resUrl: URL): Array[Byte] = {
    if (resUrl != null) {
      if (detectedResources.containsKey(resUrl)) {
        detectedResources.get(resUrl)
      } else {
        //TRY TO RESOLVE URL
        if (resUrl.toString.startsWith("file:kclstream:")) {
          val b = new Array[Byte](2048)
          val out = new ByteArrayOutputStream();
          var len = 0;
          val stream = jarContentURL.get(name).openStream()
          while (stream.available() > 0) {
            len = stream.read(b);
            if (len > 0) {
              out.write(b, 0, len);
            }
          }
          stream.close()
          out.flush()
          out.close()
          detectedResources.put(resUrl, out.toByteArray)
          out.toByteArray
        } else {
          null
        }
      }
    } else {
      println("null res content = " + name + ",url=" + resUrl)
      null
    }
  }*/
}