/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/31/21, 12:05 PM
 ******************************************************************************/

package cinvestav.crypto.keystore

import cats.data.OptionT
import cats.effect.std.Console
import cats.implicits._
import cats.effect.{IO, Resource}
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms
import cinvestav.crypto.keystore.enums.KeyStoreXTypes.KeyStoreXTypes
import cinvestav.logger.LoggerX
import cinvestav.crypto.keygen.KeyGeneratorX.KeyGeneratorX
import com.sun.javafx.scene.traversal.Algorithm

import java.io.{FileInputStream, FileNotFoundException, FileOutputStream}
import java.security.{KeyPair, KeyStore, PrivateKey}
import java.util.concurrent.ScheduledExecutorService
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import scala.util.Try
import scala.concurrent.duration._
import scala.language.postfixOps
import cinvestav.crypto.keygen.KeyGeneratorXDSL._
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms
import cinvestav.crypto.keygen.enums.SecretKeyAlgorithms.SecretKeyAlgorithms
import cinvestav.crypto.keystore.enums.KeyStoreXTypes

import java.security.cert.X509Certificate

object KeyStoreX {
  trait KeyStoreX[F[_]]{
    def create(path:String, password:String,ksType:Option[String]=None,provider:Option[String]):F[KeyStore]
    def load(path:String,password:String,ksType:Option[String]=Some("BKS"),provider:Option[String]=Some("BC"))
    :F[KeyStore]
    def saveSymmetricKey(keystore:KeyStore,secretKey: SecretKey,alias:String,password:String):F[Boolean]
    def savePrivateKey(keyStore: KeyStore,privateKey:PrivateKey,cert:X509Certificate,alias:String,password:String):F[Boolean]
    def getCertificate(keyStore: KeyStore,alias:String):F[X509Certificate]
    def getPrivateKey(keyStore: KeyStore,alias:String,password:String):F[PrivateKey]
    def getKeyPair(keyStore: KeyStore,alias:String,password:String):F[KeyPair]

//    def createKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String]):F[Unit]
    def loadKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String]):OptionT[F, KeyStore]
    def saveSecretKey(ks:KeyStore,keyx:KeyX,password:String):F[Unit]
    def getSecretKey(ks:KeyStore,alias:String,pass:KeyStore.PasswordProtection):OptionT[F,KeyStore.Entry]
    def getSecretKeyFromDefaultKeyStore(alias:String):OptionT[F,KeyStore
    .Entry]
    def passwordFromString(password:String):F[(Array[Char],KeyStore.PasswordProtection)]
  }

  case class KeyX(value:String, alias:String, algorithm: KeyGeneratorAlgorithms)
}


object KeyStoreXDSL {
  import KeyStoreX._
  import cinvestav.logger.LoggerXDSL._
  def createFileOutput :(String)=>Resource[IO,FileOutputStream]= (filename)=>Resource.fromAutoCloseable(
    IO(new FileOutputStream(s"${System.getProperty("user.dir")}/target/keys/$filename.jcek"))
  )

  def createFileInput:String=>Resource[IO,FileInputStream] = filename=>Resource.fromAutoCloseable(
    IO(new FileInputStream(s"${System.getProperty("user.dir")}/target/keys/$filename.jcek"))
  )

