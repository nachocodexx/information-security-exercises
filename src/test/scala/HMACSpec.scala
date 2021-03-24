import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cinvestav.crypto.hmac.HMACAlgorithms
import cinvestav.crypto.hmac.HMACInterpreter._
import cinvestav.crypto.keygen.enums.KeyGeneratorAlgorithms._
import cinvestav.crypto.keygen.KeyGeneratorXDSL._
import org.scalatest.funsuite.AnyFunSuite
import cats.implicits._
import cinvestav.utils.UtilsInterpreter._

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 2/3/21, 8:12 PM
 ******************************************************************************/
import cinvestav.HMACApp.program
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.crypto.hmac.HMACInterpreter._
import cinvestav.utils.UtilsInterpreter._
import cinvestav.utils.formatters.FormattersDSL._
import cinvestav.logger.LoggerXDSL._

class HMACSpec extends AnyFunSuite{

  test("DES/SHA1"){
    program(DES,HMACAlgorithms.HmacSHA1)
      .unsafeRunSync()
    assert(true)
  }
  test("Simple test"){
    val cipherText=  for {
      key             <-   keyGeneratorXIO.generateRandomKey(DES)
      data            <-   "HOLA".pure[IO].map(_.getBytes)
      value           <-   HMACIO.digest(data,HMACAlgorithms.HmacSHA1,key)
      _               <-  loggerXIO.info(value)
    } yield value
    cipherText.unsafeRunSync()
    assert(true)
  }
}
