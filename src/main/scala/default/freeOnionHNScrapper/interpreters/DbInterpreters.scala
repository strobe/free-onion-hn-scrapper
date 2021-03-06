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

package default.freeOnionHNScrapper.interpreters

import cats.~>
import monix.eval.Task
import default.freeOnionHNScrapper.MainProgram.DbDSL
import default.freeOnionHNScrapper.impl.DbDSLImpl
import scalikejdbc._

/**
  * Created by Evgeniy Tokarev on 28/11/2016.
  */
object DbInterpreters extends DbDSLImpl {

  val dbDsl2Task = new (DbDSL ~> Task) {
    def apply[A](fa: DbDSL[A]): Task[A] = fa match {
      case DbDSL.Save(items) =>
        Task(saveHYNews(items))
    }
  }
}
