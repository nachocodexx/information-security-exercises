/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 9:04 PM
 ******************************************************************************/

package cinvestav.crypto.cipher.enums

object CipherXModel extends  Enumeration {
  type CipherXModel = Value
  val CBC = Value("CBC")
  val ECB = Value("ECB")
}
