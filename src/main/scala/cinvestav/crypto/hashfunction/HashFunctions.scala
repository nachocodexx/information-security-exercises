package cinvestav.crypto.hashfunction

import cats.effect.IO
import cats.implicits._
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms.MessageDigestAlgorithms
import cinvestav.crypto.providers.ProviderX
import cinvestav.crypto.providers.ProviderX.ProviderX
import cinvestav.logger.LoggerX
import cinvestav.utils.Utils
import cinvestav.utils.UtilsInterpreter._
import fs2.Pipe
import cinvestav.logger.LoggerXDSL._

import java.security.MessageDigest

trait HashFunctions[F[_]]{
  def selectHashFunction(algorithm:MessageDigestAlgorithms,provider:Option[ProviderX]= Some(ProviderX.BouncyCastle))
  :F[MessageDigest]
  def digest(bytes:Array[Byte],algorithm: MessageDigestAlgorithms):F[String]
  def digestFile:MessageDigestAlgorithms=>Pipe[F,Array[Byte],String]
}

object HashFunctionsInterpreter {

  implicit val hashFunctionAlgorithmIO: HashFunctions[IO] = new HashFunctions[IO] {
    override def selectHashFunction(algorithm: MessageDigestAlgorithms,provider:Option[ProviderX]): IO[MessageDigest] =
      MessageDigest.getInstance(algorithm.toString,provider.getOrElse("SunJCE").toString).pure[IO]

    override def digest(bytes: Array[Byte],algorithm: MessageDigestAlgorithms): IO[String] = {
      val U = implicitly[Utils[IO]]
      val L = implicitly[LoggerX[IO]]
      for {
        messageDigest <-  selectHashFunction(algorithm)
        _<- messageDigest.update(bytes).pure[IO]
        digestBytes <- messageDigest.digest().pure[IO]
//        hex <- digestBytes.map(x=>Integer.toHexString(0xFF&x)).fold("")(_+_).pure[IO]
        hex <- IO(U.toHex _) ap digestBytes.pure[IO]
//        _<- L.info(hex)
      } yield hex
    }

    override def digestFile:MessageDigestAlgorithms=>Pipe[IO, Array[Byte], String] =
      algorithm=>_.evalMap(digest(_, algorithm))
  }
}
