/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/9/21, 8:29 PM
 ******************************************************************************/

package cinvestav.utils.formatters

import cats.effect.IO

trait Formatter[F[_],A]{
  def format:F[Array[Byte]=>A]
}

object FormattersDSL {
  implicit val formatterIOString: Formatter[IO, String] = new Formatter[IO,String] {
    override def format: IO[Array[Byte]=>String] =
      IO((xs:Array[Byte])=>xs.map(x=>Integer.toHexString(0xFF&x)).fold("")(_+_))


  }
}
