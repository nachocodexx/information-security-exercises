/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/15/21, 8:06 PM
 ******************************************************************************/

package cinvestav

import cats.implicits._
import cats.effect.{Blocker, Clock, ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import cinvestav.crypto.cipher.CipherX.{CipherX, Transformation}
import cinvestav.utils.files.FilesOps
import cinvestav.utils.files.FilesOpsInterpreter.filesOpsIO
import cinvestav.utils.stopwatch.StopWatch
import fs2.Stream
import fs2.io.file.writeAll
import fs2.text.utf8Encode

import scala.concurrent.duration._
import scala.language.postfixOps
import cinvestav.utils.stopwatch.StopWatchDSL._
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.pbkdf2.PBKDF2XDSL._
import cinvestav.crypto.pbkdf2.PBKDF2X
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms.{HMACSHA1, HMACSHA256, HMACSHA384, HMACSHA512}
import cinvestav.utils.UtilsInterpreter._
import cinvestav.utils.Utils

import java.nio.file.Paths
import javax.crypto.spec.SecretKeySpec

object PBKDF2App extends IOApp{

  val desECB = Transformation(CipherXAlgorithms.DES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val desCBC = Transformation(CipherXAlgorithms.DES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val des3ECB = Transformation(CipherXAlgorithms.DES3,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3CBC = Transformation(CipherXAlgorithms.DES3,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val aesECB = Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val aesCBC= Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
//  def task5Seconds[F[_]:Sync](): F[String] = timer.sleep(5 seconds) *> "SHAS".pure[F]
  def program[F[_]:Sync]()(implicit KDF:PBKDF2X[F],CX:CipherX[F],SW:StopWatch[F], FO:FilesOps[F],T:Timer[F],
                           C:ContextShift[F],U:Utils[F])
  :F[Unit]=
    Blocker[F]
    .use {
    blocker =>
    FO.directoryToBytesAndFilename(blocker,"/home/nacho/Documents/test/test")
      .evalMap { plainText =>
        val saltBytes  = 100
        val iterations = 100
        val keyLen     = 64
        KDF.generatePassword(CipherXAlgorithms.DES, HMACSHA1, "topsecret", saltBytes, iterations, keyLen)
          .map(x => (plainText, x))
      }
      .evalMap{
        case (fileInfo, secretKey) =>
          SW.measure(CX.encrypt(fileInfo.bytes,desECB,secretKey),MILLISECONDS)
              .map{
                case (text, l) => (text,l,fileInfo)
              }
      }
      .evalMap{
        case (cipherText, time,fileInfo) =>
          val hex            = U.toHex(cipherText.value)
          val iv             = cipherText.params.map(_.getEncoded).map(U.toHex).getOrElse("")
          val algorithms     = cipherText.algorithm.getOrElse("")
          val cipherTextSize = cipherText.size.getOrElse(0L).toString
          Sync[F].pure(s"${fileInfo.name},${fileInfo.size},$hex,$iv,$algorithms,$cipherTextSize,$time")
      }
//      .debug()
      .intersperse("\n")
      .through(utf8Encode)
      .through(writeAll(Paths.get( "/home/nacho/Programming/Python/cinvestav/src/DataAnalysis/LAB-01/des_cbc.csv"),blocker))
      .compile
      .drain
  }
//    FO.directoryToBytes()
  override def run(args: List[String]): IO[ExitCode] =
    program[IO]()
      .as(ExitCode.Success)

}
