package services

import java.util.UUID
import javax.inject.Inject
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

import models.Product

class ProductsService @Inject()(implicit ec: ExecutionContext) {

  private val products = TrieMap.empty[String, Product]

  def all(): Future[Seq[Product]] = Future { products.values.toSeq }

  def findById(id: String): Future[Option[Product]] = Future {
    products.get(id)
  }

  def create(name: String): Future[String] = Future {
    val id = randomId()
    products += (id -> Product(id = id, name = name))
    id
  }

  def remove(id: String): Future[Unit] = Future { products -= id }

  private def randomId() = UUID.randomUUID.toString.take(6)

}