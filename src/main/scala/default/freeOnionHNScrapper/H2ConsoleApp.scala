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

import org.h2.tools.Server
import org.h2.tools.Console
import org.h2.Driver

/**
  * Created by Evgeniy Tokarev on 14/12/2016.
  */
object H2ConsoleApp extends App {
  val server: Server = Server.createTcpServer().start()
  Console.main("-web")
}
