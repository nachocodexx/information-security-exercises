/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/9/21, 9:03 PM
 ******************************************************************************/

package cinvestav.utils.stopwatch

import cats.implicits._
import cats.data.{Chain, StateT, WriterT}
import cats.effect.{Clock, IO, Sync}

import scala.concurrent.duration.{FiniteDuration, TimeUnit}

trait StopWatch[F[_]]{
   def measure[A](fa:F[A],timeUnit: TimeUnit):F[(A,FiniteDuration)]
}

object StopWatchDSL {
  implicit def stopWatchT[F[_]]: StopWatch[IO] = new StopWatch[IO] {
    override def measure[A](fa: IO[A],timeUnit: TimeUnit): IO[(A,FiniteDuration)] =
      for {
        start  <- IO.realTime
        result <- fa
        finish <- IO.realTime
//        duratio <- IO
    } yield ( result,finish-start)

  }
}
