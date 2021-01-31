package cinvestav.crypto.hashfunction

import cats.effect.IO
import cats.implicits._
import HashFunctionAlgorithm.HashFunctionAlgorithm
import cinvestav.utils.Utils
import cinvestav.utils.UtilsInterpreter._
import fs2.Pipe
import io.chrisdavenport.log4cats.{Logger, SelfAwareStructuredLogger}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import java.security.MessageDigest

trait HashFunctions[F[_]]{
  def selectHashFunction(algorithm:HashFunctionAlgorithm):F[MessageDigest]
  def digest(bytes:Array[Byte],algorithm: HashFunctionAlgorithm):F[String]
  def digestFile:HashFunctionAlgorithm=>Pipe[F,Array[Byte],String]
}

object HashFunctionsInterpreter {
  implicit def unsafeLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  implicit val hashFunctionAlgorithmIO: HashFunctions[IO] = new HashFunctions[IO] {
    override def selectHashFunction(algorithm: HashFunctionAlgorithm): IO[MessageDigest] =
      MessageDigest.getInstance(algorithm.toString).pure[IO]

    override def digest(bytes: Array[Byte],algorithm: HashFunctionAlgorithm): IO[String] = {
      val U = implicitly[Utils[IO]]
      val L = implicitly[Logger[IO]]
      for {
        messageDigest <-  selectHashFunction(algorithm)
        _<- messageDigest.update(bytes).pure[IO]
        digestBytes <- messageDigest.digest().pure[IO]
//        hex <- digestBytes.map(x=>Integer.toHexString(0xFF&x)).fold("")(_+_).pure[IO]
        hex <- IO(U.bytesToHexString _) ap digestBytes.pure[IO]
        _<- L.info(hex)
      } yield hex
    }

    override def digestFile:HashFunctionAlgorithm=>Pipe[IO, Array[Byte], String] =
      algorithm=>_.evalMap(digest(_, algorithm))
  }
}
