package uk.co.danielrendall.saas.server

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import scala.annotation.tailrec

object StreamUtils:

  private val bufferSize: Int = 16384

  def copy(is: InputStream, os: OutputStream, size: Int): Unit = {
    val buf = Array.ofDim[Byte](bufferSize)

    @tailrec
    def copyRec(remaining: Int): Unit = {
      if (remaining > 0) {
        val bytesRead = is.read(buf, 0, bufferSize)
        os.write(buf, 0, bytesRead)
        copyRec(remaining - bytesRead)
      }
    }

    copyRec(size)
    os.flush()
  }

  def toByteArray(is: InputStream, size: Int): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    copy(is, baos, size)
    baos.toByteArray
  }
