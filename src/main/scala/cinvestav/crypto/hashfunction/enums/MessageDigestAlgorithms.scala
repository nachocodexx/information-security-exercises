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

}
