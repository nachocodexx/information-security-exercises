/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 9:06 PM
 ******************************************************************************/

package cinvestav.crypto.cipher.enums

object CipherXPadding extends Enumeration {
  type CipherXPadding = Value
  val NO_PADDING: CipherXPadding = Value("NoPadding")
  val PKCS5PADDING:CipherXPadding = Value("PKCS5Padding")
  val PKCS1PADDING:CipherXPadding= Value("PKCS1Padding")
  val OAEPWithSHAAndMGF1Padding:CipherXPadding = Value("OAEPWithSHA-1AndMGF1Padding")

}
