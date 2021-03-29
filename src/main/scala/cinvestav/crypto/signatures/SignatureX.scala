/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/24/21, 12:08 PM
 ******************************************************************************/

package cinvestav.crypto.signatures
import cats.effect.IO
import cats.implicits._
import cinvestav.crypto.providers.ProviderX.{BouncyCastle, ProviderX}
import cinvestav.crypto.signatures.enums.SignatureAlgorithmsX.SignatureAlgorithmX

import java.security.{PrivateKey, PublicKey, Signature}

case class SignatureResult(algorithm:String,value:Array[Byte])
trait SignatureX[F[_]] {
  def sign(data:Array[Byte], sk:PrivateKey, signatureAlgorithmX: SignatureAlgorithmX, provider:Option[ProviderX]=Some
  (BouncyCastle))
  :F[SignatureResult]
  def verify(
              data:Array[Byte],
              signatureResult:SignatureResult,
              pk:PublicKey,
              provider:Option[ProviderX]=Some(BouncyCastle)):F[Boolean]
}
object SignatureXDSL {
  implicit val signatureXIO: SignatureX[IO] = new SignatureX[IO] {
    override def sign(data: Array[Byte], sk: PrivateKey, signatureAlgorithmX: SignatureAlgorithmX,
                      provider: Option[ProviderX]): IO[SignatureResult] = for {
      signature <- Signature.getInstance(signatureAlgorithmX.toString,provider.getOrElse("SunJCE").toString).pure[IO]
      _         <- signature.initSign(sk).pure[IO]
      _         <- signature.update(data).pure[IO]
      result    <- SignatureResult(signatureAlgorithmX.toString,signature.sign()).pure[IO]
    } yield  result
    override def verify(data: Array[Byte], signatureResult: SignatureResult, pk: PublicKey,
                        provider: Option[ProviderX]): IO[Boolean] =
      for {
        signature  <- Signature.getInstance(signatureResult.algorithm,provider.getOrElse("SunJCE").toString).pure[IO]
        _          <- signature.initVerify(pk).pure[IO]
        _          <- signature.update(data).pure[IO]
      } yield signature.verify(signatureResult.value)
  }
}
