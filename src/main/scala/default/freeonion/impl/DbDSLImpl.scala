/*
 * Copyright 2016 Evgeniy Tokarev
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

package default.freeOnionHNScrapper.impl

import cats.data.NonEmptyList
import scalikejdbc._
import org.joda.time._
import default.freeOnionHNScrapper.model.DataModel._

trait DbDSLImpl {
  def saveHYNews(items: NonEmptyList[PieceOfNews])(
      implicit session: DBSession = AutoSession): Unit = {
    for {
      i <- items
    } yield {
      sql"""
         |INSERT INTO news (name, link, points, created_at)
         |VALUES (${i.name}, ${i.link}, ${i.points}, ${DateTime.now})
         |ON DUPLICATE KEY UPDATE created_at = ${DateTime.now}
        """.stripMargin.update.apply()
    }
    ()
  }
}
