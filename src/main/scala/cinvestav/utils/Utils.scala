package cinvestav.utils

import cats.implicits._
import cats.effect.implicits._
import cats.effect.IO
import javax.xml.bind.DatatypeConverter

trait Utils[F[_]] {
  def defaultPad(x:Int):F[String]
  def toHex(x:Array[Byte]):String
  def toBase64(x:Array[Byte]):String
  def fromBase64(x:String):Array[Byte]
  def fromHex(x:String):Array[Byte]
}


object UtilsInterpreter {

  implicit  val utilsIO: Utils[IO] = new Utils[IO] {
    override def defaultPad(x: Int): IO[String] = f"$x%02d".pure[IO]

    override def toHex(xs: Array[Byte]): String =DatatypeConverter.printHexBinary(xs).toLowerCase

//      xs.map(x=>Integer.toHexString(0xFF&x)).fold("")(_+_)
    override def fromHex(x: String): Array[Byte] = DatatypeConverter.parseHexBinary(x)

    override def toBase64(x: Array[Byte]): String = DatatypeConverter.printBase64Binary(x)

    override def fromBase64(x: String): Array[Byte] = DatatypeConverter.parseBase64Binary(x)
  }


}
