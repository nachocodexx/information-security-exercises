/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/25/21, 9:52 PM
 ******************************************************************************/

package cinvestav.crypto.keyagreement

import cats.effect.IO
import cats.implicits._

import java.security.{PrivateKey, PublicKey}
import javax.crypto.{KeyAgreement, SecretKey}

trait KeyAgreementX[F[_]]{
  def sharedSecret(algorithm:String, secretAlgorithm:String, initiatorSk:PrivateKey,
                   recipientPk:PublicKey,provider:String="BC")
  :F[SecretKey]
  def sharedSecretECCDHAES256(initiatorSk:PrivateKey,
                              recipientPk:PublicKey):F[SecretKey]
//  def recipientKeyAgreement(algorithm:String,provider:String,secretAlgorithm:String,recipientSk:PrivateKey,initiatorPk:PublicKey):F[SecretKey]
}
object KeyAgreementXDSL {
  implicit val keyAgreementXIO: KeyAgreementX[IO] = new KeyAgreementX[IO] {
    override def sharedSecret(algorithm:String,secretAlgorithm:String, initiatorSk: PrivateKey,
                              recipientPk: PublicKey,provider:String)
    : IO[SecretKey] =
      for {
      keyAgreement <- KeyAgreement.getInstance(algorithm,provider).pure[IO]
      _            <- keyAgreement.init(initiatorSk).pure[IO]
      _            <- keyAgreement.doPhase(recipientPk,true).pure[IO].map(_.asInstanceOf[SecretKey])
      secret     <- keyAgreement.generateSecret(secretAlgorithm).pure[IO]
    } yield secret

//    override def recipientKeyAgreement(algorithm:String,provider:String,secretAlgorithm:String,recipientSk:PrivateKey,initiatorPk:PublicKey): IO[SecretKey] = for {
//      keyAgreement <- KeyAgreement.getInstance(algorithm,provider).pure[IO]
//      _            <- keyAgreement.init(recipientSk).pure[IO]
//      _            <- keyAgreement.doPhase(initiatorPk,true).pure[IO].map(_.asInstanceOf[SecretKey])
//      secret       <- keyAgreement.generateSecret(secretAlgorithm).pure[IO]
//    } yield secret
    override def sharedSecretECCDHAES256(initiatorSk: PrivateKey, recipientPk: PublicKey): IO[SecretKey] =
    sharedSecret("ECCDH","AES[256]",initiatorSk,recipientPk)
  }
}
