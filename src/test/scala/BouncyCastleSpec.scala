import cats.effect.{Concurrent, IO}
import cats.effect.unsafe.implicits.global
import cats.implicits._
import cats.effect.std.Console
import cinvestav.crypto.cipher.CipherX.Transformation
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms
import cinvestav.crypto.providers.ProviderX
import cinvestav.crypto.signatures.enums.SignatureAlgorithmsX
import cinvestav.crypto.cipher.CipherXDSL._
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.scalatest.funsuite.AnyFunSuite

import javax.crypto.SecretKey

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/22/21, 9:19 PM
 ******************************************************************************/
import cinvestav.crypto.pbkdf2.PBKDF2XDSL._
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.crypto.keygen.KeyGeneratorXDSL._
import cinvestav.crypto.keygen.enums._
import cinvestav.crypto.signatures.SignatureXDSL._
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cinvestav.crypto.keyagreement.KeyAgreementXDSL._
import java.security.Security
import cats.effect.std.Console
import cinvestav.crypto.signatures.SignatureXDSL._

class BouncyCastleSpec extends AnyFunSuite{
  private val t1 = Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  private val KEYSTORE_NAME = "default.bks"
  private val KEYSTORE_PATH = s"/home/nacho/Programming/Scala/cinvestav-IS-00/target/keystore/$KEYSTORE_NAME"
  def initBC():IO[Unit] = IO(
    Security.addProvider(new BouncyCastleProvider())
  )

  test("ECCDH: Eliptic Curve Co-factor Diffie Hellman"){
    val app = for {
      _       <- initBC()
      kStore  <- keyStoreXIO.load(KEYSTORE_PATH,"changeit",Some("BKS"),Some("BC"))
      aliceKeypair <- keyStoreXIO.getKeyPair(kStore,"alice160","changeit")
      bobKeypair <- keyStoreXIO.getKeyPair(kStore,"bob160","changeit")
      secret  <- keyAgreementXIO.sharedSecret("ECCDH","AES[256]",aliceKeypair.getPrivate,bobKeypair
        .getPublic)
      secret2 <- keyAgreementXIO.sharedSecret("ECCDH","AES[256]",bobKeypair.getPrivate,aliceKeypair.getPublic)
      message <- "MESSAGE".pure[IO].map(_.getBytes)
      ct      <- cipherXIO.encrypt(message,t1,secret,Some(ProviderX.BouncyCastle))
      pt      <- cipherXIO.decrypt(ct,t1,secret2,Some(ProviderX.BouncyCastle))
      _       <- Console[IO].println(new String(pt.bytes))
      ds      <- signatureXIO.sign(message,aliceKeypair.getPrivate,SignatureAlgorithmsX.SHA512WithECDSA)
      valid   <- signatureXIO.verify(message,ds,aliceKeypair.getPublic)
      _       <- Console[IO].println(ds)
      _       <- Console[IO].println(valid)
    } yield secret === secret2
    val result = app.unsafeRunSync()
    assert(result)
  }

  test("Cipher"){
    val app = for {
      _       <- initBC()
      message <- "text".pure[IO].map(_.getBytes)
      symKey  <- keyGeneratorXIO.generateKey(KeyGeneratorAlgorithms.AES).map(_.asInstanceOf[SecretKey])
      ct      <- cipherXIO.encrypt(message,t1,symKey,Some(ProviderX.BouncyCastle))
      pt      <- cipherXIO.decrypt(ct,t1,symKey,Some(ProviderX.BouncyCastle)).map(_.bytes).map(new String(_))
      _       <- Console[IO].println(ct)
      _       <- Console[IO].println(pt)
    } yield ()
    app.unsafeRunSync()
  }
  test("Key pair generator"){
    val app = initBC()>>keyGeneratorXIO
      .keyPairGenerator(SecurityLevelsX.ECC_384,KeyGeneratorAlgorithms.EC,Some(ProviderX.BouncyCastle))
      .flatTap{Console[IO].println}
    app.unsafeRunSync()
  }
  test("Digital Signature") {
    val app = for {
      _       <- initBC()
      message <- "Texto".getBytes().pure[IO]
      pair    <- keyGeneratorXIO.keyPairGenerator(SecurityLevelsX.DS_RSADSA_2048,KeyGeneratorAlgorithms.DSA)
      sign    <- signatureXIO.sign(message,pair.getPrivate,SignatureAlgorithmsX.SHA512WithDSA)
      _       <- Console[IO].println(sign)
      result  <- signatureXIO.verify(message,sign,pair.getPublic)
      _       <- Console[IO].println(result)
    } yield result
    val result = app.unsafeRunSync()
    assert(result)
  }

}
