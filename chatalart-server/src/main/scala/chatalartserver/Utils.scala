package chatalartserver

import com.typesafe.config.{Config, ConfigFactory}

object Utils {

  val conf: Config = ConfigFactory.load

  val chatDomain: String = conf.getString("chat.domain")

  val token: String = conf.getString("chat.token")
}
