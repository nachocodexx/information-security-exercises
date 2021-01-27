package cinvestav.crypto.hmac

object KeyGeneratorAlgorithms extends Enumeration {
  type KeyGeneratorAlgorithms = Value
  val AES = Value("AES")
  val DES = Value("DES")
  val DESede = Value("DESede")
  val HmacSHA1 = Value("HmacSHA1")
  val HmacSHA256 = Value("HmacSHA256")
  val HmacSHA384  = Value("HmacSHA384")
  val HmacSHA512= Value("HmacSHA512")


}
