package cinvestav.crypto.hmac

object HMACAlgorithms extends Enumeration {
  type HMACAlgorithms = Value
  val HmacSHA1= Value("HmacSHA1")
  val HmacSHA256= Value("HmacSHA256")
  val HmacSHA384 = Value("HmacSHA384")
  val HmacSHA512 = Value("HmacSHA512")

}
