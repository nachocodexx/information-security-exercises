/*******************************************************************************
 * Copyright (c) 2021, Ignacio Castillo.
 * ________________________________________
 * Created at: 3/24/21, 11:36 AM
 ******************************************************************************/

package cinvestav.crypto.keygen.enums

object SecurityLevelsX extends Enumeration {
  type SecurityLevelX = Value
//  Symmetric Schema
  val SS_56          = Value(56)
  val SS_80          = Value(80)
  val SS_112         = Value(112)
  val SS_128         = Value(128)
  val SS_192         = Value(192)
  val SS_256         = Value(192)
//  RSA / DSA
  val DS_RSADSA_512  = Value(512)
  val DS_RSADSA_1024 = Value(1024)
  val DS_RSADSA_2048 = Value(2048)
  val DS_RSADSA_3072 = Value(3072)
//  ECC
  val ECC_112        = Value(112)
  val ECC_160        = Value(160)
  val ECC_224        = Value(224)
  val ECC_256        = Value(256)
  val ECC_384        = Value(384)
  val ECC_512        = Value(512)


}
