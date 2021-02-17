/** *****************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/1/21, 4:49 PM
 * **************************************************************************** */

package cinvestav.crypto.keygen.enums

import cinvestav.crypto.cipher.enums.CipherXAlgorithms.CipherXAlgorithm
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms.MessageDigestAlgorithms

object SecretKeyAlgorithms extends Enumeration {
  type SecretKeyAlgorithms = Value
  val AES = Value("AES")
  val ARC4 = Value("ARCFOUR")
  val DES = Value("DES")
  val DES3 = Value("DESede")
  //  val PBEWithHmacSHA256AndAES_128=Value("PBEWithHmacSHA256AndAES_128")
  def getPBE(digest: MessageDigestAlgorithms, encryption: CipherXAlgorithm,keyLen:Int): SecretKeyAlgorithms =
    Value(s"PBEWith${digest.toString.replace("-","")}And${encryption.toString}_$keyLen")

}
