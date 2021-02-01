package cinvestav.crypto.hmac

import cats.implicits._
import cats.effect.implicits._
import cats.effect.IO
import java.security.{Key, SecureRandom}
import javax.crypto.{KeyGenerator, Mac}
import cinvestav.crypto.hmac.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms
import cinvestav.crypto.hmac.HMACAlgorithms.HMACAlgorithms
import fs2.{Pipe,Stream}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cinvestav.logger.LoggerX
import cinvestav.logger.LoggerXInterpreter._

trait HMAC[F[_]]{
  def digest[A](message:Array[Byte],HMACAlgorithm: HMACAlgorithms,key:Key,outputConvert:F[Array[Byte]=>A]):F[A]
  def digestFile[A](HMACAlgorithm: HMACAlgorithms,key:Key,outputConvert:F[Array[Byte]=>A]):Pipe[F,Array[Byte],A]
  def generateKey(keyGeneatorAlgorithms: KeyGeneratorAlgorithms):F[Key]
}

object HMACInterpreter {
  implicit def unsafeLogger = Slf4jLogger.getLogger[IO]


  implicit val HMACIO: HMAC[IO] = new HMAC[IO] {
    override def digest[A](message: Array[Byte],HMACAlgorithm: HMACAlgorithms,key: Key,outputConvert:IO[Array[Byte]=>A])
    : IO[A]
    = {
      implicit val L = implicitly[LoggerX[IO]]
      for {
        mac <- Mac.getInstance(HMACAlgorithm.toString).pure[IO]
        _<- mac.init(key).pure[IO]
        bytes <- mac.doFinal(message).pure[IO]
        result<- outputConvert ap bytes.pure[IO]
        _<- L.info(result.toString)
      } yield  result
    }

    override def generateKey(keyGeneratorAlgorithms: KeyGeneratorAlgorithms): IO[Key] = for {
      keyAlgorithm <- keyGeneratorAlgorithms.toString.pure[IO]
      keygenerator <- KeyGenerator.getInstance(keyAlgorithm).pure[IO]
      secureRandom <- IO(new SecureRandom())
      _<-keygenerator.init(secureRandom).pure[IO]
      key <- keygenerator.generateKey().pure[IO]
    } yield key

    override def digestFile[A](algorithm: HMACAlgorithms, key: Key, outputConvert: IO[Array[Byte] => A])
    : Pipe[IO, Array[Byte], A] = {
      _.evalMap(digest(_,algorithm,key,outputConvert))
    }
  }

}
