/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/16/21, 7:22 PM
 ******************************************************************************/

package cinvestav

import cats.Monoid
import cats.implicits._
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cinvestav.config.DefaultConfig
import cinvestav.crypto.cipher.CipherX.Transformation
import cinvestav.crypto.cipher.enums.CipherXAlgorithms
import fs2.{Stream, io}
import fs2.io.{file => FI}
import fs2.text

import java.io.{File, FileInputStream}
import java.nio.file.{Paths, StandardOpenOption}
import javax.crypto.{Cipher, CipherInputStream}
import cinvestav.crypto.pbkdf2.PBKDF2XDSL._
import cinvestav.crypto.pbkdf2.PBKDF2X
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms
import cinvestav.lab.Lab1.{Lab1Test, aesCBC192_SHA256, aesCBC_SHA1, aesCBC_SHA256, aesCBC_SHA384, aesCBC_SHA512, des3CBC_SHA256, des3CBC_SHA384, des3CBC_SHA512, desCBC, desCBC_SHA1, desCBC_SHA256, desCBC_SHA384, desCBC_SHA512, desECB_SHA1, desECB_SHA256}
import cinvestav.utils.Utils

import scala.concurrent.duration._
import scala.language.postfixOps
import cinvestav.utils.UtilsInterpreter._

import java.security.AlgorithmParameters
import javax.crypto.spec.IvParameterSpec
import pureconfig._
import pureconfig.generic.auto._

class CipherFileName(val filename:String,val hash:String,val cipher:String,val iterations:Int,val salt:String,val
iv:String)
object CipherFileName {
  def apply(filename:String,hash:String,cipher:String,iterations:Int,salt:String,iv:String) =
    s"$filename:$hash:$cipher:$iterations:$salt:$iv"
  def unapply(x:String):Option[(String,String,String,Int,String,String)] = {
    val xs = x.split(':')
    Some((xs(0),xs(1),xs(2),xs(3).toInt,xs(4),xs(5)))
  }
}
object Testing extends IOApp{
  case class PBEKDF2Test(cipherAlgorithms: CipherXAlgorithms.CipherXAlgorithm,hashAlgorithm:String,keyLen:Int,iterations:Int,
                         transformation: Transformation)


  def encrypt(labTest: Lab1Test)(implicit PBEKDF:PBKDF2X[IO],U:Utils[IO],C:DefaultConfig): IO[ExitCode] ={ Blocker[IO].use {
    blocker =>
      val cipher                        =  Cipher.getInstance(labTest.transformation.show)

      FI.directoryStream[IO](blocker, Paths.get(C.dataPath))

        .evalMap{ path =>
          val filename                      = path.getFileName.toString
          val filenameHex                   = U.toHex(filename.getBytes)
          val startTime                     = System.currentTimeMillis()
          val file                          = new File(path.toUri)
          val fileIs                        = new FileInputStream(file)

          val cipherAlgorithm               = labTest.cipherAlgorithms
          val secretKeyAlgorithm            = labTest.hashAlgorithm
          val iterations                    = labTest.iterations
          val keyLen                        = labTest.keyLen

          val result    = for {
            salt          <- PBEKDF.generateSalt(8)
            secretKeyAlgo <- secretKeyAlgorithm.pure[IO]
            secret        <- PBEKDF.generatePasswordWithSalt(salt,cipherAlgorithm,secretKeyAlgo, C.password,
              iterations,keyLen)
          } yield (secret,salt)

          result.map{
            case (secret, salt) =>
              val params                         = Option(cipher.getParameters)
              val saltHex                        = utilsIO.toHex(salt)
              val css                            = IO(new CipherInputStream(fileIs,cipher))
              val encryptedFileName = CipherFileName(filenameHex,PBKDF2Algorithms.getHashAlgorithm(secretKeyAlgorithm),
                cipherAlgorithm.toString+s"_$keyLen",
                iterations,saltHex,_)

              val writeEncryptedFiles  = (x:String)=>  io.readInputStream[IO](css,4096,blocker,closeAfterUse = true)
                  .through(FI.writeAll[IO](Paths.get(s"${C.encryptPath}/${encryptedFileName(x)}"), blocker))
                  .map(_=>Monoid[String].empty)
                  .onComplete[IO,String](
                    Stream(s"$filename,${file.length},${System.currentTimeMillis()-startTime},${labTest
                      .hashAlgorithm.toString},${labTest.cipherAlgorithms.toString},${labTest.transformation.show}," +
                      s"${labTest.keyLen}")
                  )

              params match {
                case Some(params) =>
                  val ivHex =utilsIO.toHex(params.getEncoded)
                  cipher.init(Cipher.ENCRYPT_MODE,secret,params)
                  writeEncryptedFiles(ivHex)
                case None =>
                  cipher.init(Cipher.ENCRYPT_MODE,secret,null.asInstanceOf[AlgorithmParameters])
                  writeEncryptedFiles("IV")
              }
          }

        }
        .flatMap(identity)
        .evalTap(x=>println(s"[${Thread.currentThread().getName}] - ${x.split(',')(0)} completed").pure[IO])
        .intersperse("\n")
        .through(text.utf8Encode)
        .through(FI.writeAll[IO](Paths.get(s"${C.csvPath}/data.csv"),blocker))
        .debug()
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }


