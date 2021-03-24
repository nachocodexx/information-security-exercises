/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/22/21, 10:55 PM
 ******************************************************************************/

package cinvestav.crypto.providers

object ProviderX extends Enumeration {
  type ProviderX   = Value
  val BouncyCastle:ProviderX = Value("BC")

}
