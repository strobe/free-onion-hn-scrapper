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

import cats._
import cats.data.NonEmptyList
import cats.free.Free
import freek._
import monix.eval._
import monix.cats._
import monix.execution.Scheduler.Implicits.global
import scalikejdbc._
import scalikejdbc.config._
import java.io.FileWriter

import scala.language.higherKinds
import scala.util.{ Failure, Success, Try }
import scala.concurrent.duration._
import scala.concurrent.Await

import default.freeOnionHNScrapper.model.DataModel._
import default.freeOnionHNScrapper.interpreters.{ DbInterpreters, HttpInterpreters }
import default.freeOnionHNScrapper.model.DDL
import DbInterpreters._
import HttpInterpreters._
import MainProgram._

/**
  * Created by Evgeniy Tokarev on 13/11/2016.
  */
object MainApp extends App with DDL {

  dbInit()

  val transpileNat = CopKNat[CoreAndDbDSL.Cop].replace(HttpSubProgram.transpiler)
  val freeProgram  = MainProgram.program("https://news.ycombinator.com", 10).transpile(transpileNat)

  val finalInterpreter                              = dbDsl2Task :&: httpWLog2Task
  val finalProgram: Task[NonEmptyList[PieceOfNews]] = freeProgram.interpret(finalInterpreter)

  val future = finalProgram.runAsync

  future.onComplete {
    case Success(value) =>
      println(s"success, ${value.toList.size} news items written")
    case Failure(ex) =>
      System.err.println(s"""ERROR: ${ex.getMessage}
        ${ex.getStackTrace.toList.take(10).fold("")((x, y) => x + "\n" + y)}""")
  }

  Await.result(future, 10.seconds)

  dbClose()
}
