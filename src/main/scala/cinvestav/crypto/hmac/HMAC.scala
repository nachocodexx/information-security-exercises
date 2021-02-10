package cinvestav.crypto.hmac

import cats.implicits._
import cats.effect.implicits._
import cats.effect.IO

import java.security.{Key, SecureRandom}
import javax.crypto.{KeyGenerator, Mac}
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms._
import cinvestav.crypto.hmac.HMACAlgorithms.HMACAlgorithms
import fs2.{Pipe, Stream}
//import cinvestav.utils.formatters.FormattersDSL._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cinvestav.logger.LoggerX
import cinvestav.logger.LoggerXDSL._
import cinvestav.utils.formatters.Formatter

trait HMAC[F[_]]{
  def digest[A](message:Array[Byte],HMACAlgorithm: HMACAlgorithms,key:Key )(implicit FR:Formatter[F,A]):F[A]
//  def digest[A](message:Array[Byte],HMACAlgorithm: HMACAlgorithms,key:Key,outputConvert:F[Array[Byte]=>A]  ):F[A]
  def digestFile[A](HMACAlgorithm: HMACAlgorithms,key:Key)(implicit FR:Formatter[F,A]):Pipe[F,Array[Byte],A]
}

object HMACInterpreter {
  implicit def unsafeLogger = Slf4jLogger.getLogger[IO]


  implicit val HMACIO: HMAC[IO] = new HMAC[IO] {
    override def digest[A](message: Array[Byte],HMACAlgorithm: HMACAlgorithms,key: Key)(implicit FR:Formatter[IO, A])
    : IO[A]
    = {
      implicit val L = implicitly[LoggerX[IO]]
      for {
        mac          <- Mac.getInstance(HMACAlgorithm.toString).pure[IO]
        _            <- mac.init(key).pure[IO]
        bytes        <- mac.doFinal(message).pure[IO]
        result       <- FR.format ap bytes.pure[IO]
//        _            <- L.info(result.toString)
      } yield  result
    }


    override def digestFile[A](algorithm: HMACAlgorithms, key: Key)(implicit FR:Formatter[IO,A]): Pipe[IO, Array[Byte], A] = {
      _.evalMap(digest(_,algorithm,key))
    }
  }

}
