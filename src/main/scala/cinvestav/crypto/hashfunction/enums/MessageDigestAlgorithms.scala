/** *****************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/1/21, 3:17 PM
 * **************************************************************************** */

package cinvestav.crypto.hashfunction.enums

object MessageDigestAlgorithms extends Enumeration {
  type MessageDigestAlgorithms = Value
  val MD2: MessageDigestAlgorithms = Value("MD2")
  val MD5: MessageDigestAlgorithms = Value("MD5")
  val SHA1: MessageDigestAlgorithms = Value("SHA-1")
  val SHA224: MessageDigestAlgorithms = Value("SHA-224")
  val SHA256: MessageDigestAlgorithms = Value("SHA-256")
  val SHA384: MessageDigestAlgorithms = Value("SHA-384")
  val SHA512: MessageDigestAlgorithms = Value("SHA-512")
  val SHA512_224: MessageDigestAlgorithms = Value("SHA-512/224")
  val SHA512_256: MessageDigestAlgorithms = Value("SHA-512/256")

  def fromString(x: String): MessageDigestAlgorithms = x match {
    case "SHA-1" => SHA1
    case "SHA-256" => SHA256
    case "SHA-384" => SHA384
    case "SHA-512" => SHA512
  }

  def fromInteger(x: Int): MessageDigestAlgorithms = x match {
    case 0 => SHA1
    case 1 => SHA256
    case 2 => SHA384
    case 3 => SHA512
  }
}
