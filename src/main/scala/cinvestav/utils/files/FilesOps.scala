package cinvestav.utils.files

import cats.effect.IO
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms.MessageDigestAlgorithms
import fs2.io.file.Files
import cinvestav.utils.Utils
import fs2.{Chunk, Pull}
import fs2.io.file.{copy, deleteIfExists, directoryStream, writeAll}

//import java.nio.file.Paths
import java.nio.file.{Files => FS, Paths}
import cinvestav.crypto.hashfunction.HashFunctions
import cinvestav.crypto.hashfunction.HashFunctionsInterpreter.hashFunctionAlgorithmIO
import cinvestav.utils.UtilsInterpreter.utilsIO
import fs2.{Pipe, Stream}
import fs2.io.file.Files
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
//
case class FileInfo(name:String,bytes:Array[Byte],size:Long)
trait FilesOps[F[_]]{
  //
//  def saveIn():F[Unit]
  //   Create N 1MB size files
//  def replicateFilesN(sourcePath:String, targetPath:String, n:Int):F[Unit]
  //  Convert all the files in the directory into a Stream of Array[Byte]
  def directoryToBytes(path:String):Stream[F,(Array[Byte],String)]
  def directoryToBytes_(path:String):Stream[F,Byte]

  def directoryToBytesAndFilename(path:String):Stream[F,FileInfo]
  // Apply a hash function on a file
  def digest(path:String, algorithm: MessageDigestAlgorithms): F[Unit]
  //  Apply a hash function to all files in a directory
  def transformFiles[A](path:String, p:Pipe[IO,Array[Byte],A]):F[Unit]
  //  Remove all the files in a directory
  def cleanDirectory(path:String):F[Unit]
}

object FilesOpsInterpreter {
//

  implicit def filesOpsIO: FilesOps[IO] = new FilesOps[IO] {
    override def directoryToBytes(path: String): Stream[IO,(Array[Byte],String)] =
      Files[IO].directoryStream(Paths.get(path))
        .map(x=>(FS.readAllBytes(x),x.getFileName.toString))

    override def directoryToBytesAndFilename(path: String): Stream[IO, FileInfo] = ???

    override def digest(path: String, algorithm: MessageDigestAlgorithms): IO[Unit] = ???

    override def transformFiles[A](path: String, p: Pipe[IO, Array[Byte], A]): IO[Unit] = ???

    override def cleanDirectory(path: String): IO[Unit] = ???

    override def directoryToBytes_(path: String): Stream[IO, Byte] = Files[IO]
      .directoryStream(Paths.get(path))
      .flatMap(Files[IO].readAll(_,4096))
  }
}
