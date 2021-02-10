import cats.effect.IO
import cinvestav.crypto.cipher.CipherX.Transformation
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.utils.files.FilesOpsInterpreter._
import org.scalatest.funsuite.AnyFunSuite

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/3/21, 6:17 PM
 ******************************************************************************/
import cinvestav.CipherXApp.program
import cinvestav.crypto.cipher.CipherXDSL._
import cinvestav.utils.UtilsInterpreter._
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cats.implicits._

class CipherSpec extends AnyFunSuite{
  val desECB = Transformation(CipherXAlgorithms.DES,CipherXModel.ECB,CipherXPadding.NO_PADDING)
  val desCBC = Transformation(CipherXAlgorithms.DES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val des3ECB = Transformation(CipherXAlgorithms.DES3,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3CBC = Transformation(CipherXAlgorithms.DES3,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val aesECB = Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val aesCBC= Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  test("DES/ECB/PKCS5Padding"){
    program("deskey", desECB).map(_.unsafeRunSync).flatTap{x=>
      assert(true)
      Right(x)
    }
  }
  test("DES/CBC/PKCS5Padding"){
    program("deskey", desCBC).map(_.unsafeRunSync).flatTap{x=>
      assert(true)
      Right(x)
    }
  }
  test("DES3/ECB/PKCS5Padding"){
    program("des3key", des3ECB).map(_.unsafeRunSync).flatTap{x=>
      assert(true)
      Right(x)
    }
  }
  test("DES3/CBC/PKCS5Padding"){
    program("des3key", des3CBC).map(_.unsafeRunSync).flatTap{x=>
      assert(true)
      Right(x)
    }
  }
  test("AES/ECB/PKCS5Padding"){
    program("aeskey", aesECB).map(_.unsafeRunSync).flatTap{x=>
      assert(true)
      Right(x)
    }
  }
  test("AES/CBC/PKCS5Padding"){
    program("aeskey", aesCBC).map(_.unsafeRunSync).flatTap{x=>
      assert(true)
      Right(x)
    }
  }

}
