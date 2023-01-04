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

object ListItem {

  def createId[A](index: Int, item: A) = s"$index.${item.hashCode()}"

  def findById[A](id: String, items: Seq[A]): Option[A] =
    try
      id.split("\\.") match {
        case Array(index: String, hashcode: String) if items(index.toInt).hashCode() == hashcode.toInt => Some(items(index.toInt))
        case _                                                                                         => None
      }
    catch {
      case _: Exception => None
    }
}
