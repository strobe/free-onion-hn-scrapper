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
import cats.free.Free
import cats.~>

import freek._
import default.freeOnionHNScrapper.model.DataModel._

/**
  * Created by Evgeniy Tokarev on 26/11/2016.
  */
object HttpSubProgram {

  sealed trait HttpDSL[A]
  object HttpDSL {
    case class HttpReq(url: String)                     extends HttpDSL[String]
    case class Parse(s: String)                         extends HttpDSL[NonEmptyList[PieceOfNews]]
    case class JustReturn(a: NonEmptyList[PieceOfNews]) extends HttpDSL[NonEmptyList[PieceOfNews]]
  }

  sealed trait LogDSL[A]
  object LogDSL {
    case class Info[A](message: String) extends LogDSL[Unit]
    case class Nothing[A](a: A)         extends LogDSL[A]
  }

  import HttpDSL._
  import MainProgram.CoreDSL

  type HTTPDSL = HttpDSL :|: NilDSL
  val HTTPDSL = DSL.Make[HTTPDSL]

  // this is our transpiler transforming a CoreDSL into another free program
  val transpiler = new (CoreDSL ~> Free[HTTPDSL.Cop, ?]) {

    def apply[A](f: CoreDSL[A]): Free[HTTPDSL.Cop, A] = f match {
      case CoreDSL.GetYCNews(url) =>
        for {
          s <- HttpReq(url).freek[HTTPDSL]
          r <- Parse(s).freek[HTTPDSL]
        } yield r

      case CoreDSL.Filter(l, p) =>
        for {
          r <- JustReturn(NonEmptyList.fromListUnsafe(l.filter(p))).freek[HTTPDSL]
        } yield r
    }
  }

}
