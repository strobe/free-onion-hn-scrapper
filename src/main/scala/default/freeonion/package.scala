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

package default

import cats.free.Free
import cats.{ Functor, ~> }

/**
  * Created by Evgeniy Tokarev on 26/11/2016.
  */
package object freeOnionHNScrapper {

  type Interpreter[F[_], G[_]] = F ~> Free[G, ?]
  type ~<[F[_], G[_]]          = Interpreter[F, G]
  type Const[F[_], Out, A]     = F[Out] // is a wrapper type provided explicit out type for free dsl interpreter

  // evidence that Const type is a functor
  implicit def constFunctor[F[_], Out]: Functor[Const[F, Out, ?]] =
    new Functor[Const[F, Out, ?]] {
      override def map[A, B](fa: Const[F, Out, A])(f: (A) => B): Const[F, Out, B] = fa
    }

  // it maps `Free[Const[F, Unit, ?], A]` to `Free[F, Unit]`
  implicit class FreeConstOps[F[_], A](free: Free[Const[F, Unit, ?], A]) {
    def unconst: Free[F, Unit] =
      free.fold(x => Free.pure(()), Free.liftF(_))
  }

}
