/** *****************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 8:37 PM
 * **************************************************************************** */

package cinvestav.logger

import cats.effect.IO

import java.text.SimpleDateFormat
import java.util.Date
import scala.Console.{GREEN, RED, RESET}


trait LoggerX[F[_]]{
  def format(message:Any,format:Option[String]):String
  def info(message:Any):F[Unit]
  def error(message:String):F[Unit]
//  def
}
object LoggerXDSL {
  implicit val loggerXIO: LoggerX[IO] = new LoggerX[IO] {
    override def info(message: Any): IO[Unit] = {
      IO(Console.println(s"$GREEN${format(message.toString,None)}$RESET"))
    }

    override def error(message: String): IO[Unit] = {
     IO(Console.println(s"$RED${format(message,None)}$RESET" ))
    }

    override def format(message: Any, format: Option[String]): String = {

      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")
      val threadName= Thread.currentThread().getName
      val currentDate= new Date(System.currentTimeMillis())
      s"${dateFormatter.format(currentDate)} - [$threadName] $message"
    }
  }
}
