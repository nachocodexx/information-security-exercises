/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/31/21, 12:05 PM
 ******************************************************************************/

package cinvestav.crypto.keystore

import cats.data.OptionT
import cats.implicits._
import cats.effect.{ContextShift, ExitCode, IO, Resource, Timer}
import cinvestav.crypto.keystore.enums.KeyStoreXTypes.KeyStoreXTypes
import cinvestav.logger.LoggerX
import cinvestav.utils.crypto.KeyGeneratorX.KeyGeneratorX
import com.sun.javafx.scene.traversal.Algorithm

import java.io.{FileInputStream, FileNotFoundException, FileOutputStream}
import java.security.KeyStore
import java.util.concurrent.ScheduledExecutorService
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import scala.util.Try
import scala.concurrent.duration._
import scala.language.postfixOps
import cinvestav.utils.crypto.KeyGeneratorXDSL._
import cinvestav.utils.crypto.enums.KeyGeneratorAlgorithms

object KeyStoreX {
  trait KeyStoreX[F[_]]{
    def createKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String]):F[Unit]
    def loadKeyStore(keyStoreXType: KeyStoreXTypes,password:String,name:Option[String])(implicit
                                                                                        cs:ContextShift[IO],
                                                                                        timer:Timer[IO])
    :OptionT[F,
      KeyStore]
    def saveSecretKey(ks:KeyStore,alias:String,key:String,password:String):F[Unit]
    def getSecretKey(ks:KeyStore,alias:String,pass:KeyStore.PasswordProtection):OptionT[F,KeyStore.Entry]
  }
}


object KeyStoreXDSL {
  import KeyStoreX._
  import cinvestav.logger.LoggerXInterpreter._
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

    override def saveSecretKey(ks: KeyStore, alias: String, key: String, password: String): IO[Unit] = {
      implicit val KG = implicitly[KeyGeneratorX[IO]]
      val L = implicitly[LoggerX[IO]]
      for {
        sk    <-  KG.generateKeyEntry(key)
        _     <-  L.info(sk.toString)
        passChar <- password.toCharArray.pure[IO]
        pass  <-  new KeyStore.PasswordProtection(passChar).pure[IO]
        _     <-  ks.setEntry(alias,sk,pass).pure[IO]
        _     <-  createFileOutput("default")
                  .use(x=>ks.store(x,passChar).pure[IO])
        _     <-  L.info("SECRET KEY SAVED")
      } yield ()
    }

    override def loadKeyStore(keyStoreXType: KeyStoreXTypes, password: String, name: Option[String])
                             (implicit cs:ContextShift[IO],timer: Timer[IO])
    : OptionT[IO,
      KeyStore] = {
      val L = implicitly[LoggerX[IO]]

      val response =
        for {
//          passwordChr   <- password.toCharArray.pure[IO]
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
//        password <- new KeyStore.PasswordProtection(pass.toCharArray).pure[IO]
        sk       <- Option(ks.getEntry(alias,pass)).pure[IO]
        _        <- L.info(sk.toString)
      } yield sk
      OptionT[IO,KeyStore.Entry](x)
    }
  }

}
