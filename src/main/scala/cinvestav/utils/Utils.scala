package cinvestav.utils

import cats.implicits._
import cats.effect.implicits._
import cats.effect.IO

trait Utils[F[_]] {
  def defaultPad(x:Int):F[String]
  def bytesToHexString(x:Array[Byte]):String
}


object UtilsInterpreter {

  implicit  val utilsIO: Utils[IO] = new Utils[IO] {
    override def defaultPad(x: Int): IO[String] = f"$x%02d".pure[IO]

    override def bytesToHexString(xs: Array[Byte]): String =
      xs.map(x=>Integer.toHexString(0xFF&x)).fold("")(_+_)

  }


}
