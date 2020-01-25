package io.github.gustibimo.zioWeb.dao

import io.github.gustibimo.zioWeb.IOTransactor
import io.github.gustibimo.zioWeb.model.{Stock, StockDBAccessError, StockError, StockNotFound}
import zio.IO
import doobie.implicits._
import zio.interop.catz._

trait StockDAO {
  def currentStock(stockId: Int): IO[StockError, Stock]
  def updateStock(stockId: Int, updateValue: Int): IO[StockError, Stock]
}


/**
 * The methods in this class are pure functions
 * They can describe how to interact with the database (select, insert, ...)
 * But as IO is lazy, no side effect will be executed here
 *
 * @param xa
 */

class StockDAOLive(val xa: IOTransactor) extends StockDAO{

  override def currentStock(stockId: Int): IO[StockError, Stock] = {
    val stockDatabaseResult = sql"""
      SELECT * FROM stock where id=$stockId
     """.query[Stock].option

    stockDatabaseResult.transact(xa).mapError(StockDBAccessError)
      .flatMap{
        case Some(stock) => IO.succeed(stock)
        case None => IO.fail(StockNotFound)
      }
  }

  override  def updateStock(stockId: Int, updateValue: Int): IO[StockError, Stock] = {
    val newStockDatabaseResult = for {
      _ <- sql""" UPDATE stock SET value = value + $updateValue where id=$stockId""".update.run
      newStock <- sql"""SELECT * FROM stock where id=$stockId""".query[Stock].unique
    } yield newStock

    newStockDatabaseResult.transact(xa).mapError(StockDBAccessError)
  }
}
