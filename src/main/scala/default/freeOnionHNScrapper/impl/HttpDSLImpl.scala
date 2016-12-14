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

import java.io.StringReader

import cats.data.NonEmptyList
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.util.{ Success, Try, Failure }
import scala.xml.Elem
import scalaj.http._
import org.ccil.cowan.tagsoup
import scala.xml.Node
import scala.collection.JavaConverters._
import default.freeOnionHNScrapper.model.DataModel._

/**
  * Created by Evgeniy Tokarev on 26/11/2016.
  */
trait HttpDSLImpl {

  def getHttpPage(url: String): String = {
    val response: HttpResponse[String] = Http(url).asString
    response.body
  }

  // TODO: proper error handing should implemented here
  def pageParse(rawHtml: String): NonEmptyList[PieceOfNews] = {
    // html parsing for cleaning by TagSoup
    def cleanHtml(html: String): Node = {
      lazy val parserFactory = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
      lazy val parser        = parserFactory.newSAXParser()
      lazy val adapter       = new scala.xml.parsing.NoBindingFactoryAdapter

      val source        = new org.xml.sax.InputSource(new StringReader(html))
      val content: Node = adapter.loadXML(source, parser)
      content
    }

    val dom               = cleanHtml(rawHtml)
    val cleanedHtmlString = dom.toString()

    val doc: Document       = Jsoup.parse(cleanedHtmlString, "UTF-8")
    val itemsList: Elements = doc.select(".itemlist > tbody")

    val athing: Elements  = itemsList.select(".athing")
    val subtext: Elements = itemsList.select(".subtext")

    val items: List[(Element, Element)] =
      athing.asScala.toList zip subtext.asScala.toList

    val news = for (i <- items) yield {
      val storylink = Try(i._1.select(".storylink"))
      val scoreElem = Try(i._2.select(".score"))

      val link = storylink.flatMap(x => Try(x.attr("href")))
      val name = storylink.flatMap(x => Try(x.text()))

      val scoreText = scoreElem.flatMap(x => Try(x.text()))

      val score = scoreText match {
        case Success(v) => {
          if (v.contains(" points"))
            v.replace(" points", "").toInt
          else if (v.contains(" point"))
            v.replace(" point", "").toInt
          else {
            0 // TODO: - is not found "point/s" case
          }
        }
        case Failure(e) =>
          println("Unable get Score value, error: " + e.getMessage)
          0
      }

      for {
        n <- name
        l <- link
      } yield PieceOfNews(n, l, score)
    }

    val r = news.filter(_.isSuccess).map(_.get)
    if (r.nonEmpty)
      NonEmptyList.fromListUnsafe(r)
    else
      NonEmptyList.of(PieceOfNews("test", "url", 111))
  }

}
