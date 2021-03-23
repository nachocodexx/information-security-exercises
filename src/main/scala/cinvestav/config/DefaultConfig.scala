package cinvestav.config
import pureconfig._
import pureconfig.generic.auto._
case class DefaultConfig(dirPath:String,
                         filenameGenesis:String,
                         dataPath:String,
                         encryptPath:String,
                         decryptPath:String,
                         cipher:String,
                         hashFunction:String,
                         keyLength:Int,
                         iterations:Int,
                         operationMode:String,
                         cipherMode:Int,
                         csvPath:String,
                         password:String
                        )

