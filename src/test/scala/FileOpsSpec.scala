import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.{Chunk, Pipe}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths
import cats.effect.std.Console

import java.security.MessageDigest
import java.util.Base64

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/26/21, 1:10 PM
 ******************************************************************************/
import cats.implicits._
import cinvestav.utils.files.FilesOpsInterpreter._
import fs2.Stream
import fs2.io.file.Files

class FileOpsSpec extends AnyFunSuite{
  private val PATH = "/home/nacho/Documents/test/default"
  test("Directory to bytes "){
    val app = filesOpsIO
      .directoryToBytes(PATH)
      .debug()
      .compile
      .drain
    app.unsafeRunSync()
    assert(true)

  }
  test("Chunks"){
    val digest:Pipe[IO,Byte,Array[Byte]]=
      _.chunks
        .fold(MessageDigest.getInstance("SHA-512")){(digest,chunk)=>
          digest.update(chunk.toArray)
          digest
        }.map(_.digest)

    val app = Files[IO]
      .directoryStream(Paths.get(PATH))
      .flatMap{
        Files[IO].readAll(_,4096)
          .through(digest)
          .map(Base64.getEncoder.encode)
          .map(new String(_))
      }
      .debug()
      .compile
      .drain
    app.unsafeRunSync()
    assert(true)
  }
}
