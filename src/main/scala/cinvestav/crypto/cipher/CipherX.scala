package cinvestav.crypto.cipher

import cats.Show
import cats.implicits._
import cats.effect.implicits._
import cats.effect.IO
import cinvestav.crypto.cipher.enums.CipherXAlgorithms.CipherXAlgorithm
import cinvestav.crypto.cipher.enums.CipherXMode
import cinvestav.crypto.cipher.enums.CipherXMode.CipherXMode
import cinvestav.crypto.cipher.enums.CipherXModel.CipherXModel
import cinvestav.crypto.cipher.enums.CipherXPadding.CipherXPadding
import cinvestav.logger.LoggerXInterpreter._
import cinvestav.logger.LoggerX
import cinvestav.utils.Utils
import cinvestav.crypto.keygen.KeyGeneratorX.KeyGeneratorX
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms
import cinvestav.crypto.keystore.KeyStoreX.KeyStoreX

import java.security.KeyStore
import javax.crypto.SecretKey
//import com.sun.crypto.provider.AESKeyGenerator

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


// Describe the set of operations to be perform of an Array[Byte]




object CipherX {
  trait CipherX[F[_]]{
    def encrypt(xs:Array[Byte],transformation: Transformation,key:KeyStore.Entry)
    :F[String]
    def decrypt(xs:Array[Byte]):F[String]
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
  import cinvestav.logger.LoggerXInterpreter._

  implicit  val cipherXIO = new CipherX[IO] {
    override def decrypt(xs: Array[Byte]): IO[String] = ???

    override def encrypt(xs: Array[Byte],transformation: Transformation,
//                         keyGeneratorAlgorithms: KeyGeneratorAlgorithms
                        key:KeyStore.Entry
                        ): IO[String] = {
      val KG = implicitly[KeyGeneratorX[IO]]
      val U = implicitly[Utils[IO]]
      val L = implicitly[LoggerX[IO]]
      for {
            cipher         <- Cipher.getInstance(transformation.show).pure[IO]
            k              <- IO(key.asInstanceOf[KeyStore.SecretKeyEntry].getSecretKey)
            _              <- cipher.init(CipherXMode.ENCRYPT.id,k).pure[IO]
            _              <- cipher.update(xs).pure[IO]
            bytes          <- cipher.doFinal().pure[IO]
            cipherText     <- (U.bytesToHexString _).pure[IO] ap bytes.pure[IO]
      } yield cipherText
    }
  }
}