  def decrypt(labTest: Lab1Test)(implicit U:Utils[IO],P:PBKDF2X[IO],C:DefaultConfig): IO[ExitCode] =
    Blocker[IO].use {
    blocker=>
    val cipher =  Cipher.getInstance(labTest.transformation.show)
    FI.directoryStream[IO](blocker,Paths.get(C.encryptPath))
      .evalMap{ path=>
        path.getFileName.toString match {
          case filename @ CipherFileName(filenameHex,_,_, iterations, salt, iv) =>
            val originalFilename = new String(U.fromHex(filenameHex),"UTF8")
            val saltBytes        = U.fromHex(salt)
            val startTime        = System.currentTimeMillis()
            val secretKeyIO      = P.generatePasswordWithSalt(saltBytes,labTest.cipherAlgorithms,labTest.hashAlgorithm,
              C.password, iterations,labTest.keyLen)

            secretKeyIO.map{ secret=>
              iv match {
                case "IV"  =>
                  val pams= AlgorithmParameters.getInstance(labTest.cipherAlgorithms.toString)
                  cipher.init(Cipher.DECRYPT_MODE,secret,pams)
                case value =>
                  val iiv = U.fromHex(iv)
//                  println(value)
                  val pams= AlgorithmParameters.getInstance(labTest.cipherAlgorithms.toString)
                  pams.init(iiv)
                  cipher.init(Cipher.DECRYPT_MODE,secret,pams)
              }
              val file      = new File(filename)
              val encryptedFilename = s"${C.encryptPath}/$filename"
              val ins    = new FileInputStream(new File(encryptedFilename))
              val cIn       = IO(new CipherInputStream(ins,cipher))

              io.readInputStream[IO](cIn,4096,blocker,closeAfterUse = true)
                .map(x=>new String(Array(x),"UTF8"))
                .through(text.utf8Encode)
                .through(FI.writeAll[IO](Paths.get(s"${C.decryptPath}/$originalFilename"),blocker))
                .map(_=>Monoid[String].empty)
                .onComplete[IO,String](
                  Stream(s"$filename,$originalFilename,${file.length},${System.currentTimeMillis()-startTime}," +
                    s"${labTest.hashAlgorithm},${labTest.cipherAlgorithms},${labTest.transformation.show},${labTest
                      .keyLen}")
                )
            }

        }
      }
      .flatMap(identity)
      .evalTap(x=>println(s"[${Thread.currentThread().getName}] - ${x.split(',')(0)} completed").pure[IO])
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(FI.writeAll[IO](Paths.get(s"${C.csvPath}/data_decrypted.csv"),blocker))
      .compile
      .drain
      .as(ExitCode.Success)
  }

  override def run(args: List[String]): IO[ExitCode] =
    IO(
      ConfigSource.default.load[DefaultConfig]
    ).flatMap {
      case Left(error) =>
        println(error.head.description)
        IO.unit.as(ExitCode.Error)
      case Right(config) =>
        implicit val c = config
        println(config)
        val t = Lab1Test.fromString(config.cipher, config.hashFunction, config.keyLength, config
          .iterations, config.operationMode)
        if(config.cipherMode == 0) encrypt(t) else decrypt(t)
    }
}
