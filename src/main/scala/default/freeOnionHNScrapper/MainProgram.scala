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

package default.freeOnionHNScrapper

import cats.data.NonEmptyList

import freek._
import default.freeOnionHNScrapper.model.DataModel._

/**
  * Created by Evgeniy Tokarev on 26/11/2016.
  */
object MainProgram {

  sealed trait CoreDSL[A]
  object CoreDSL {
    case class GetYCNews(url: String) extends CoreDSL[NonEmptyList[PieceOfNews]]
    case class Filter(list: NonEmptyList[PieceOfNews], p: PieceOfNews => Boolean)
        extends CoreDSL[NonEmptyList[PieceOfNews]]
  }

  sealed trait DbDSL[A]
  object DbDSL {
    case class Save(items: NonEmptyList[PieceOfNews]) extends DbDSL[Unit]
  }

  type CoreAndDbDSL = CoreDSL :|: DbDSL :|: NilDSL
  val CoreAndDbDSL = DSL.Make[CoreAndDbDSL]

  def program(url: String, minScore: Int) = {
    for {
      list     <- CoreDSL.GetYCNews(url).freek[CoreAndDbDSL]
      filtered <- CoreDSL.Filter(list, _.points >= minScore).freek[CoreAndDbDSL]
      _        <- DbDSL.Save(filtered).freek[CoreAndDbDSL]
    } yield filtered
  }
}
