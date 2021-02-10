package cinvestav.crypto.cipher

import cats.Show
import cats.data.OptionT
import cats.implicits._
import cats.effect.{ContextShift, IO, Timer}
import cinvestav.crypto.cipher.enums.CipherXAlgorithms.CipherXAlgorithm
import cinvestav.crypto.cipher.enums.CipherXMode
import cinvestav.crypto.cipher.enums.CipherXModel.CipherXModel
import cinvestav.crypto.cipher.enums.CipherXPadding.CipherXPadding
import cinvestav.utils.Utils
import cinvestav.crypto.keygen.KeyGeneratorX.KeyGeneratorX
import cinvestav.crypto.keystore.KeyStoreX.KeyStoreX
import cinvestav.crypto.keystore.enums.KeyStoreXTypes
import cinvestav.logger.LoggerX
import fs2.Pipe

import java.security.KeyStore
import javax.crypto.Cipher

object CipherX {
  trait CipherX[F[_]]{
    def encrypt[A](xs:Array[Byte],transformation: Transformation,key:KeyStore.Entry,outputConvert:F[Array[Byte]=>A])
    :F[A]
    def encryptUsingKeyAlias[A](xs:Array[Byte],alias:String,transformation: Transformation,
                             outputConvert:F[Array[Byte]=>A])(implicit KS:KeyStoreX[F],cs:ContextShift[IO],
                                                              timer:Timer[IO])
    :F[Option[A]]

    def decrypt(xs:Array[Byte]):F[String]
    def encryptFile[A](key:KeyStore.Entry,transformation: Transformation,outputConvert:F[Array[Byte]=>A])(implicit L:LoggerX[IO])
    :Pipe[F,
      Array[Byte],A]
  }

  case class Transformation(algorithm:CipherXAlgorithm, model:CipherXModel, padding:CipherXPadding){
    def getValue = s"$algorithm/$model/$padding"
  }
  implicit val showTransformation:Show[Transformation] = Show.show {
    case Transformation(algorithm, model, padding) => s"${algorithm.toString}/${model.toString}/${padding.toString}"
  }

}

object CipherXDSL {
  import CipherX._
  import cinvestav.crypto.keygen.KeyGeneratorXDSL._
  import cinvestav.utils.UtilsInterpreter._
  import cinvestav.logger.LoggerX
  import cinvestav.logger.LoggerXDSL._

  implicit  val cipherXIO = new CipherX[IO] {
    override def decrypt(xs: Array[Byte]): IO[String] = ???

    override def encrypt[A](xs: Array[Byte],transformation: Transformation, key:KeyStore.Entry, outputConvert:IO[Array[Byte]=>A]): IO[A] = {
      for {
        cipher         <- Cipher.getInstance(transformation.show).pure[IO]
        secretKey       <- key.asInstanceOf[KeyStore.SecretKeyEntry].getSecretKey.pure[IO]
        _              <- cipher.init(CipherXMode.ENCRYPT.id,secretKey).pure[IO]
        _              <- cipher.update(xs).pure[IO]
        bytes          <- cipher.doFinal().pure[IO]
        cipherText     <- outputConvert ap bytes.pure[IO]
      } yield cipherText
    }

    override def encryptFile[A](key:KeyStore.Entry,transformation: Transformation,outputConvert:IO[Array[Byte]=>A])
                               (implicit L:LoggerX[IO])
    : Pipe[IO,
      Array[Byte], A] =
        _.evalMap(encrypt(_,transformation,key,outputConvert)).evalTap(L.info)

    override def encryptUsingKeyAlias[A](
                                          xs: Array[Byte],
                                          alias: String,
                                          transformation: Transformation,
                                          outputConvert: IO[Array[Byte] => A])(implicit KS:KeyStoreX[IO],
                                                                               cs:ContextShift[IO],timer:Timer[IO])
    : IO[Option[A]] = {
      val response = for {
        secretKey       <- KS.getSecretKeyFromDefaultKeyStore(alias)
        cipherText      <- OptionT.liftF(encrypt(xs,transformation,secretKey,outputConvert))
      } yield  cipherText

      response.value
    }
  }
}
