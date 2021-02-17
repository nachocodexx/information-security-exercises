/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/16/21, 7:22 PM
 ******************************************************************************/

package cinvestav

import cats.Show
import cats.implicits._
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cinvestav.crypto.cipher.CipherX.Transformation
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import fs2.{Chunk, Stream, io}
import fs2.io.{file => FI}
import fs2.text

import java.io.{File, FileInputStream}
import java.nio.file.Paths
import javax.crypto.{Cipher, CipherInputStream}
import cinvestav.crypto.pbkdf2.PBKDF2XDSL._
import cinvestav.crypto.pbkdf2.{PBKDF2X, SecretKeyAndSalt}
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms

import scala.concurrent.duration._
import scala.language.postfixOps
import cinvestav.utils.UtilsInterpreter._

import java.security.AlgorithmParameters

class CipherFileName(val hash:String,val cipher:String,val iterations:Int,val salt:String,val iv:String)
object CipherFileName {
  def apply(hash:String,cipher:String,iterations:Int,salt:String,iv:String) =
    s"$hash:$cipher:$iterations:$salt:$iv"
  def unapply(x:String):Option[(String,String,Int,String,String)] = {
    val xs = x.split(':')
    Some((xs(0),xs(1),xs(2).toInt,xs(3),xs(4) ))
  }
}
object Testing extends IOApp{

  val desECB  = Transformation(CipherXAlgorithms.DES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val desCBC  = Transformation(CipherXAlgorithms.DES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val des3ECB = Transformation(CipherXAlgorithms.DES3,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3CBC = Transformation(CipherXAlgorithms.DES3,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val aesECB  = Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val aesCBC  = Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val cipher                        =  Cipher.getInstance(desCBC.show)
  final private val OUTPUT_PATH     = "/home/nacho/Documents/test/encrypted"
  final private val CSV_OUTPUT_PATH = "/home/nacho/Documents/test/test"
  val password                      = "topsecret"
//  implicit val cipherFileNameShow: Show[CipherFileName] =
  //    Show[CipherFileName](x=>s"")


  def encrypt()(implicit PBEKDF:PBKDF2X[IO]) ={ Blocker[IO].use { blocker =>
      FI.directoryStream[IO](blocker, Paths.get("/home/nacho/Documents/test/test"))
        .evalMap{ path =>
          val filename                      = path.getFileName.toString
          val startTime                     = System.currentTimeMillis()
          val file                          = new File(path.toUri)
          val fileIs                        = new FileInputStream(file)
          val cipherAlgorithm               = CipherXAlgorithms.DES
          val secretKeyAlgorithm            = "SHA1"
          val iterations                    = 1000
          val keyLen                        = 64
          val result    = for {
            salt          <- PBEKDF.generateSalt(8)
            secretKeyAlgo <- PBKDF2Algorithms.fromString(secretKeyAlgorithm).pure[IO]
            secret        <- PBEKDF.generatePasswordWithSalt(salt,cipherAlgorithm,secretKeyAlgo, password, 8, iterations,keyLen)
          } yield (secret,salt)

          result.map{
            case (secret, salt) =>
              val params                         = Option(cipher.getParameters)
              val saltHex                        = utilsIO.toHex(salt)
              val css                            = IO(new CipherInputStream(fileIs,cipher))
              val encryptedFileName = CipherFileName(secretKeyAlgorithm,cipherAlgorithm.toString+s"_$keyLen", iterations,_,saltHex)
              params match {
                case Some(params) =>
                  val ivHex =utilsIO.toHex(params.getEncoded)
                  cipher.init(Cipher.ENCRYPT_MODE,secret,params)
                  io.readInputStream[IO](css,4096,blocker,closeAfterUse = true)
                    .through(FI.writeAll[IO](Paths.get(s"$OUTPUT_PATH/${encryptedFileName(ivHex).show}"), blocker))
                    .map(x=>"")
                    .onComplete[IO,String](Stream(s"$filename,${file.length},${System.currentTimeMillis()-startTime}"))
                case None =>
                  cipher.init(Cipher.ENCRYPT_MODE,secret,null.asInstanceOf[AlgorithmParameters])
                  io
                    .readInputStream[IO](css,4096,blocker,closeAfterUse = true)
                    .through(FI.writeAll[IO](Paths.get(s"$OUTPUT_PATH/${encryptedFileName("IV").show}"), blocker))
                    .map(x=>"")
                    .onComplete[IO,String](Stream(s"$filename,${file.length},${System.currentTimeMillis()-startTime}"))
              }
          }

//            .unsafeRunSync()
        }
        .flatMap(x=>x)
        .intersperse("\n")
        .through(text.utf8Encode)
        .through(FI.writeAll[IO](Paths.get(s"$CSV_OUTPUT_PATH/data.csv"),blocker))
        .debug()
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }


  def decrypt(): IO[ExitCode] = Blocker[IO].use{ blocker=>
    FI.directoryStream[IO](blocker,Paths.get(OUTPUT_PATH))
      .map{ path=>
        path.getFileName.toString match {
          case CipherFileName(hash,cipher, iterations, salt, iv) =>
            iv match {
              case "IV"=>
                println("ECB")
              case x =>
                println("CBC")
            }
        }
      }
      .compile
      .drain
      .as(ExitCode.Success)
  }

  override def run(args: List[String]): IO[ExitCode] = decrypt()
}
