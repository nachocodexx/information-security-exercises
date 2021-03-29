/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/26/21, 1:00 PM
 ******************************************************************************/

package cinvestav

import cats.effect.std.Console
import cats.implicits._
import fs2.Stream
import fs2.io.file.Files
import fs2.text
import cats.effect.{ExitCode, IO, IOApp}
import cinvestav.crypto.cipher.CipherX.{CipherText, CipherX, Transformation}
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.keyagreement.KeyAgreementX
import cinvestav.crypto.keyagreement.KeyAgreementXDSL._
import cinvestav.crypto.keyagreement.KeyAgreementXDSL._
import cinvestav.crypto.keystore.KeyStoreX.KeyStoreX
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cinvestav.crypto.providers.ProviderX
import cinvestav.crypto.signatures.{SignatureResult, SignatureX}
import cinvestav.crypto.signatures.SignatureXDSL._
import cinvestav.crypto.signatures.enums.SignatureAlgorithmsX
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.utils.files.FilesOps
import cinvestav.utils.stopwatch.StopWatch
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.{KeyPair, Security}
import cinvestav.utils.stopwatch.StopWatchDSL._

import java.nio.file.{Paths, StandardOpenOption}
import scala.concurrent.duration._
//import scala.concurrent.duration.MILLISECONDS.
import scala.language.postfixOps
import javax.crypto.SecretKey
import scala.concurrent.duration.FiniteDuration
object Lab02App extends IOApp{
  sealed trait Person{
    def name:String
    def keypair:KeyPair
  }
  case class Alice(name:String="Alice",keypair: KeyPair) extends Person
  case class Bob(name:String="Bob",keypair: KeyPair) extends Person
  case class Experiment(algorithm:String,aliceCert:String,bobCert:String,cipher:String,signature:SignatureAlgorithmsX.SignatureAlgorithmX)
//  case class KeyRing()
  private val TARGET_PATH             = "/home/nacho/Programming/Scala/cinvestav-IS-00/target"
  private val RESULT_PATH             = s"$TARGET_PATH/results"
  private val DATA_PATH               = "/home/nacho/Documents/test/data"
  private val KEYSTORE_NAME           = "default.bks"
  private val KEYSTORE_PATH           = s"$TARGET_PATH/keystore/$KEYSTORE_NAME"
  private val KEYSTORE_PASSWORD       = "changeit"
  private val ALICE_KEYSTORE_PASSWORD = "changeit"
  private val BOB_KEYSTORE_PASSWORD   = "changeit"
  private val transformation = Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  private val e0                       = Experiment("ECCDH","alice384","bob384","AES[256]",SignatureAlgorithmsX.SHA512WithECDSA)
//  private val e0                       = Experiment("ECCDH","alice256","bob256","AES[192]",SignatureAlgorithmsX.SHA256WithECDSA)
//    private val e0                       = Experiment("ECCDH","alice224","bob224","AES[128]",SignatureAlgorithmsX.SHA224WithECDSA)

  type KeyRing = (Alice,Bob)
  case class SendFileResponse(ct:CipherText,ds:SignatureResult)

  def initBC():IO[Unit] = IO(
    Security.addProvider(new BouncyCastleProvider())
  )
  def getKeyRing()(implicit KS:KeyStoreX[IO]):IO[(Alice,Bob)] = for {
    ks           <- KS.load(KEYSTORE_PATH,KEYSTORE_PASSWORD)
    aliceKeypair <- KS.getKeyPair(ks,e0.aliceCert,ALICE_KEYSTORE_PASSWORD)
    bobKeyPair   <- KS.getKeyPair(ks,e0.bobCert,BOB_KEYSTORE_PASSWORD)
    alice        <- Alice(keypair = aliceKeypair).pure[IO]
    bob          <- Bob(keypair = bobKeyPair).pure[IO]
  } yield (alice,bob)

