/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/17/21, 1:35 PM
 ******************************************************************************/

package cinvestav.lab

import cinvestav.crypto.cipher.CipherX.Transformation
import cinvestav.crypto.cipher.enums.{CipherXAlgorithms, CipherXModel, CipherXPadding}
import cinvestav.crypto.pbkdf2.enums.PBKDF2Algorithms

object Lab1 {
  val desECB  = Transformation(CipherXAlgorithms.DES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val desCBC  = Transformation(CipherXAlgorithms.DES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val des3ECB = Transformation(CipherXAlgorithms.DES3,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val des3CBC = Transformation(CipherXAlgorithms.DES3,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)
  val aesECB  = Transformation(CipherXAlgorithms.AES,CipherXModel.ECB,CipherXPadding.PKCS5PADDING)
  val aesCBC  = Transformation(CipherXAlgorithms.AES,CipherXModel.CBC,CipherXPadding.PKCS5PADDING)

  case class Lab1Test(cipherAlgorithms: CipherXAlgorithms.CipherXAlgorithm,hashAlgorithm:PBKDF2Algorithms.PBKDF2Algortims,keyLen:Int,
                      iterations:Int,transformation: Transformation){

  }
  object Lab1Test{

    def fromString(cipher:String,hash:String,keyLen:Int,iterations:Int,operationMode:String): Lab1Test = {
      val c =  CipherXAlgorithms.fromString(cipher)
      val m = CipherXModel.fromString(operationMode)
      Lab1Test(c,PBKDF2Algorithms.fromString(hash),keyLen,iterations,Transformation(c,m,CipherXPadding.PKCS5PADDING))
    }
  }
  val SHA1               = PBKDF2Algorithms.fromString("SHA1")
  val SHA256             = PBKDF2Algorithms.fromString("SHA256")
  val SHA384             = PBKDF2Algorithms.fromString("SHA384")
  val SHA512             = PBKDF2Algorithms.fromString("SHA512")
//  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
  val desECB_SHA1        = Lab1Test(CipherXAlgorithms.DES,SHA1,64,1000,desECB)
  val desECB_SHA256      = Lab1Test(CipherXAlgorithms.DES,SHA256,64,1000,desECB)
  val desECB_SHA384      = Lab1Test(CipherXAlgorithms.DES,SHA256,64,1000,desECB)
  val desECB_SHA512      = Lab1Test(CipherXAlgorithms.DES,SHA512,64,1000,desECB)

  val desCBC_SHA1        = Lab1Test(CipherXAlgorithms.DES,SHA1,64,1000,desCBC)
  val desCBC_SHA256      = Lab1Test(CipherXAlgorithms.DES,SHA256,64,1000,desCBC)
  val desCBC_SHA384      = Lab1Test(CipherXAlgorithms.DES,SHA384,64,1000,desCBC)
  val desCBC_SHA512     = Lab1Test(CipherXAlgorithms.DES,SHA512,64,1000,desCBC)
//  _____________________________________________________________________________
  val des3ECB_SHA256   = Lab1Test(CipherXAlgorithms.DES3,SHA256,192,1000,des3ECB)
  val des3CBC_SHA256   = Lab1Test(CipherXAlgorithms.DES3,SHA256,192,1000,des3CBC)
//  ****************************************************************************
  val des3ECB_SHA384   = Lab1Test(CipherXAlgorithms.DES3,SHA384,192,1000,des3ECB)
  val des3CBC_SHA384   = Lab1Test(CipherXAlgorithms.DES3,SHA384,192,1000,des3CBC)
  //  ****************************************************************************
  val des3ECB_SHA512   = Lab1Test(CipherXAlgorithms.DES3,SHA512,192,1000,des3ECB)
  val des3CBC_SHA512   = Lab1Test(CipherXAlgorithms.DES3,SHA512,192,1000,des3CBC)
//  _____________________________________________________________________________
  val aesECB_SHA1    = Lab1Test(CipherXAlgorithms.AES,SHA1,128,1000,aesECB)
  val aesCBC_SHA1    = Lab1Test(CipherXAlgorithms.AES,SHA1,128,1000,aesCBC)
// **************************************************************************
  val aesECB_SHA256  = Lab1Test(CipherXAlgorithms.AES,SHA256,256,1000,aesECB)
  val aesCBC_SHA256  = Lab1Test(CipherXAlgorithms.AES,SHA256,256,1000,aesCBC)
  val aesCBC192_SHA256  = Lab1Test(CipherXAlgorithms.AES,SHA256,192,1000,aesCBC)
//  **************************************************************************
  val aesECB_SHA384  = Lab1Test(CipherXAlgorithms.AES,SHA384,256,1000,aesECB)
  val aesCBC_SHA384  = Lab1Test(CipherXAlgorithms.AES,SHA384,256,1000,aesCBC)
  //  **************************************************************************
  val aesECB_SHA512  = Lab1Test(CipherXAlgorithms.AES,SHA512,256,1000,aesECB)
  val aesCBC_SHA512  = Lab1Test(CipherXAlgorithms.AES,SHA512,256,1000,aesCBC)
}
