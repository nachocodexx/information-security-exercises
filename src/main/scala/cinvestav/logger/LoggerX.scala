/** *****************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 8:37 PM
 * **************************************************************************** */

package cinvestav.logger

import cats.effect.IO

import java.text.SimpleDateFormat
import java.util.Date
import scala.Console.{RED, RESET}


trait LoggerX[F[_]]{
  def format(message:String,format:Option[String]):String
  def info(message:String):F[Unit]
  def error(message:String):F[Unit]
//  def
}
object LoggerXInterpreter {
  implicit val loggerXIO: LoggerX[IO] = new LoggerX[IO] {
    override def info(message: String): IO[Unit] = {
      IO(Console.println(format(message,None)))
    }

    override def error(message: String): IO[Unit] = {
     IO(Console.println(s"$RED${format(message,None)}$RESET" ))
    }

    override def format(message: String, format: Option[String]): String = {

      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")
      val threadName= Thread.currentThread().getName
      val currentDate= new Date(System.nanoTime())
      s"${dateFormatter.format(currentDate)} - [$threadName] $message"
    }
  }
}
