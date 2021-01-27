package cinvestav
import cinvestav.crypto.hashfunction.HashFunctionAlgorithm
import cinvestav.crypto.hashfunction.HashFunctionAlgorithm.HashFunctionAlgorithm
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.utils.files.FilesOps
//
import cats.implicits._
import cats.effect.implicits._
import cats.effect.{ExitCode, IO, IOApp, Sync}
//
import pureconfig._
import pureconfig.generic.auto._
//
import cinvestav.crypto.hashfunction.HashFunctions
import cinvestav.config.DefaultConfig
import cinvestav.crypto.hashfunction.HashFunctionsInterpreter._
//
import scala.language.postfixOps

object HashFunctionsApp extends IOApp {



  def program(algorithm: HashFunctionAlgorithm)(implicit FI:FilesOps[IO], HF:HashFunctions[IO]): IO[ExitCode] =  {
    val app = ConfigSource.default.load[DefaultConfig]
      .flatMap(x=>FI.digest(x.dirPath,algorithm).asRight)
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
    program(HashFunctionAlgorithm.SHA1)



}
