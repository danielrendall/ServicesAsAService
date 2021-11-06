package uk.co.danielrendall.saas.server

import fi.iki.elonen.NanoHTTPD.IHTTPSession
import uk.co.danielrendall.saas.interfaces.ServiceSession

import java.io.{ByteArrayOutputStream, InputStream}
import java.util
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

class SessionWrapper(iHTTPSession: IHTTPSession) extends ServiceSession:

  override def headers: Map[String, String] =
    iHTTPSession.getHeaders.asScala.toMap

  override def queryParameters: Map[String, List[String]] =
    iHTTPSession.getParameters.asScala.view.mapValues(_.asScala.toList).toMap

  override def bodyAsBytes: Array[Byte] = {
    val bodySize: Int = iHTTPSession.getHeaders.asScala.get("content-length").map(_.toInt).getOrElse(0)
    val baos = new ByteArrayOutputStream(bodySize)
    StreamUtils.copy(iHTTPSession.getInputStream, baos, bodySize)
    baos.toByteArray
  }

  override def bodyAsInputStream: InputStream =
    iHTTPSession.getInputStream

  override def bodyAsFiles: Map[String, String] = {
    val map = new util.LinkedHashMap[String, String]()
    iHTTPSession.parseBody(map)
    map.asScala.toMap
  }
