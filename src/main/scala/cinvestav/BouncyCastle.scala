/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/22/21, 10:05 PM
 ******************************************************************************/

package cinvestav

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cinvestav.crypto.cipher.CipherX.Transformation
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.Security
import javax.crypto.{KeyGenerator, SecretKey}
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.crypto.hashfunction.HashFunctionsInterpreter._
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cats.effect.std.Console
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms
import cinvestav.crypto.providers.ProviderX

object BouncyCastle extends IOApp{
  def initBC():IO[Unit] = IO(
    Security.addProvider(new BouncyCastleProvider())
  )
  def genKey():IO[SecretKey]= {
    val keyGenerator = KeyGenerator.getInstance("AES", "BC")
    keyGenerator.init(256)
    keyGenerator.generateKey.pure[IO]
  }

  override def run(args: List[String]): IO[ExitCode] =for {
    _           <- initBC()
    key         <- genKey()
    t1          <- Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING).pure[IO]
    message     <- "hola".getBytes.pure[IO]
    cipherText  <- cipherXIO.encrypt(message,t1,key,Some(ProviderX.BouncyCastle))
    platinText  <- cipherXIO.decrypt(cipherText,t1,key,Some(ProviderX.BouncyCastle)).map(x=>new String(x.bytes))
    _           <- Console[IO].println(platinText)
    digest      <- hashFunctionAlgorithmIO.digest(platinText.getBytes,MessageDigestAlgorithms.SHA512)
    _           <- Console[IO].println(digest)

  } yield ExitCode.Success
}
