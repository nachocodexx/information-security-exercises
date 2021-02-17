/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/15/21, 8:50 PM
 ******************************************************************************/

package cinvestav.crypto.pbkdf2
import cats.implicits._
import cats.effect.IO
import cinvestav.crypto.cipher.enums.CipherXAlgorithms.CipherXAlgorithm
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms.PBKDF2Algortims
import cinvestav.utils.Utils
import com.sun.javafx.scene.traversal.Algorithm

import java.security.SecureRandom
import javax.crypto.{SecretKey, SecretKeyFactory}
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}

case class SecretKeyAndSalt(key:SecretKey,salt:Array[Byte])
trait PBKDF2X[F[_]] {
  def generatePassword(cipherXAlgorithm: CipherXAlgorithm, algorithm:PBKDF2Algortims, rawPassword:String, saltBytes:Int, iterations:Int,
                       hashBytes:Int):F[SecretKey]

//  def getKeyFrom(cipherXAlgorithm: CipherXAlgorithm,algortim: PBKDF2Algortims,salt:String,iterations:Int)(implicit
//                                                                                                           U:Utils[F])
//  :F[SecretKey]
  def generatePasswordWithSalt(salt:Array[Byte],cipherXAlgorithm: CipherXAlgorithm, algorithm:PBKDF2Algortims,
                               rawPassword:String,
                        saltBytes:Int, iterations:Int,
                       hashBytes:Int):F[SecretKey]
  def generateSalt(saltBytes:Int):F[Array[Byte]]
}
object PBKDF2XDSL {
  implicit  def pbkdf2IO: PBKDF2X[IO] = new PBKDF2X[IO] {
    override def generatePassword(cipherXAlgorithm: CipherXAlgorithm, algorithm: PBKDF2Algortims, rawPassword: String, saltBytes: Int, iterations: Int,
                                  hashBytes: Int)
    : IO[SecretKey] =
      for {
        salt <- generateSalt(saltBytes)
        passwordChar <- rawPassword.toCharArray.pure[IO]
        passwordSpec <- IO(new PBEKeySpec(passwordChar, salt, iterations, hashBytes))
        skf <- IO(SecretKeyFactory.getInstance(algorithm.toString))
        result <- skf.generateSecret(passwordSpec).pure[IO]
        key <- IO(new SecretKeySpec(result.getEncoded, cipherXAlgorithm.toString))
      } yield key

    override def generateSalt(saltBytes:Int): IO[Array[Byte]] =  for {
      srg    <-  IO(new SecureRandom())
      salt   <-  IO(new Array[Byte](saltBytes))
      _      <-  srg.nextBytes(salt).pure[IO]
    } yield  salt

    override def generatePasswordWithSalt(salt:Array[Byte], cipherXAlgorithm: CipherXAlgorithm,
    algorithm: PBKDF2Algortims,
                                          rawPassword: String, saltBytes: Int, iterations: Int, hashBytes: Int)
    : IO[SecretKey] = for {
//      salt         <- generateSalt(saltBytes)
      passwordChar <- rawPassword.toCharArray.pure[IO]
      passwordSpec <- IO(new PBEKeySpec(passwordChar, salt, iterations, hashBytes))
      skf          <- IO(SecretKeyFactory.getInstance(algorithm.toString))
      result       <- skf.generateSecret(passwordSpec).pure[IO]
      key          <- IO(new SecretKeySpec(result.getEncoded, cipherXAlgorithm.toString))
    } yield key

//    override def getKeyFrom(cipherXAlgorithm: CipherXAlgorithm, algortim: PBKDF2Algortims, salt: String,
//                             iterations: Int)(implicit U:Utils[IO]): IO[SecretKey] =
//      for {
//       saltBytes <- U.fromHex(salt).pure[IO]
//
//    } yield ???
  }
}
