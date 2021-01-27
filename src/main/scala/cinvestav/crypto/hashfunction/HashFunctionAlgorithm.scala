package cinvestav.crypto.hashfunction

object HashFunctionAlgorithm extends Enumeration {
  type HashFunctionAlgorithm = Value
  val SHA1: HashFunctionAlgorithm = Value("SHA-1")
  val SHA256: HashFunctionAlgorithm = Value("SHA-256")
  val SHA384: HashFunctionAlgorithm = Value("SHA-384")
  val SHA512: HashFunctionAlgorithm = Value("SHA-512")

  def fromString(x:String): HashFunctionAlgorithm = x match {
    case "SHA-1" => SHA1
    case "SHA-256" => SHA256
    case "SHA-384" => SHA384
    case "SHA-512" => SHA512
  }
  def fromInteger(x:Int):HashFunctionAlgorithm = x match {
    case 0 => SHA1
    case 1 => SHA256
    case 2 => SHA384
    case 3 => SHA512
  }
}
