/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/31/21, 12:09 PM
 ******************************************************************************/

package cinvestav.crypto.keystore.enums

import java.security.KeyStore

object KeyStoreXTypes extends Enumeration {
  type KeyStoreXTypes = Value
  val JKS = Value("JKS")
  val JCEKS = Value("JCEKS")
  val PKCS12 = Value("PKCS12")
  val DKS = Value("DKS")

}
