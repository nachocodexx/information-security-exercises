/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/24/21, 12:08 PM
 ******************************************************************************/

package cinvestav.crypto.signatures.enums

object SignatureAlgorithmsX extends Enumeration {
  type SignatureAlgorithmX = Value
  val SHA1WithDSA = Value("SHA1withDSA")
  val SHA224WithDSA = Value("SHA224withDSA")
  val SHA256WithDSA = Value("SHA256withDSA")
  val SHA384WithDSA = Value("SHA384withDSA")
  val SHA512WithDSA = Value("SHA512withDSA")
  val SHA512WithECDSA = Value("SHA512withECDSA")
  val SHA256WithECDSA = Value("SHA256withECDSA")
  val SHA224WithECDSA = Value("SHA224withECDSA")
}
