package io.github.gustibimo.zioWeb

import doobie.util.transactor.Transactor
import io.github.gustibimo.zioWeb.dao.{StockDAO, StockDAOLive}
import zio.Task
import zio.clock.Clock
import zio.interop.catz._

trait ExtServices extends Clock {
  val stockDao: StockDAO
}

object ExtServicesLive extends ExtServices with Clock.Live  {

  val xa = Transactor.fromDriverManager[Task](
    "org.h2.Driver",
    "jdbc:h2:mem:poc;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
    , "sa", ""
  )
  override val stockDao: StockDAO = new StockDAOLive(xa)
}
