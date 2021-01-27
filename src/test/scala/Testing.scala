import cats.effect.IO
import cinvestav.crypto.hashfunction.HashFunctionAlgorithm
import cinvestav.crypto.hashfunction.HashFunctionAlgorithm.{HashFunctionAlgorithm, SHA1, SHA256, SHA384, SHA512}
import org.scalameter.api._
import org.scalameter.picklers.Pickler
import org.scalameter.picklers.noPickler.instance
//import org.scalameter.picklers.Implicits._
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.crypto.hashfunction.HashFunctionsInterpreter._
import cinvestav.HashFunctionsApp.program
object Testing extends  Bench.OfflineReport{

//  val generator: Gen[Int] = Gen.range("algorithms")(0,3,1)

  val gen00: Gen[HashFunctionAlgorithm] =  Gen.single("SHA-1")(HashFunctionAlgorithm.SHA1)
  val gen01: Gen[HashFunctionAlgorithm] = Gen.single("SHA-256")(HashFunctionAlgorithm.SHA256)
  val gen02: Gen[HashFunctionAlgorithm] = Gen.single("SHA-384")(HashFunctionAlgorithm.SHA384)
  val gen03: Gen[HashFunctionAlgorithm] = Gen.single("SHA-512")(HashFunctionAlgorithm.SHA512)
  override def persistor = new SerializationPersistor("target/results")

  override def reporter: Reporter[Double] = Reporter.Composite(
    new DsvReporter(','),
    new RegressionReporter(
      RegressionReporter.Tester.Accepter(),
      RegressionReporter.Historian.ExponentialBackoff()
    ),
    new HtmlReporter(true)
  )
//  val algorithms = Gen.range("size")(0,3,1)
//  val (SHA1,SHA256,SHA384,SHA512) = HashFunctionAlgorithm
  val algorithms= Gen.enumeration("algorithms")(SHA1,SHA256,SHA384,SHA512)
//  val algorithms: Gen[String] = Gen.enumeration("algorithms")(SHA1.toString,SHA256.toString,SHA384.toString,SHA512.toString)

  performance of "FileOps" in {
    measure method "digest" config (
      exec.independentSamples -> 2,
      exec.benchRuns->5
    ) in {
//      using(algorithms) in { x=> program[IO](HashFunctionAlgorithm.fromInteger(x)).unsafeRunSync()}
//      using(algorithms) in { x=> program[IO](HashFunctionAlgorithm.fromInteger(x)).unsafeRunSync()}
//        using(algorithms) in { x=> program[IO](HashFunctionAlgorithm.fromString(x)).unsafeRunSync()}
        using(algorithms) in { x=> program(x).unsafeRunSync()}
//      using(gen00) in {algorithm=>program[IO](algorithm).unsafeRunSync()}
//      using(gen01) in {algorithm=>program[IO](algorithm).unsafeRunSync()}
//      using(gen02) in {algorithm=>program[IO](algorithm).unsafeRunSync()}
//      using(gen03) in {algorithm=>program[IO](algorithm).unsafeRunSync()}
    }
  }
}
