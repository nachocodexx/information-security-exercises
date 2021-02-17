import cats.effect.IO
import cinvestav.crypto.cipher.CipherX.{CipherText, Transformation}
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms
import cinvestav.crypto.keygen.enums.SecretKeyAlgorithms
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms
import org.scalatest.funsuite.AnyFunSuite

import java.security.AlgorithmParameters
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{SecretKey, SecretKeyFactory}

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/15/21, 9:04 PM
 ******************************************************************************/
import cats.implicits._
import cinvestav.crypto.pbkdf2.PBKDF2XDSL._
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.utils.UtilsInterpreter._

class Pbkdf2Spec extends AnyFunSuite{

  val desECB = Transformation(CipherXAlgorithms.DES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val desCBC = Transformation(CipherXAlgorithms.DES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val des3ECB = Transformation(CipherXAlgorithms.DES3,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3CBC = Transformation(CipherXAlgorithms.DES3,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val aesECB = Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val aesCBC= Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  test("Generate SALT") {
    val salt =  pbkdf2IO.generateSalt(8)
      .map(utilsIO.toHex)
      .unsafeRunSync()
    println(salt)
    assert(true)
  }
  test ("Generate password"){
    val password = "topsecret"
    val plaintext = "helloworld"
    val secretKey  = pbkdf2IO
      .generatePassword(CipherXAlgorithms.AES,PBKDF2Algorithms.HMACSHA1,password,100,1000,128)
      .unsafeRunSync()

//    val key =secretKey.getEncoded
//    val hex = utilsIO.toHex(key)
//    val keyBytes = utilsIO.fromHex(hex)
//    val areEquals = key.sameElements(keyBytes)
//    assert(areEquals)
//    println(hex)
    val cipherText = cipherXIO.encrypt(plaintext.getBytes,aesCBC,secretKey).unsafeRunSync()
    val plain      = cipherXIO.decrypt(cipherText,aesCBC,secretKey).unsafeRunSync()
    val iv = cipherText.params.map(_.getEncoded).getOrElse(Array.empty[Byte])
    val ivv = new IvParameterSpec(iv)
    println(utilsIO.toHex(iv))
    val text = new String(plain.bytes,"UTF8")

    val params = AlgorithmParameters.getInstance("AES")
    params.init(iv)
    val cipherText2  = CipherText(plain.bytes,Some(params),None,None)
    val plain2 = cipherXIO.decrypt(cipherText,aesCBC,secretKey).unsafeRunSync()
    val text2  =  new String(plain2.bytes,"UTF8")
    println(plain2)
    println(text2)
    println(text)
    println(plain)
//    println(plain.bytes)
//    println(utilsIO.toHex(cipherText))
//    val plainText2 = cipherXIO.decrypt(cipherText,desCBC,secretKey).unsafeRunSync()
//    val response = plainText2.sameElements(plaintext.getBytes)
//    assert(response)
  }
  test("TEst") {
    class Ta(foo:String,bar:String)
    object Ta {
      def apply(f:String,b:String)=s"$f:$b"
      def unapply(x:String):Option[(String,String)] = {
        val xs = x.split(':')
        Option((xs(0),xs(1)))
      }
    }
    val ta = Ta("NACHO","CaSTILLO")
    println(ta)
    ta match {
      case Ta(name,last)=> println(name,last)
    }

  }

}
