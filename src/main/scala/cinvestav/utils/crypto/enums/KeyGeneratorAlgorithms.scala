/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 10:12 PM
 ******************************************************************************/

package cinvestav.utils.crypto.enums

object KeyGeneratorAlgorithms extends Enumeration {
  type KeyGeneratorAlgorithms = Value
  val AES = Value("AES")
  val DES  = Value("DES")
  val DES3 = Value("DESede")
  val HmacSHA1 = Value("HmacSHA1")
  val HmacSHA256  = Value("HmacSHA256")

}