  def sendFile(data:Array[Byte],keyRing:KeyRing,sharedSecret:SecretKey)(implicit C:CipherX[IO],KA:KeyAgreementX[IO],S:SignatureX[IO],
                                                 SW:StopWatch[IO])
  :IO[(SendFileResponse,(Long,Long,Long))]
  = keyRing match {
    case (alice,bob)=>
      for {
//        time
        aliceSk           <- alice.keypair.getPrivate.pure[IO]
        bobPk             <- bob.keypair.getPublic.pure[IO]
//        (sharedSecret,t0) <- SW.measure[SecretKey]( KA.sharedSecret(e0.algorithm,e0.cipher,aliceSk,bobPk),MILLISECONDS)
//        time
        (ct,t1)           <- SW.measure[CipherText](C.encrypt(data,transformation,sharedSecret),MILLISECONDS)
//        time
        (ds,t2)           <- SW.measure[SignatureResult](S.sign(data,aliceSk,e0.signature),MILLISECONDS)
      } yield (SendFileResponse(ct,ds),(1,t1.toMillis,t2.toMillis))
  }
  def receiveFile(sfr:SendFileResponse,keyRing: KeyRing,sharedSecret:SecretKey)(implicit C:CipherX[IO],S:SignatureX[IO],
                                                         KA:KeyAgreementX[IO],SW:StopWatch[IO])
  :IO[(Long,Long,Long)]=
    keyRing match {
      case (alice,bob)=>
        for {
          bobSk        <- bob.keypair.getPrivate.pure[IO]
          alicePk      <- alice.keypair.getPublic.pure[IO]
//          time
//          (sharedSecret,t0) <- SW.measure(KA.sharedSecret(e0.algorithm,e0.cipher,bobSk,alicePk),MILLISECONDS)
//          time
          (pt,t1)           <- SW.measure(C.decrypt(sfr.ct,transformation,sharedSecret).map(_.bytes),MILLISECONDS)
//          time
          (valid,t2)        <- SW.measure(S.verify(pt,sfr.ds,alicePk),MILLISECONDS)
//          _            <- Console[IO].println(valid)
        } yield (1,t1.toMillis,t2.toMillis)
    }


  def program()(implicit signatureXIO:SignatureX[IO], cipherXIO:CipherX[IO], keyAgreementXIO:KeyAgreementX[IO],
                keyStoreXIO:KeyStoreX[IO],
                FOps:FilesOps[IO],stopWatch:StopWatch[IO])
   =
    Stream.eval(initBC())++
      Stream.eval(getKeyRing).flatMap { kr =>
        val sharedSecret = keyAgreementXIO.sharedSecret(e0.algorithm,e0.cipher,kr._1.keypair.getPrivate,kr
          ._2.keypair.getPublic)
        val sharedSecret2 = keyAgreementXIO.sharedSecret(e0.algorithm,e0.cipher,kr._2.keypair.getPrivate,kr
          ._1.keypair.getPublic)
        val sharedSecrets = sharedSecret.product(sharedSecret2)
//        Stream.
        Stream.eval(sharedSecrets).flatMap{
          case (shared1,shared2)=>
            FOps.directoryToBytes(DATA_PATH)
//              .evalMap { case (data, filename) =>
              .parEvalMap(10){ case (data, filename) =>
                for {
                  (result, d0) <- sendFile(data, kr,shared1)
                  d1           <- receiveFile(result, kr,shared2)
                  _            <- Console[IO].println(s"$filename done!")
                } yield (filename, data.length, d0._1, d0._2, d0._3, d1._1, d1._2, d1._3, e0.algorithm, e0.cipher).toString().replace("(", "").replace(")", "")
              }
        }
          .map(x => s"\n$x")
          //      .intersperse("\n")
          .through(text.utf8Encode)
          .through(Files[IO].writeAll(Paths.get(s"$RESULT_PATH/data.csv"), List(StandardOpenOption.APPEND)))
          .debug()
      }

  override def run(args: List[String]): IO[ExitCode] =
    program()
      .compile
      .drain
      .as(ExitCode.Success)
}
