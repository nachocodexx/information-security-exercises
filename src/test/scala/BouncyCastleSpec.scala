import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import cinvestav.crypto.cipher.CipherX.Transformation
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.scalatest.funsuite.AnyFunSuite

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/22/21, 9:19 PM
 ******************************************************************************/
import cinvestav.crypto.pbkdf2.PBKDF2XDSL._
import cinvestav.crypto.cipher.CipherXDSL._
import java.security.Security

class BouncyCastleSpec extends AnyFunSuite{
  private val t1 = Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  def initBC():IO[Unit] = IO(
    Security.addProvider(new BouncyCastleProvider())
  )
  test("Cipher: encrypt"){


//    val app = initBC() >> pbkdf2IO
//      .generatePassword(CipherXAlgorithms.AES,PBKDF2Algorithms.HMACSHA256,"hola",8,1000,256)
//      .flatMap(key=>
//        cipherXIO.encrypt("HOLA".getBytes,t1,key)
//          .flatMap(cipherXIO.decrypt(_,t1,key))
////          .map
//      )
//      .map{ x=>
//        println(new String(x.bytes))
//      }
////      .map(x=>println(x))
//    app.unsafeRunSync()
////    println("HELLOE")
  }

}
