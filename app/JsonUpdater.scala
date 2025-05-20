/*
 * Copyright 2025 HM Revenue & Customs
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

import scala.io.Source
import scala.util.Using
import scala.util.{Success, Try}
import play.api.libs.json._

import java.io.{File, PrintWriter}

object JsonUpdater extends App {
  val filePath = "conf/code-lists/additionalDocumentCodes/additionalDocumentCodes.json"

  def readJsonFile(path: String): Try[JsArray] =
    Try(Using(Source.fromFile(path))(_.mkString).map(Json.parse).get match {
      case JsArray(elements) => JsArray(elements)
      case _                 => throw new IllegalArgumentException("Not provided a valid json file")
    })

  def writeJsonFile(path: String, data: JsArray): Unit = {
    val writer = new PrintWriter(new File(path))
    try
      writer.write(Json.prettyPrint(data))
    finally writer.close()

  }

  println("Please enter a comma-separated list of codes for DELETION:")
  val deletionInput = scala.io.StdIn.readLine()
  val codesToDelete = deletionInput.split(",").map(_.trim).filter(_.nonEmpty).toSet

  println("\nPlease enter a comma-separated list of codes for ADDITION:")
  val additionInput = scala.io.StdIn.readLine()
  val codesToAdd = additionInput
    .split(",")
    .map(_.trim)
    .filter(_.nonEmpty)
    .map(code => Json.obj("parentCode" -> JsString(code), "childCodes" -> JsArray(Seq(JsString("DocumentCodesRequiringAReason")))))
    .toList

  readJsonFile(filePath) match {
    case Success(existingData) =>
      println("Initial number of records in json file were : " + existingData.value.length)
      val updatedData = JsArray(existingData.value.filter { jsonObject =>
        (jsonObject \ "parentCode").asOpt[String].forall(!codesToDelete.contains(_))
      })
      println("Records removed from the json file are : " + codesToDelete.size)
      println("Records added to the json file are : " + codesToAdd.size)
      val finalData = JsArray(updatedData.value ++ codesToAdd)
      val sortedFinalData: JsArray = JsArray(finalData.value.sortBy(obj => (obj \ "parentCode").as[String]))
      writeJsonFile(filePath, sortedFinalData)

      println("Total number of records in json file after update are : " + finalData.value.length)
  }

}
