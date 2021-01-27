import cinvestav.crypto.hashfunction.HashFunctionAlgorithm
import cinvestav.crypto.hmac.HMACAlgorithms.HMACAlgorithms
//import cinvestav.crypto.hashfunction.HashFunctionAlgorithm.{HashFunctionAlgorithm, SHA1, SHA256, SHA384, SHA512}
import cinvestav.crypto.hmac.KeyGeneratorAlgorithms
import cinvestav.crypto.hmac.HMACAlgorithms
import org.scalameter.api._
import org.scalameter.picklers.noPickler.instance
import cinvestav.HMACApp.program
import cinvestav.crypto.hmac.HMACInterpreter._
import cinvestav.utils.files.FilesOpsInterpreter._
import cinvestav.utils.UtilsInterpreter._


object HMACSpec extends  Bench.OfflineReport{


  override def persistor = new SerializationPersistor("target/results")

  override def reporter: Reporter[Double] = Reporter.Composite(
    new DsvReporter(','),
    new RegressionReporter(
      RegressionReporter.Tester.Accepter(),
      RegressionReporter.Historian.ExponentialBackoff()
    ),
    new HtmlReporter(true)
  )
  val algorithms: Gen[HMACAlgorithms] = Gen.enumeration("algorithms")(
    HMACAlgorithms.HmacSHA1,HMACAlgorithms.HmacSHA256,HMACAlgorithms.HmacSHA384,HMACAlgorithms.HmacSHA512
  )

  performance of "FileOps" in {
    measure method "digest" config (
      exec.independentSamples -> 2,
      exec.benchRuns->5
    ) in {
        using(algorithms) in { x=> program(KeyGeneratorAlgorithms.HmacSHA1,x).unsafeRunSync()}
    }
  }
}
