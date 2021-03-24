/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 10:39 PM
 ******************************************************************************/

package cinvestav

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import cinvestav.config.DefaultConfig
import cinvestav.crypto.cipher.CipherX.{CipherX, Transformation}
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cinvestav.crypto.keystore.KeyStoreX.KeyStoreX
import cinvestav.logger.LoggerXDSL._
import cinvestav.utils.Utils
import cinvestav.utils.UtilsInterpreter._
import cinvestav.utils.files.FilesOps
import cinvestav.utils.files.FilesOpsInterpreter._
import pureconfig._
import pureconfig.generic.auto._


object CipherXApp {

  val desCBC = Transformation(CipherXAlgorithms.DES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val desECB = Transformation(CipherXAlgorithms.DES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3ECB = Transformation(CipherXAlgorithms.DES3,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3CBC = Transformation(CipherXAlgorithms.DES3,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val aesECB = Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val aesCBC= Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)

//  def program(alias:String,transformation: Transformation)(implicit FO:FilesOps[IO],C:CipherX[IO] ,KS:KeyStoreX[IO],
//                                                           U:Utils[IO])=
//  ConfigSource.default.load[DefaultConfig].flatMap {config=>
//    KS.getSecretKeyFromDefaultKeyStore(alias)(contextShift,timer).value
//      .flatMap {
//        case Some(value) =>
//           val pipe = C.encryptFile(value,transformation)
//           FO.transformFiles(config.dirPath, pipe)
//        case None => IO.unit
//      }.asRight
//  }
//  override def run(args: List[String]): IO[ExitCode] =
//  program("aeskey", aesECB) match {
//    case Left(value) =>
//      println(value)
//      IO.unit.as(ExitCode.Error)
//    case Right(value) =>
//      println("RIGHT")
//      value.as(ExitCode.Success)
//  }
//        keyStoreXIO.createKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default")).as(ExitCode.Success)
}
