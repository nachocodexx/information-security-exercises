/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 8:59 PM
 ******************************************************************************/

package cinvestav.crypto.cipher.enums

object CipherXAlgorithms extends Enumeration {

  type CipherXAlgorithm = Value
  val AES:CipherXAlgorithm = Value("AES")
  val DES:CipherXAlgorithm= Value("DES")
  val DES3:CipherXAlgorithm = Value("DESede")
}
