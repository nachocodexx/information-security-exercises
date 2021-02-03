/** *****************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/1/21, 4:49 PM
 * **************************************************************************** */

package cinvestav.crypto.keygen.enums

object KeyGeneratorAlgorithms extends Enumeration {
  type KeyGeneratorAlgorithms = Value
  val AES = Value("AES")
  val ARC4 = Value("ARCFOUR")
  val DES = Value("DES")
  val DES3 = Value("DESede")
  val HmacSHA1 = Value("HmacSHA1")
  val HmacSHA224 = Value("HmacSHA224")
  val HmacSHA256 = Value("HmacSHA256")
  val HmacSHA384 = Value("HmacSHA384")
  val HmacSHA512 = Value("HmacSHA512")
  val RC2  = Value("RC2")

}
