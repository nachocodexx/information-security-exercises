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
import cinvestav.utils.crypto.enums.KeyGeneratorAlgorithms
//import cinvestav.crypto.hmac.KeyGeneratorAlgorithms
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cinvestav.crypto.keystore.KeyStoreX.KeyStoreX
import cinvestav.crypto.keystore.enums.KeyStoreXTypes
import cinvestav.logger.LoggerX
import cinvestav.logger.LoggerXInterpreter._
import cinvestav.utils.crypto.KeyGeneratorX

import java.security.KeyStore
import javax.crypto.SecretKeyFactory


object CipherXApp extends  IOApp{
  def program()(implicit C:CipherX[IO]):IO[ExitCode] = {
    val t = Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
    C.encrypt("hola".getBytes,t,KeyGeneratorAlgorithms.AES)
      .as(ExitCode.Success)
  }
  def program2()(implicit KS:KeyStoreX[IO],L:LoggerX[IO]): IO[ExitCode] ={
    val x = for {
      passStr    <- OptionT.liftF("password".pure[IO])
      pp         <- OptionT.liftF( new KeyStore.PasswordProtection(passStr.toCharArray).pure[IO] )
      ks         <- KS.loadKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default"))
      _          <- OptionT.liftF(KS.saveSecretKey(ks,"sk0","secretkey",passStr))
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
    program2()
//      keyStoreXIO.createKeyStore(KeyStoreXTypes.JCEKS,"password",Some("default")).as(ExitCode.Success)
}
