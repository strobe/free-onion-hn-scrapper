# freeOnionHNScrapper #

Welcome to freeOnionHNScrapper!

## Overview

Is a toy app example which is inspired by:

- John A De Goes article "Modern Functional Programming: Part 2" http://degoes.net/articles/modern-fp-part-2

- implementation of that article ideas https://github.com/notxcain/onion-architecure

Basically this code doing Hacker News scrapping by getting list of news,
filtered it by score and saved result data to H2 db file (and logging some operation to file).

Goal of this project demonstrate composition of free monad DSLs at minimal reasonable practical context
(by using Cats and Freek libraries).

## Detailed Information

DSLs defined as:

```
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
```

Basic composition of DSLs looks like this (it produced CoProduct):

```
  type CoreAndDbDSL = CoreDSL :|: DbDSL :|: NilDSL
  val CoreAndDbDSL = DSL.Make[CoreAndDbDSL]
```

Free program defined as:

```
  def program(url: String, minScore: Int) = {
    for {
      list     <- CoreDSL.GetYCNews(url).freek[CoreAndDbDSL]
      filtered <- CoreDSL.Filter(list, _.points >= minScore).freek[CoreAndDbDSL]
      _        <- DbDSL.Save(filtered).freek[CoreAndDbDSL]
    } yield filtered
  }
```

Some part of app  may be implemented in terms of other Free program. Key for this is a
`tranpiler` interpreter with `replace` and `transpile` operations (provided by Freek lib.).

```
  val transpileNat = CopKNat[CoreAndDbDSL.Cop].replace(HttpSubProgram.transpiler)
  val freeProgram  = MainProgram.program("https://news.ycombinator.com", 10).transpile(transpileNat)

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
```

Interpreters defined like this:

```
  val dbDsl2Task = new (DbDSL ~> Task) {
    def apply[A](fa: DbDSL[A]): Task[A] = fa match {
      case DbDSL.Save(items) =>
        Task(saveHYNews(items))
    }
  }
```

Composition and usage of interpreters:

```
  val finalInterpreter                              = dbDsl2Task :&: httpWLog2Task
  val finalProgram: Task[NonEmptyList[PieceOfNews]] = freeProgram.interpret(finalInterpreter)
```

Different kind of interpreters composition is possible by ignoring/combining results of few interpreters inside new one:

```
  // it merging http2log and http2task by ignoring (http2log->log2task) result
  val httpWLog2Task = new (HttpDSL ~> Task) {
    override def apply[A](fa: HttpDSL[A]): Task[A] = {
      for {
        _ <- httpDsl2log(fa).unconst.foldMap(logDsl2Task)
        r <- httpDsl2Task(fa)
      } yield r
    }
  }
```

## H2 db web console

To view db content you may use ```sbt h2ConsoleTask``` which will launch web ui on some local port
(usually is a http://localhost:8082)
(use url like ```jdbc:h2:file:./db/default``` without username and password).

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).
