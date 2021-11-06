package uk.co.danielrendall.saas.server

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.{MIME_PLAINTEXT, Response, newFixedLengthResponse}
import uk.co.danielrendall.saas.interfaces.{ResponseFactory, ServiceMetadata, ServiceSession, Serviceable}

import scala.collection.mutable

class CoreService extends Serviceable:

  private val nameToService: mutable.Map[String, Serviceable] =
    new mutable.HashMap[String, Serviceable]()

  object Constants:
    val LIST = "_list"
    val SERVICE = "_service"

  override val getMetadata: ServiceMetadata = ServiceMetadata("_core")

  override def get(session: ServiceSession,
                   first: String,
                   rest: List[String])
                  (implicit responseFactory: ResponseFactory): NanoHTTPD.Response =
    first match {
      case Constants.LIST =>
        responseFactory.newFixedLengthResponse(Status.OK, "text/plain", nameToService.keys.toList.sorted.mkString(", "))
      case x if x.startsWith("_") =>
        badRequest(s"Unknown or inappropriate command '$x'")
      case o => nameToService.get(o) match {
        case Some(value) =>
          rest match {
            case head :: next =>
              value.get(session, head, next)
            case Nil =>
              value.get(session, "", List.empty)
          }
        case None =>
          badRequest(s"No service registered for '$o'")
      }
    }

  override def post(session: ServiceSession,
                    first: String,
                    rest: List[String])
                   (implicit responseFactory: ResponseFactory): NanoHTTPD.Response =
    first match {
      case x if x.startsWith("_") =>
        badRequest(s"Unknown or inappropriate command '$x'")
      case o => nameToService.get(o) match {
        case Some(value) =>
          rest match {
            case head :: next =>
              value.post(session, head, next)
            case Nil =>
              value.post(session, "", List.empty)
          }
        case None =>
          badRequest(s"No service registered for '$o'")
      }
    }

  override def put(session: ServiceSession,
                   first: String,
                   rest: List[String])
                  (implicit responseFactory: ResponseFactory): NanoHTTPD.Response =
    first match {
      case Constants.SERVICE =>
        rest match {
          case head :: _ =>
            if (isValidMountPoint(head)) {
              nameToService.get(head) match {
                case Some(_) =>
                  newFixedLengthResponse(Status.CONFLICT, MIME_PLAINTEXT, s"Already have service for '$head' - please delete it first")
                case None =>
                  // TODO - consider adding support for a constructor that takes query parameters...?
                  if (addNewService(head, session)) {
                    okMsg(s"Added '$head'")
                  } else {
                    newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, s"Couldn't add service at '$head'")
                  }
              }
            } else {
              badMountPointResponse(head)
            }
          case Nil =>
            badRequest("No mount point specified")
        }
      case x if x.startsWith("_") =>
        badRequest(s"Unknown or inappropriate command '$x'")
      case o => nameToService.get(o) match {
        case Some(value) =>
          rest match {
            case head :: next =>
              value.put(session, head, next)
            case Nil =>
              value.put(session, "", List.empty)
          }
        case None =>
          badRequest(s"No service registered for '$o'")
      }
    }


  override def delete(session: ServiceSession,
                      first: String,
                      rest: List[String])
                     (implicit responseFactory: ResponseFactory): NanoHTTPD.Response =
    first match {
      case Constants.SERVICE =>
        rest match {
          case head :: _ =>
            if (isValidMountPoint(head)) {
              nameToService.remove(head) match {
                case Some(_) =>
                  okMsg(s"Removed service at '$head'")
                case None =>
                  notFound(s"No service found at '$head'")
              }
            } else {
              badMountPointResponse(head)
            }
          case Nil =>
            badRequest("No mount point specified")
        }
      case x if x.startsWith("_") =>
        badRequest(s"Unknown or inappropriate command '$x'")
      case o => nameToService.get(o) match {
        case Some(value) =>
          rest match {
            case head :: next =>
              value.delete(session, head, next)
            case Nil =>
              value.delete(session, "", List.empty)
          }
        case None =>
          badRequest(s"No service registered for '$o'")
      }
    }

  private def addNewService(head: String,
                            session: ServiceSession): Boolean =
    try {
      val service = ServiceFinder.getService(session.bodyAsBytes)
      nameToService.put(head, service)
      true
    } catch {
      case ex: Exception =>
        println(s"Couldn't add service '$head'")
        ex.printStackTrace()
        false
    }

  private val validMountPointRegex = "^[a-z0-9]+$".r

  private def isValidMountPoint(string: String) = validMountPointRegex.matches(string)

  private def badMountPointResponse(mount: String)
                                   (implicit responseFactory: ResponseFactory): Response =
    badRequest(s"Mount point '$mount' is invalid - must be lower-case letters and digits only")



  // TODO - move these into interface
  private def okMsg(msg: String)
                   (implicit responseFactory: ResponseFactory): Response =
    responseFactory.newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, msg)

  private def notFound(msg: String)
                   (implicit responseFactory: ResponseFactory): Response =
    responseFactory.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, msg)

  private def badRequest(msg: String)
                        (implicit responseFactory: ResponseFactory): Response =
    responseFactory.newFixedLengthResponse(Status.BAD_REQUEST, MIME_PLAINTEXT, msg)
