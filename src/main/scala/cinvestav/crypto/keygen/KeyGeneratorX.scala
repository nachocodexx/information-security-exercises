/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/1/21, 4:48 PM
 ******************************************************************************/

package cinvestav.crypto.keygen

import cats.effect.IO
import cats.implicits._
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms
import cinvestav.crypto.keygen.enums.SecretKeyAlgorithms.SecretKeyAlgorithms
import cinvestav.crypto.keygen.enums.SecurityLevelsX.SecurityLevelX
import cinvestav.crypto.providers.ProviderX.{BouncyCastle, ProviderX}

import java.security.{Key, KeyPair, KeyPairGenerator, KeyStore, SecureRandom}
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

object KeyGeneratorX{
  trait KeyGeneratorX[F[_]]{
    def getInstance(algorithms: KeyGeneratorAlgorithms):F[KeyGenerator]
    def generateKey(algorithms: KeyGeneratorAlgorithms):F[Key]
    def generateKeyEntry(password:String,keyGeneratorAlgorithms: KeyGeneratorAlgorithms):F[KeyStore.SecretKeyEntry]
    def generateRandomKey(algorithms: KeyGeneratorAlgorithms):F[Key]
    def keyPairGenerator(securityLvl:SecurityLevelX, algorithms: KeyGeneratorAlgorithms,
                         provider:Option[ProviderX]=Some(BouncyCastle))
    :F[KeyPair]
  }
}

object KeyGeneratorXDSL {

  import KeyGeneratorX._

  implicit val keyGeneratorXIO: KeyGeneratorX[IO] = new KeyGeneratorX[IO] {
    override def getInstance(algorithms: KeyGeneratorAlgorithms): IO[KeyGenerator] = IO(
      KeyGenerator.getInstance(algorithms.toString)
    )

    override def generateKey(algorithms: KeyGeneratorAlgorithms): IO[Key] = for {
      generator <- getInstance(algorithms)
      key <- generator.generateKey().pure[IO]
    } yield key

    override def generateKeyEntry(password: String, keyGeneratorAlgorithms: KeyGeneratorAlgorithms): IO[KeyStore.SecretKeyEntry] = {
      generateKey(keyGeneratorAlgorithms)
        .map(key => new SecretKeySpec(key.getEncoded, keyGeneratorAlgorithms.toString))
        .map(new KeyStore.SecretKeyEntry(_))
    }

    override def generateRandomKey(algorithms: KeyGeneratorAlgorithms): IO[Key] =
      for {
        generator <- getInstance(algorithms)
        secureRandom <- IO(new SecureRandom())
        _ <- generator.init(secureRandom).pure[IO]
        key <- generator.generateKey().pure[IO]
      } yield key

    override def keyPairGenerator(securityLevelX: SecurityLevelX,algorithms: KeyGeneratorAlgorithms, provider: Option[ProviderX]): IO[KeyPair] =
      for {
        keypairGen  <- KeyPairGenerator.getInstance(algorithms.toString,provider.getOrElse("SunJCE").toString).pure[IO]
        _           <- keypairGen.initialize(securityLevelX.toString.toInt).pure[IO]
      } yield keypairGen.generateKeyPair()
  }
}
