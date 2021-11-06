package uk.co.danielrendall.saas.server

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.*
import fi.iki.elonen.NanoHTTPD.Response.Status
import uk.co.danielrendall.saas.interfaces.{ResponseFactory, ServiceSession}

import java.io.InputStream

class ServicesServer(port: Int)
  extends NanoHTTPD(port):

  object Constants {
    val QUIT = "_quit"
  }

  private implicit val responseFactory: ResponseFactory = new ResponseFactory {
    override def newFixedLengthResponse(status: Response.IStatus, mimeType: String, data: InputStream, totalBytes: Long): Response =
      NanoHTTPD.newFixedLengthResponse(status, mimeType, data, totalBytes)

    override def newFixedLengthResponse(status: Response.IStatus, mimeType: String, txt: String): Response =
      NanoHTTPD.newFixedLengthResponse(status, mimeType, txt)
  }

  private val coreService = new CoreService

  override def serve(session: IHTTPSession): Response =
  // URI always starts with "/"
    session.getUri.tail.split("/").filterNot(_.isEmpty).toList match {
      case head::tail =>
        if (head == Constants.QUIT) {
          quit()
        } else {
          session.getMethod match {
            case Method.GET => coreService.get(wrap(session), head, tail)
            case Method.POST => coreService.post(wrap(session), head, tail)
            case Method.PUT => coreService.put(wrap(session), head, tail)
            case Method.DELETE => coreService.delete(wrap(session), head, tail)
            case _ => badRequest("Unsupported method: " + session.getMethod.name())
          }
        }
      case _ =>
        okMsg("ServicesServer is running")
    }

  private def wrap(iHTTPSession: IHTTPSession): ServiceSession = new SessionWrapper(iHTTPSession)

  private def quit(): Response = {
    println("Quitting")
    stop()
    okMsg("")
  }

  private def okMsg(msg: String): Response =
    newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, msg)

  private def badRequest(msg: String): Response =
    newFixedLengthResponse(Status.BAD_REQUEST, MIME_PLAINTEXT, msg)

object ServicesServer:

  private val defaultPort = 1810

  def main(args: Array[String]): Unit = {
    val port = Option(System.getProperty("port")).map(_.toInt).getOrElse(defaultPort)
    new ServicesServer(port).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
  }
