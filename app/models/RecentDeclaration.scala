/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS

import utils.{Page, Pagination}

import scala.util.Random

sealed trait RecentDeclarationStatus

case object PreLodged extends RecentDeclarationStatus
case object Arrived extends RecentDeclarationStatus
case object Accepted extends RecentDeclarationStatus
case object Query extends RecentDeclarationStatus
case object Departed extends RecentDeclarationStatus

sealed trait RecentDeclarationMode

case object Preview extends RecentDeclarationMode
case object Normal extends RecentDeclarationMode

case class RecentDeclaration(
  name: String,
	dateAndTime: String,
	ducr: String,
	status: RecentDeclarationStatus,
	messages: String,
	action: String
)

// Companion object to generate fake data to show behaviour of dashboard
//scalastyle:off magic.number
object RecentDeclaration {
	def random(from: LocalDateTime, to: LocalDateTime): LocalDateTime = {
		val diff = DAYS.between(from, to)
		val random = new Random(System.nanoTime) // You may want a different seed
		from.plusDays(random.nextInt(diff.toInt))
	}

	val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
	val from = LocalDateTime.of(2018, 7, 2, 0, 0)
	val to = LocalDateTime.now()

	val prelodged = RecentDeclaration(
		"Name" + Random.nextInt(1000000),
		dateTimeFormatter.format(random(from, to)),
		"5GB123456789000-" + Random.nextInt(100000),
		PreLodged,
		"Messages",
		"Action"
	)

	val arrived = RecentDeclaration(
		"Name" + Random.nextInt(1000000),
		dateTimeFormatter.format(random(from, to)),
		"5GB123456789000-" + Random.nextInt(100000),
		Arrived,
		"Messages",
		"Action"
	)

	val accepted = RecentDeclaration(
		"Name" + Random.nextInt(1000000),
		dateTimeFormatter.format(random(from, to)),
		"5GB123456789000-" + Random.nextInt(100000),
		Accepted,
		"Messages",
		"Action"
	)

	val query = RecentDeclaration(
		"Name" + Random.nextInt(1000000),
		dateTimeFormatter.format(random(from, to)),
		"5GB123456789000-" + Random.nextInt(100000),
		Query,
		"Messages",
		"Action"
	)

	val departed = RecentDeclaration(
		"Name" + Random.nextInt(1000000),
		dateTimeFormatter.format(random(from, to)),
		"5GB123456789000-" + Random.nextInt(100000),
		Departed,
		"Messages",
		"Action"
	)

	def generateRecentDeclarations(amount: Int): List[RecentDeclaration] = {
		val statuses = List(prelodged, arrived, accepted, query, departed)

		List.fill(amount)(statuses).flatten.sortWith(_.dateAndTime < _.dateAndTime)
	}

	def generatePageDeclarations(page: Int): Page[RecentDeclaration] = {
		val pagination = new Pagination[RecentDeclaration](generateRecentDeclarations(96), page, 9)

		pagination.currentPageResult
	}
}
//scalastyle:on magic.number
