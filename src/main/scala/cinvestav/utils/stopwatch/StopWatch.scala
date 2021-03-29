/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/9/21, 9:03 PM
 ******************************************************************************/

package cinvestav.utils.stopwatch
import cats.effect.IO

import scala.concurrent.duration.{FiniteDuration, TimeUnit}

trait StopWatch[F[_]]{
   def measure[A](fa:F[A],timeUnit: TimeUnit):F[(A,FiniteDuration)]
}

object StopWatchDSL {
  implicit def stopWatch: StopWatch[IO] = new StopWatch[IO] {
    override def measure[A](fa: IO[A],timeUnit: TimeUnit): IO[(A,FiniteDuration)] =
      for {
        start  <- IO.realTime
        result <- fa
        finish <- IO.realTime
    } yield ( result,finish-start)

  }
}
