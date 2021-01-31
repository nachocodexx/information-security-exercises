/** *****************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 8:37 PM
 * **************************************************************************** */

package cinvestav.logger

import cats.effect.IO


trait LoggerX[F[_]]{
  def info(message:String):F[Unit]
}
object LoggerXInterpreter {
  implicit val loggerXIO: LoggerX[IO] = new LoggerX[IO] {
    override def info(message: String): IO[Unit] =
      IO(println(message))
  }
}
