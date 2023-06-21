/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import org.apache.commons.codec.digest.HmacAlgorithms

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

object HashingUtils {
  private val algorithm = HmacAlgorithms.HMAC_SHA_256.toString

  def generateHashOfValue(value: String, hiddenSalt: String): String = {
    val secretSpec = new SecretKeySpec(hiddenSalt.getBytes(), algorithm)
    val hmac = Mac.getInstance(algorithm)

    hmac.init(secretSpec)

    val sig = hmac.doFinal(value.getBytes("UTF-8"))
    DatatypeConverter.printHexBinary(sig)
  }
}
