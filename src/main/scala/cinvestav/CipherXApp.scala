/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 10:39 PM
 ******************************************************************************/

package cinvestav

import cats.implicits._
import cats.data.OptionT
import cats.effect.{ExitCode, IO, IOApp}
import cinvestav.crypto.cipher.CipherX.{CipherX, Transformation}
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.keygen.KeyGeneratorX.KeyGeneratorX
import cinvestav.crypto.keygen.enums.{KeyGeneratorAlgorithms, SecretKeyAlgorithms}
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cinvestav.crypto.keystore.KeyStoreX.{KeyStoreX, KeyX}
import cinvestav.crypto.keystore.enums.KeyStoreXTypes
import cinvestav.logger.LoggerX
import cinvestav.logger.LoggerXInterpreter._

import java.security.KeyStore
import javax.crypto.SecretKeyFactory


object CipherXApp extends  IOApp{
  def program()(implicit C:CipherX[IO],KS:KeyStoreX[IO], L:LoggerX[IO]):IO[ExitCode] = {
    val response = for {
      transformation  <- OptionT.liftF(Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING).pure[IO])
//
      ks              <- KS.loadKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default"))
      (_,password)    <- OptionT.liftF(KS.passwordFromString("password"))
//
      secretKey       <- KS.getSecretKey(ks,"my-key",password)
      plainText       <- OptionT.liftF("Hola".pure[IO])
      cipherText      <- OptionT.liftF(C.encrypt(plainText.getBytes,transformation,secretKey))
      _               <- OptionT.liftF(L.info(s"Cipher text: ${cipherText}"))
    } yield ()
    response.value.as(ExitCode.Success)
  }


  def program2()(implicit KS:KeyStoreX[IO],L:LoggerX[IO]): IO[ExitCode] ={
    val x = for {
      passStr    <- OptionT.liftF("password".pure[IO])
      (_,_)      <- OptionT.liftF(KS.passwordFromString(passStr))
      ks         <- KS.loadKeyStore(KeyStoreXTypes.JCEKS,passStr,Some("default"))
      keyx       <- OptionT.liftF( KeyX(value="awesome",alias="my-key",KeyGeneratorAlgorithms.AES).pure[IO] )
      _          <- OptionT.liftF(KS.saveSecretKey(ks,keyx,passStr))
//      res <- IO.unit.as(ExitCode.Success
    } yield ()
    x.value.as(ExitCode.Success)
  }
  def program3()(implicit KS:KeyStoreX[IO],L:LoggerX[IO]) = {

   val res =  for {
      pp         <- OptionT.liftF(new KeyStore.PasswordProtection("password".toCharArray).pure[IO])
      ks         <- KS.loadKeyStore(KeyStoreXTypes.JCEKS, "password", Some("default"))
      entry      <- KS.getSecretKey(ks, "sk0", pp)
      _          <- OptionT.liftF(L.info(entry.toString) )
    } yield ()
    res.value.as(ExitCode.Success)
  }
  override def run(args: List[String]): IO[ExitCode] =
    program()
//      keyStoreXIO.createKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default")).as(ExitCode.Success)
}
