import cats.data.{Chain, Writer, WriterT}
import cats.effect.IO
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import cinvestav.utils.stopwatch.StopWatchDSL._
import cats.implicits._
/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/9/21, 9:21 PM
 ******************************************************************************/

class StopWatchSpec extends AnyFunSuite{
//  implicit val timer:Timer[IO] = IO.timer(global)
  test("Measure the time"){
//    val t5 = Timer[IO].sleep(5 seconds).map(_=>1)
//    val t10 = Timer[IO].sleep(10 seconds).map(_=>2)
//    val w1:WriterT[IO,Chain[Long],Int] = WriterT(stopWatchT.measure(t5,SECONDS))
//    val w2:WriterT[IO,Chain[Long],Int] = WriterT(stopWatchT.measure(t10,SECONDS))
//    val r = for {
//      r1 <-w1
//      r2 <- w2
//    } yield r1+r2
////    val r = (w1.flatMap(x=>w2.fmap(y=>x+y)).run).unsafeRunSync()
//    println(r.run.unsafeRunSync())
//    val result  = for {
//      r1 <- stopWatchT.measure(w1.run,SECONDS)
//      r2 <- stopWatchT.measure(w2.run,SECONDS).map(_._1._2)
//    } yield 0
//    val a = result.unsafeRunSync()
//    println(a)
//    val fa =Timer[IO].sleep(5 seconds)
//      .map(x=>println("HOLAAA"))
//
//    val x = stopWatchT.measure(fa,MILLISECONDS).unsafeRunSync()
//    println(x)
    assert(true)
  }
}
