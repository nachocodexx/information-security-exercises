package cinvestav.crypto.cipher

import cats.Show
import cats.data.OptionT
import cats.implicits._
import cats.effect.IO
import cinvestav.crypto.cipher.enums.CipherXAlgorithms.CipherXAlgorithm
import cinvestav.crypto.cipher.enums.CipherXMode
import cinvestav.crypto.cipher.enums.CipherXMode.CipherXMode
import cinvestav.crypto.cipher.enums.CipherXModel.CipherXModel
import cinvestav.crypto.cipher.enums.CipherXPadding.CipherXPadding
import cinvestav.crypto.keystore.KeyStoreX.KeyStoreX
import cinvestav.crypto.keystore.enums.KeyStoreXTypes
import cinvestav.crypto.providers.ProviderX.ProviderX
import cinvestav.logger.LoggerX
import fs2.Pipe
import cats.effect.std.Console

import java.security.{AlgorithmParameters, KeyStore}
import javax.crypto.spec.IvParameterSpec
import javax.crypto.{Cipher, SecretKey}

object CipherX {
  case class CipherText(value:Array[Byte], params:Option[AlgorithmParameters],algorithm: Option[String],
                        size:Option[Long])
  case class PlainText(bytes:Array[Byte])
  case class Transformation(algorithm:CipherXAlgorithm, model:CipherXModel, padding:CipherXPadding){
    def getValue = s"$algorithm/$model/$padding"
  }
  implicit val showTransformation:Show[Transformation] = Show.show {
    case Transformation(algorithm, model, padding) => s"${algorithm.toString}/${model.toString}/${padding.toString}"
  }

  trait CipherX[F[_]]{
    def cipher(mode:CipherXMode.CipherXMode,xs: Array[Byte], transformation: Transformation, secretKey:SecretKey,
               providerX: Option[ProviderX])
    :F[CipherText]
    def encrypt(xs:Array[Byte],transformation: Transformation,secretKey: SecretKey,provider:Option[ProviderX])
    :F[CipherText]
    def decrypt(cipherText:CipherText,transformation: Transformation,secretKey: SecretKey,providerX: Option[ProviderX])
    :F[PlainText]
    def encryptUsingKeyAlias[A](xs:Array[Byte],alias:String,transformation: Transformation)
                               (implicit KS:KeyStoreX[F]):F[Option[CipherText]]

    def encryptFile(key:KeyStore.Entry,transformation: Transformation )(implicit L:LoggerX[IO]):Pipe[F, Array[Byte],CipherText]
  }


}

object CipherXDSL {
  import CipherX._
  import cinvestav.logger.LoggerX

  implicit  val cipherXIO: CipherX[IO] = new CipherX[IO] {
    override def decrypt(cipherText: CipherText,transformation: Transformation,secretKey: SecretKey,provider:Option[ProviderX])
    : IO[PlainText] =
      cipherText.params match {
      case Some(value) => for {
              cipher    <- Cipher.getInstance(transformation.show,provider.getOrElse("SunJCE").toString).pure[IO]
              _         <- cipher.init(CipherXMode.DECRYPT.id,secretKey,value).pure[IO]
              bytes     <- cipher.doFinal(cipherText.value).pure[IO]
              plainText <- PlainText(bytes) .pure[IO]
      } yield plainText
      case None => cipher(CipherXMode.DECRYPT,cipherText.value,transformation,secretKey,provider).map(x=>PlainText(x
        .value))
    }


    override def encrypt(xs: Array[Byte], transformation: Transformation, secretKey:SecretKey,provider: Option[ProviderX])
    : IO[CipherText] = cipher(CipherXMode.ENCRYPT,xs,transformation,secretKey,provider)

    override def encryptFile(key:KeyStore.Entry,transformation: Transformation )(implicit L:LoggerX[IO]): Pipe[IO,
      Array[Byte], CipherText] =
        _.evalMap(encrypt(_,transformation,key.asInstanceOf[KeyStore.SecretKeyEntry].getSecretKey,None)).evalTap(L
          .info)

    override def encryptUsingKeyAlias[A](xs: Array[Byte], alias: String, transformation: Transformation)(implicit KS:KeyStoreX[IO])
    : IO[Option[CipherText]] = {
      val response = for {
        secretKey       <- KS.getSecretKeyFromDefaultKeyStore(alias)
        cipherText      <- OptionT.liftF(encrypt(xs,transformation,secretKey.asInstanceOf[KeyStore.SecretKeyEntry]
          .getSecretKey,None))
      } yield  cipherText

      response.value
    }

    override def cipher(mode: CipherXMode, xs: Array[Byte], transformation: Transformation, secretKey: SecretKey,
                        provider: Option[ProviderX])
    : IO[CipherText] =
      for {
//        Constant provider: only for testing purposes = Bouncy Castle.
        cipher         <- Cipher.getInstance(transformation.show).pure[IO]
        params         <- cipher.getParameters.pure[IO]
//        _              <- Console[IO].println(params)
        _              <- cipher.init(mode.id,secretKey,params).pure[IO]
        bytes          <- cipher.doFinal(xs).pure[IO]
        result         <- CipherText(bytes,Option(params),Some(transformation.show),Some(bytes.size)).pure[IO]
      } yield result

  }
}
