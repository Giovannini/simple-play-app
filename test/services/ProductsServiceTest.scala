package services

import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.ExecutionContext.Implicits.global

import models.Product

class ProductsServiceTest extends WordSpec with ScalaFutures {

  "create" should {
    "add a new product to the list of products" in new Fixtures {
      whenReady(for {
        productId <- productService.create(productName1)
        all <- productService.all()
      } yield (productId, all)) { case (id, allProducts) =>
        assert(allProducts == Seq(Product(id, productName1)))
      }
    }

    "add several new products to the list of products" in new Fixtures {
      whenReady(for {
        productId1 <- productService.create(productName1)
        productId2 <- productService.create(productName2)
        all <- productService.all()
      } yield (productId1, productId2, all)) { case (id1, id2, allProducts) =>
        assert(allProducts.contains(Product(id1, productName1)))
        assert(allProducts.contains(Product(id2, productName2)))
      }
    }
  }


  "findById" should {
    "do not find a product that does not exist in an empty list" in new
        Fixtures {
      whenReady(for {
        maybeProduct <- productService.findById("abc")
      } yield maybeProduct) { maybeProduct =>
        assert(maybeProduct.isEmpty)
      }
    }

    "do not find a product that does not exist in a non empty list" in new
        Fixtures {
      whenReady(for {
        _ <- productService.create(productName1)
        maybeProduct <- productService.findById("abc")
      } yield maybeProduct) { maybeProduct =>
        assert(maybeProduct.isEmpty)
      }
    }

    "find an existing product in a list with only this element" in new
        Fixtures {
      whenReady(for {
        productId <- productService.create(productName1)
        maybeProduct <- productService.findById(productId)
      } yield maybeProduct) { maybeProduct =>
        assert(maybeProduct.nonEmpty)
      }
    }

    "find an existing product in a list with multiple elements" in new
        Fixtures {
      whenReady(for {
        _ <- productService.create(productName1)
        productId <- productService.create(productName2)
        maybeProduct <- productService.findById(productId)
      } yield maybeProduct) { maybeProduct =>
        assert(maybeProduct.nonEmpty)
      }
    }
  }

  "remove" should {
    "remove a new product to the list of products" in new Fixtures {
      whenReady(for {
        productId <- productService.create(productName1)
        _ <- productService.remove(productId)
        all <- productService.all()
      } yield all) { allProducts =>
        assert(allProducts.isEmpty)
      }
    }

    "remove a single product in a list of multiple products" in new Fixtures {
      whenReady(for {
        productId1 <- productService.create(productName1)
        productId2 <- productService.create(productName2)
        _ <- productService.remove(productId2)
        all <- productService.all()
      } yield (productId1, productId2, all)) { case (id1, id2, allProducts) =>
        assert(allProducts.contains(Product(id1, productName1)))
        assert(!allProducts.contains(Product(id2, productName2)))
      }
    }
  }

}

trait Fixtures {
  val productService = new ProductsService()
  val productName1 = "Livre"
  val productName2 = "Tablette"
}
