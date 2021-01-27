package cinvestav

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{ExitCode, IO, IOApp, Sync}
import cinvestav.config.DefaultConfig
import cinvestav.crypto.hmac.{HMAC, HMACAlgorithms, KeyGeneratorAlgorithms}
import cinvestav.crypto.hmac.HMACAlgorithms.HMACAlgorithms
import cinvestav.crypto.hmac.HMACInterpreter._
import cinvestav.crypto.hmac.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms
import cinvestav.utils.Utils
import cinvestav.utils.UtilsInterpreter._
import cinvestav.utils.files.FilesOps
import pureconfig.generic.auto._
import pureconfig.ConfigSource
import cinvestav.utils.files.FilesOpsInterpreter._


object HMACApp extends IOApp{
  def program(keyGeneratorAlgorithm: KeyGeneratorAlgorithms,hMACAlgorithm:HMACAlgorithms)(implicit FO:FilesOps[IO],
                                                                                           H:HMAC[IO], U:Utils[IO])
  :IO[ExitCode] = {
    val app = ConfigSource.default.load[DefaultConfig]
      .flatMap{ config =>
         H.generateKey(keyGeneratorAlgorithm).flatMap{ key =>
           val f = (U.bytesToHexString _).pure[IO]
           FO.digestP(config.dirPath,H.digestFile(hMACAlgorithm,key,f))
         }.asRight

      }

    app match {
      case Left(value) =>
        println("ERROR",value)
        IO.unit.as(ExitCode.Error)
      case Right(value) =>
        value.as(ExitCode.Success)
    }

  }
  override def run(args: List[String]): IO[ExitCode] =
    program(KeyGeneratorAlgorithms.HmacSHA1,HMACAlgorithms.HmacSHA1)
}
