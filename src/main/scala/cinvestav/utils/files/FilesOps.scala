package cinvestav.utils.files

import cats.effect.{Blocker, ContextShift, IO, Timer}
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms.MessageDigestAlgorithms
import cinvestav.utils.Utils
import fs2.io.file.{copy, deleteIfExists, directoryStream,writeAll}
import java.nio.file.{Files, Paths}
import cinvestav.crypto.hashfunction.HashFunctions
import cinvestav.crypto.hashfunction.HashFunctionsInterpreter.hashFunctionAlgorithmIO
import cinvestav.utils.UtilsInterpreter.utilsIO
import fs2.{Pipe, Stream}
//import fs2.io.readInputStream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
//
case class FileInfo(name:String,bytes:Array[Byte],size:Long)
trait FilesOps[F[_]]{
  //
//  def saveIn():F[Unit]
  //   Create N 1MB size files
  def replicateFilesN(sourcePath:String, targetPath:String, n:Int):F[Unit]
  //  Convert all the files in the directory into a Stream of Array[Byte]
  def directoryToBytes(blocker: Blocker,path:String):Stream[F,Array[Byte]]
  def directoryToBytesAndFilename(blocker: Blocker,path:String)(implicit cs:ContextShift[F]):Stream[F,FileInfo]
  // Apply a hash function on a file
  def digest(path:String, algorithm: MessageDigestAlgorithms): F[Unit]
  //  Apply a hash function to all files in a directory
  def transformFiles[A](path:String, p:Pipe[IO,Array[Byte],A]):F[Unit]
  //  Remove all the files in a directory
  def cleanDirectory(path:String):F[Unit]
}

object FilesOpsInterpreter {
//
  implicit def unsafeLogger = Slf4jLogger.getLogger[IO]

  implicit def filesOpsIO: FilesOps[IO] = new FilesOps[IO] {
    override def replicateFilesN(sourcePath: String, targetPath: String, n: Int): IO[Unit] = {
      val U = implicitly[Utils[IO]]
      implicit val L: Logger[IO] = implicitly[Logger[IO]]
      implicit val timer: Timer[IO] = IO.timer(global)
      implicit val ctxShift: ContextShift[IO] = IO.contextShift(global)
      Blocker[IO].use { blocker =>
        Stream.iterate(1)(_ + 1)
          .evalMap(U.defaultPad)
          .evalMap(filename =>
            copy[IO](blocker, Paths.get(sourcePath), Paths.get(s"$targetPath/$filename.txt"))
          )
          .map(_.toUri.toASCIIString)
          .evalTap(x => L.info(x))
          .take(n)
          .metered(100 millis)
          .compile
          .drain

      }
    }

    override def digest(path: String, algorithm: MessageDigestAlgorithms): IO[Unit] = {
      val HF: HashFunctions[IO] = implicitly[HashFunctions[IO]]
      val L = implicitly[Logger[IO]]
      implicit val ctxShift: ContextShift[IO] = IO.contextShift(global)

      Blocker[IO].use { blocker =>
        directoryToBytes(blocker,path)
          .through(HF.digestFile(algorithm))
          .evalTap(x=>L.info(s"${algorithm.toString} File Checksum: $x"))
          .compile
          .drain
      }
    }

    override def cleanDirectory(path: String): IO[Unit] = {
      implicit val contextShift: ContextShift[IO] = IO.contextShift(global)
      val L = implicitly[Logger[IO]]
      Blocker[IO].use { blocker =>
        directoryStream[IO](blocker, Paths.get(path))
          .evalMap(y => deleteIfExists[IO](blocker, y).map(x => (y.toUri.toASCIIString, x)))
          .evalTap(x => L.info(s"${x._1} was deleted"))
          .compile
          .drain
      }
    }

    override def directoryToBytes(blocker: Blocker,path:String): Stream[IO, Array[Byte]] = {
      implicit val contextShift: ContextShift[IO] = IO.contextShift(global)
      directoryStream[IO](blocker,Paths.get(path))
        .map(path=>Files.readAllBytes(path))
    }

    override def transformFiles[A](path: String, p: Pipe[IO, Array[Byte], A]): IO[Unit] ={
      Blocker[IO].use {blocker=>
        directoryToBytes(blocker,path)
          .through(p)
          .compile
          .drain
      }
    }

//    override def saveIn(): IO[Unit] = Blocker[IO].use{ blocker =>

//    }
    override def directoryToBytesAndFilename(blocker: Blocker, path: String)(implicit cs:ContextShift[IO]):Stream[IO,
      FileInfo]={
        directoryStream[IO](blocker,Paths.get(path))
          .map{ path=>
            val bytes = Files.readAllBytes(path)
            FileInfo(path.getFileName.toString,bytes,bytes.size)
          }
    }
  }
}
