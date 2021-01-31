package cinvestav.crypto.cipher

import cats.effect.IO

trait CipherX[F[_]]{

  def encrypt(xs:Array[Byte]):F[String]
  def decrypt(xs:Array[Byte]):F[String]
}


object CipherXInterpreter {

  implicit  val cipherXIO = new CipherX[IO] {
    override def encrypt(xs: Array[Byte]): IO[String] = ???
    override def decrypt(xs: Array[Byte]): IO[String] = ???
  }
}