  implicit  val keyStoreXIO: KeyStoreX[IO] = new KeyStoreX[IO] {
//    override def createKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String]): IO[Unit] = {
//      val L = implicitly[LoggerX[IO]]
//      for{
//        ks             <-  KeyStore.getInstance(keyStoreXType.toString).pure[IO]
//        passwordChars  <- password.toCharArray.pure[IO]
//        _              <-  ks.load(null,passwordChars).pure[IO]
//        _              <- createFileOutput(name.getOrElse("default"))
//                          .use(ks.store(_,passwordChars).pure[IO] *> IO.unit)
////        _              <- L.info("KEYSTORE SAVED!")
//      } yield ()
//    }

    override def saveSecretKey(ks: KeyStore,keyx:KeyX, password: String): IO[Unit] = {
      implicit val KG = implicitly[KeyGeneratorX[IO]]
      val L = implicitly[LoggerX[IO]]
      for {
        sk                    <-  KG.generateKeyEntry(keyx.value,keyx.algorithm)
        _                     <-  L.info(sk.toString)
        passwords             <-  passwordFromString(password)
        _                     <-  ks.setEntry(keyx.alias,sk,passwords._2).pure[IO]
        _                     <-  createFileOutput("default")
                                  .use(x=>ks.store(x,passwords._1).pure[IO])
        _                     <-  L.info("SECRET KEY SAVED")
      } yield ()
    }

    override def loadKeyStore(keyStoreXType: KeyStoreXTypes, password: String, name: Option[String]):OptionT[IO, KeyStore] = {
      val L = implicitly[LoggerX[IO]]

      val response =
        for {
          ks            <- IO(KeyStore.getInstance(keyStoreXType.toString))
          ksLoad        = (in:FileInputStream)=>
                                  ks.load(in,password.toCharArray).pure[IO]

          _            <-  createFileInput(name.getOrElse("default"))
                            .use(ksLoad)
                            .handleErrorWith { x=>
                              IO.unit
                                .bracket(_=>IO.never.timeout(2 seconds))(_=>L.error(x.getMessage))
                            }
          ksResponse    <-  Option(ks).pure[IO]
          _             <-  L.info("KEYSTORE LOAD")
      } yield ksResponse
      OptionT[IO,KeyStore](response)
    }

    override def getSecretKey(ks: KeyStore, alias: String,pass:KeyStore.PasswordProtection): OptionT[IO,KeyStore.Entry] = {
      val L= implicitly[LoggerX[IO]]
      val x = for{
        sk       <- Option(ks.getEntry(alias,pass)).pure[IO]
        _        <- L.info(sk.toString)
      } yield sk
      OptionT[IO,KeyStore.Entry](x)
    }
    override def getSecretKeyFromDefaultKeyStore(alias: String)
    : OptionT[IO, KeyStore.Entry] = for {
        ks              <- loadKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default"))
        (_,password)    <- OptionT.liftF(passwordFromString("password"))
        secretKey       <- getSecretKey(ks,alias,password)
      } yield secretKey


    override def passwordFromString(password: String): IO[(Array[Char], KeyStore.PasswordProtection)] =
      for {
        passChr   <- password.toCharArray.pure[IO]
        passwords <- (passChr,new KeyStore.PasswordProtection(passChr)).pure[IO]
      } yield passwords


    override def create(path: String, password: String,ksType:Option[String],provider:Option[String])
    : IO[KeyStore] =
      for {
        ks   <- KeyStore.getInstance(ksType.getOrElse(KeyStore.getDefaultType),provider.getOrElse("SunJCE")).pure[IO]
        _    <- ks.load(null,password.toCharArray).pure[IO]
        out  <- new FileOutputStream(path).pure[IO]
        _    <- ks.store(out,password.toCharArray).pure[IO]
        _    <- Console[IO].println(ks)
      } yield ks

    override def load(path:String,password:String,ksType:Option[String],provider:Option[String])
    : IO[KeyStore] =
      for {
      ks  <- KeyStore.getInstance(ksType.getOrElse(KeyStore.getDefaultType),provider.getOrElse("SunJCE")).pure[IO]
      in  <- new FileInputStream(path).pure[IO]
      _   <- ks.load(in,password.toCharArray).pure[IO]
    } yield ks

    override def saveSymmetricKey(keystore: KeyStore, sk:SecretKey, alias: String, password: String): IO[Boolean] = for {
      skEntry         <- IO(new KeyStore.SecretKeyEntry(sk))
      protectionParam <-IO(new KeyStore.PasswordProtection(password.toCharArray))
      _               <- keystore.setEntry(alias,skEntry,protectionParam).pure[IO]
    } yield true

    override def savePrivateKey(keyStore: KeyStore, privateKey: PrivateKey,certificate: X509Certificate, alias: String, password: String)
    : IO[Boolean] = for {
      //      chain <- IO(new X509Certificate[2])
      _     <- keyStore.setKeyEntry(alias,privateKey,password.toCharArray,Array(certificate)).pure[IO]
    } yield true

    override def getCertificate(keyStore: KeyStore, alias: String): IO[X509Certificate] = for {
      cert <- keyStore.getCertificate(alias).asInstanceOf[X509Certificate].pure[IO]
    } yield cert

    override def getPrivateKey(keyStore: KeyStore, alias: String, password: String): IO[PrivateKey] = for {
      pk  <- keyStore.getKey(alias,password.toCharArray).asInstanceOf[PrivateKey].pure[IO]
    } yield pk

    override def getKeyPair(keyStore: KeyStore, alias: String, password: String): IO[KeyPair] = for {
      cert <- getCertificate(keyStore,alias)
      pk   <- cert.getPublicKey.pure[IO]
      sk   <- getPrivateKey(keyStore,alias,password)
      pair <- new KeyPair(pk,sk).pure[IO]
    } yield  pair

  }

}
