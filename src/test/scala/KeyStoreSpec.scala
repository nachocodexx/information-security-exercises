import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cinvestav.BouncyCastleApp
import cinvestav.crypto.keygen.enums.{KeyGeneratorAlgorithms, SecurityLevelsX}
import cinvestav.crypto.keystore.enums.KeyStoreXTypes
import cinvestav.crypto.providers.ProviderX
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.scalatest.funsuite.AnyFunSuite
import cats.implicits._,cats.effect.std.Console

import java.security.Security

/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/25/21, 8:09 PM
 ******************************************************************************/
import cinvestav.crypto.keystore.KeyStoreXDSL._
import cinvestav.crypto.keygen.KeyGeneratorXDSL._
//import org.bouncycastle.jce.provider.BouncyCastleProvider

class KeyStoreSpec extends AnyFunSuite{
  private val KEYSTORE_PATH = "/home/nacho/Programming/Scala/cinvestav-IS-00/target/keystore"
  private val KEYSTORE_NAME = "default.bks"
  def initBC():IO[Unit] = IO(
    Security.addProvider(new BouncyCastleProvider())
  )
  test("Create keystore"){
    val app = initBC() >> keyStoreXIO.
      create(s"$KEYSTORE_PATH/$KEYSTORE_NAME","changeit",Some("BKS"),Some("BC"))
    app.unsafeRunSync()
//    assert(condition = true)
  }

  test("Get certificate"){
    val app =  for {
      _       <- initBC()
      kStore  <- keyStoreXIO.load(s"$KEYSTORE_PATH/$KEYSTORE_NAME","changeit",Some("BKS"),Some("BC"))
      cert    <- keyStoreXIO.getCertificate(kStore,"mycert")
      _       <- Console[IO].println(cert)
    } yield ()
    app.unsafeRunSync()
  }

  test("Get keypair"){
    val app =  for {
      _       <- initBC()
      kStore  <- keyStoreXIO.load(s"$KEYSTORE_PATH/$KEYSTORE_NAME","changeit",Some("BKS"),Some("BC"))
      pair      <- keyStoreXIO.getKeyPair(kStore, "mycert","changeit")
      _       <- Console[IO].println(pair)
    } yield ()
    app.unsafeRunSync()
  }

}
