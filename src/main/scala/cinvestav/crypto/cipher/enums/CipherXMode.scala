/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/31/21, 10:27 AM
 ******************************************************************************/

package cinvestav.crypto.cipher.enums
import javax.crypto.Cipher.{ENCRYPT_MODE,DECRYPT_MODE,WRAP_MODE,UNWRAP_MODE}

object CipherXMode extends  Enumeration {
  type CipherXMode = Value
  val ENCRYPT:CipherXMode = Value(ENCRYPT_MODE)
  val DECRYPT:CipherXMode = Value(DECRYPT_MODE)
  val WRAP:CipherXMode = Value(WRAP_MODE)
  val UNWRAP:CipherXMode = Value(UNWRAP_MODE)

}
