/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/31/21, 12:05 PM
 ******************************************************************************/

package cinvestav.crypto.keystore

import cats.data.OptionT
import cats.implicits._
import cats.effect.{ IO, Resource}
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms
import cinvestav.crypto.keystore.enums.KeyStoreXTypes.KeyStoreXTypes
import cinvestav.logger.LoggerX
import cinvestav.crypto.keygen.KeyGeneratorX.KeyGeneratorX
import com.sun.javafx.scene.traversal.Algorithm

import java.io.{FileInputStream, FileNotFoundException, FileOutputStream}
import java.security.KeyStore
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

object KeyStoreX {
  trait KeyStoreX[F[_]]{
    def createKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String]):F[Unit]
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
    override def createKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String]): IO[Unit] = {
      val L = implicitly[LoggerX[IO]]
      for{
        ks             <-  KeyStore.getInstance(keyStoreXType.toString).pure[IO]
        passwordChars  <- password.toCharArray.pure[IO]
        _              <-  ks.load(null,passwordChars).pure[IO]
        _              <- createFileOutput(name.getOrElse("default"))
                          .use(ks.store(_,passwordChars).pure[IO] *> IO.unit)
        _              <- L.info("KEYSTORE SAVED!")
      } yield ()
    }

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
//        ks       <- loadKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default"))
//        password <- new KeyStore.PasswordProtection(pass.toCharArray).pure[IO]
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
  }

}
