package cinvestav

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Sync}
import cinvestav.config.DefaultConfig
import cinvestav.crypto.hmac.{HMAC, HMACAlgorithms}
import cinvestav.crypto.hmac.HMACAlgorithms.HMACAlgorithms
import cinvestav.crypto.hmac.HMACInterpreter._
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms._
import cinvestav.crypto.keygen.KeyGeneratorX.KeyGeneratorX
import cinvestav.utils.Utils
import cinvestav.utils.UtilsInterpreter._
import cinvestav.utils.files.FilesOps
import pureconfig.generic.auto._
import pureconfig.ConfigSource
import cinvestav.utils.formatters.FormattersDSL._
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.crypto.keygen.KeyGeneratorXDSL._


object HMACApp {
  def program(keyGeneratorAlgorithm: KeyGeneratorAlgorithms,hMACAlgorithm:HMACAlgorithms)(implicit FO:FilesOps[IO],
                                                                                           H:HMAC[IO], U:Utils[IO],
                                                                                          KG:KeyGeneratorX[IO])
  :IO[ExitCode] = {
    val app = ConfigSource.default.load[DefaultConfig]
      .flatMap{ config =>
//         H.generateKey(keyGeneratorAlgorithm).flatMap{ key =>
        KG.generateRandomKey(keyGeneratorAlgorithm).flatMap{ key=>
//           val f = (U.bytesToHexString _).pure[IO]
           FO.transformFiles(config.dirPath,H.digestFile(hMACAlgorithm,key))
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
//  override def run(args: List[String]): IO[ExitCode] =
//    program(HmacSHA1,HMACAlgorithms.HmacSHA1)
}
