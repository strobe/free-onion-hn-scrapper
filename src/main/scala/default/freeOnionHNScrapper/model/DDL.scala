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

package default.freeOnionHNScrapper.model

import scalikejdbc.{ AutoSession, DBSession }
import scalikejdbc.config.DBs

import scala.util.Try

import scalikejdbc._
import scalikejdbc.config._

/**
  * Created by Evgeniy Tokarev on 11/12/2016.
  */
trait DDL {
  def dbInit()(implicit session: DBSession = AutoSession): Unit = {
    DBs.setupAll()

    val query = Try {
      // table creation, you can run DDL by using #execute as same as JDBC
      sql"""
         |CREATE TABLE IF NOT EXISTS NEWS
         |(
         |  ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
         |  NAME VARCHAR(250),
         |  LINK CLOB,
         |  POINTS INTEGER,
         |  CREATED_AT TIMESTAMP NOT NULL
         |);
         |ALTER TABLE NEWS ADD CONSTRAINT IF NOT EXISTS NAME_IDX UNIQUE(NAME);
         |""".stripMargin.execute.apply()
    }
  }

  def dbClose()(implicit session: DBSession = AutoSession): Unit = {
    DBs.closeAll() // wipes out ConnectionPool
  }
}
