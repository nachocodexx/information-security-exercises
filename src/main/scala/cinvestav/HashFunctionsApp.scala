
/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 1/30/21, 8:22 PM
 ******************************************************************************/

package cinvestav
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms.MessageDigestAlgorithms
import cinvestav.crypto.hashfunction.enums.MessageDigestAlgorithms
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.utils.files.FilesOps
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import pureconfig._
import pureconfig.generic.auto._
import cinvestav.crypto.hashfunction.HashFunctions
import cinvestav.config.DefaultConfig
import cinvestav.crypto.hashfunction.HashFunctionsInterpreter._
//
import scala.language.postfixOps

object HashFunctionsApp extends IOApp {



  def program(algorithm: MessageDigestAlgorithms)(implicit FI:FilesOps[IO], HF:HashFunctions[IO]): IO[ExitCode] =  {
    val app = ConfigSource.default.load[DefaultConfig]
//      .flatMap(x=>FI.digest(x.dirPath,algorithm).asRight)
      .flatMap(_=>HF.digest("HOLA ".getBytes,algorithm).asRight)
//      .map(x=>L)

//      .flatMap(config=>FI.cleanDirectory(config.dirPath).asRight)
//        .flatMap(config=>FI.replicateFilesN(s"${config.dirPath}/${config.filenameGenesis}",config.dirPath,1500).asRight)

    app match {
      case Left(value) =>
        println("ERROR",value)
        IO.unit.as(ExitCode.Error)
      case Right(value) =>
        value.as(ExitCode.Success)
    }
  }




  override def run(args: List[String]): IO[ExitCode] =
    program(MessageDigestAlgorithms.SHA1)



}
