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

import java.io.FileWriter

import cats.data.NonEmptyList
import cats.free.Free
import cats.~>
import default.freeOnionHNScrapper.HttpSubProgram.{ HttpDSL, LogDSL }
import default.freeOnionHNScrapper.MainProgram.DbDSL
import default.freeOnionHNScrapper.{ Const, Interpreter, ~< }
import default.freeOnionHNScrapper.impl.HttpDSLImpl

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.cats._
import freek._

/**
  * Created by Evgeniy Tokarev on 26/11/2016.
  */
object HttpInterpreters extends HttpDSLImpl {

  val logDsl2Task = new (LogDSL ~> Task) {
    def appendToFile(str: String) = {
      val fileName = "app.log"
      val fw       = new FileWriter(fileName, true)
      try {
        fw.write("\n" ++ str)
      } finally fw.close()
    }

    override def apply[A](fa: LogDSL[A]): Task[A] = fa match {
      case LogDSL.Info(message) =>
        Task {
          appendToFile(message)
        }
      case LogDSL.Nothing(a) => Task { a }
    }
  }

  val log2task: LogDSL ~> Task = new (LogDSL ~> Task) {
    def appendToFile(str: String) = {
      val fileName = "app.log"
      val fw       = new FileWriter(fileName, true)
      try {
        fw.write("\n" ++ str)
      } finally fw.close()
    }

    override def apply[A](fa: LogDSL[A]): Task[A] = fa match {
      case LogDSL.Info(message) =>
        Task {
          appendToFile(message)
        }
      case LogDSL.Nothing(a) => Task { a }
    }
  }

  val httpDsl2Task = new (HttpDSL ~> Task) {

    def apply[A](fa: HttpDSL[A]): Task[A] = fa match {
      case HttpDSL.HttpReq(url)  => Task(getHttpPage(url))
      case HttpDSL.Parse(html)   => Task(pageParse(html))
      case HttpDSL.JustReturn(a) => Task(a)
    }
  }

  val httpDsl2log = {
    new (HttpDSL ~< Const[LogDSL, Unit, ?]) {
      override def apply[A](fa: HttpDSL[A]): Free[Const[LogDSL, Unit, ?], A] = {
        def logInfo(string: String): Free[Const[LogDSL, Unit, ?], A] =
          Free.liftF[Const[LogDSL, Unit, ?], A](LogDSL.Info(string))

        def nothing(): Free[Const[LogDSL, Unit, ?], A] =
          Free.liftF[Const[LogDSL, Unit, ?], A](LogDSL.Nothing(()))

        fa match {
          case HttpDSL.HttpReq(url) => logInfo(s"url of request: $url")
          case HttpDSL.Parse(str) =>
            logInfo(s"string to parse ${str.substring(0, math.min(str.length, 100))}")
          case _ => nothing()
        }
      }
    }
  }

  // it merging http2log and http2task by ignoring (http2log->log2task) result
  val httpWLog2Task = new (HttpDSL ~> Task) {
    override def apply[A](fa: HttpDSL[A]): Task[A] = {
      for {
        _ <- httpDsl2log(fa).unconst.foldMap(logDsl2Task)
        r <- httpDsl2Task(fa)
      } yield r
    }
  }

}
