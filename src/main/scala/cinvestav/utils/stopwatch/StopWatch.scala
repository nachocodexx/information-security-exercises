/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/9/21, 9:03 PM
 ******************************************************************************/

package cinvestav.utils.stopwatch

import cats.implicits._
import cats.data.{Chain, StateT, WriterT}
import cats.effect.{Clock, IO, Sync, Timer}

import scala.concurrent.duration.TimeUnit

trait StopWatch[F[_]]{
   def measure[A](fa:F[A],timeUnit: TimeUnit)(implicit c:Timer[F]):F[(A,Long)]
}

object StopWatchDSL {
  implicit def stopWatchT[F[_]:Sync]: StopWatch[F] = new StopWatch[F] {
    override def measure[A](fa: F[A],timeUnit: TimeUnit)(implicit timer: Timer[F]): F[(A,Long)] =
      for {
        start  <- timer.clock.realTime(timeUnit)
        result <- fa
        finish <- timer.clock.realTime(timeUnit)
    } yield ( result,finish-start)

  }
}
