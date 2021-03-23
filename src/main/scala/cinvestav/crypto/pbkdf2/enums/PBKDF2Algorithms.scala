/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/16/21, 9:30 AM
 ******************************************************************************/

package cinvestav.crypto.pbkdf2.enums

object PBKDF2Algorithms extends Enumeration {
  type PBKDF2Algortims = Value
  private val base =(x:String)=> s"PBKDF2WithHmac$x"
  val HMACSHA1 = Value(base("SHA1"))
  val HMACSHA256 = Value(base("SHA256"))
  val HMACSHA384 = Value(base("SHA384"))
  val HMACSHA512 = Value(base("SHA512"))

  def fromString(x:String) = Value(base(x))
  def getHashAlgorithm(x:PBKDF2Algorithms.PBKDF2Algortims) = x.toString.split("PBKDF2WithHmac")(1)
}
