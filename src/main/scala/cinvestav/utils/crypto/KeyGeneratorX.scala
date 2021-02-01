/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 10:04 PM
 ******************************************************************************/

package cinvestav.utils.crypto

import cats.implicits._
import cats.effect.implicits._
import cats.effect.IO
import cinvestav.utils.crypto.enums.{KeyGeneratorAlgorithms, SecretKeyAlgorithms}
import cinvestav.utils.crypto.enums.KeyGeneratorAlgorithms.KeyGeneratorAlgorithms

import java.security.{Key, KeyStore}
import javax.crypto.{KeyGenerator, SecretKeyFactory}
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}

object KeyGeneratorX{
  trait KeyGeneratorX[F[_]]{
    def getInstance(algorithms: KeyGeneratorAlgorithms):F[KeyGenerator]
    def generateKey(algorithms: KeyGeneratorAlgorithms):F[Key]
    def generateKeyEntry(password:String):F[KeyStore.SecretKeyEntry]
  }
}

object KeyGeneratorXDSL {
  import KeyGeneratorX._

  implicit  val keyGeneratorXIO: KeyGeneratorX[IO] = new KeyGeneratorX[IO] {
    override def getInstance(algorithms: KeyGeneratorAlgorithms): IO[KeyGenerator] = IO(
      KeyGenerator.getInstance(algorithms.toString)
    )

    override def generateKey(algorithms: KeyGeneratorAlgorithms): IO[Key] = for {
      generator <- getInstance(algorithms)
      key <- generator.generateKey().pure[IO]
    } yield key

    override def generateKeyEntry(password: String): IO[KeyStore.SecretKeyEntry] =
      IO(
        new SecretKeySpec(password.getBytes,"DSA")
      ).map(new KeyStore.SecretKeyEntry(_))
//        SecretKeyFactory
//          .getInstance(SecretKeyAlgorithms.PBEWithHmacSHA256AndAES_128.toString)
//          .generateSecret(
//          )
//      ).map(ne)
//      IO(new KeyStore.SecretKeyEntry( new SecretKeySpec(password.getBytes,KeyGeneratorAlgorithms.AES.toString) ))
//        generateKey(KeyGeneratorAlgorithms.AES).map{key=>
//          val sk = new SecretKeySpec(key.getEncoded,KeyGeneratorAlgorithms.AES.toString)
//          new KeyStore.SecretKeyEntry(sk)
//        }
  }
}
