/*
 * Copyright 2019 HM Revenue & Customs
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

package models

//sealed abstract case class HtmlTable[A, B](header: HtmlTableRow[A], rows: Seq[HtmlTableRow[B]])
//
//object HtmlTable {
//
//  def apply[A, B](header: HtmlTableRow[A], rows: Seq[HtmlTableRow[B]]): Option[HtmlTable[A, B]] =
//    if (rows.exists(_.size != header.size)) None
//    else Some(new HtmlTable(header, rows) {})
//
//  def apply[A, B](header: A)(values: Seq[B]): HtmlTable[A, B] =
//    new HtmlTable(HtmlTableRow(header), values.map(HtmlTableRow(_))) {}
//
//  def apply[A, B](header1: A, header2: A)(values: Seq[(B, B)]): HtmlTable[A, B] =
//    new HtmlTable(HtmlTableRow(header1, List(header2)), values.map { case (a, b) => HtmlTableRow(a, List(b))}) {}
//
//  def apply[A, B](header1: A, header2: A, header3: A)(values: Seq[(B, B, B)]): HtmlTable[A, B] =
//    new HtmlTable(
//      HtmlTableRow(header1, List(header2, header3)),
//      values.map { case (a, b, c) => HtmlTableRow(a, List(b, c)) }) {}
//
//  def apply[A, B](header1: A, header2: A, header3: A, header4: A)(values: Seq[(B, B, B, B)]): HtmlTable[A, B] =
//    new HtmlTable(
//      HtmlTableRow(header1, List(header2, header3, header4)),
//      values.map { case (a, b, c, d) => HtmlTableRow(a, List(b, c, d)) }) {}
//
//  def apply[A, B](header1: A, header2: A, header3: A, header4: A, header5: A)
//    (values: Seq[(B, B, B, B, B)]): HtmlTable[A, B] =
//    new HtmlTable(
//      HtmlTableRow(header1, List(header2, header3, header4, header5)),
//      values.map { case (a, b, c, d, e) => HtmlTableRow(a, List(b, c, d, e)) }) {}
//}
//
//sealed abstract case class HtmlTableRow[A](value: A, values: Seq[A]) {
//
//  def map[B](f: A => B): HtmlTableRow[B] =
//    HtmlTableRow(f(value), values.map(f))
//
//  def foldLeft[B](z: B)(f: (B, A) => B): B =
//    values.foldLeft(f(z, value))(f)
//
//  def size: Int =
//    foldLeft(0)((b, _) => b + 1)
//}
//
//object HtmlTableRow {
//
//  def apply[A](value: A, values: Seq[A] = List()): HtmlTableRow[A] =
//    new HtmlTableRow(value, values) {}
//}