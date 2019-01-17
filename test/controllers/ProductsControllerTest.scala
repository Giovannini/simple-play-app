package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import models.Product
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import services.ProductsService

class ProductsControllerTest extends WordSpec with Results with MockitoSugar {
    import play.api.test.Helpers._
    import play.api.test._

    "list" should {
        "succeed if list returned by service is empty" in new Fixtures {
            // Given
            val productList: Nil.type = Nil

            // When
            when(productService.all()).thenReturn(Future.successful(productList))

            // Then
            val result: Future[Result] = controller.list.apply(FakeRequest())
            assert(contentAsString(result) == "")
            assert(status(result) == OK)
        }

        "succeed if list returned by service has one element" in new Fixtures {
            // Given
            val productList: Seq[Product] = Seq(Product("1", "iPhone"))

            // When
            when(productService.all()).thenReturn(Future.successful(productList))

            // Then
            val result: Future[Result] = controller.list.apply(FakeRequest())
            assert(contentAsString(result) == "Product(1,iPhone)")
            assert(status(result) == OK)
        }

        "succeed if list returned by service has 3 element" in new Fixtures {
            // Given
            val productList: Seq[Product] = Seq(
                Product("1", "iPhone"),
                Product("2", "Aspirateur"),
                Product("3", "Pantoufle")
            )

            // When
            when(productService.all()).thenReturn(Future.successful(productList))

            // Then
            val result: Future[Result] = controller.list.apply(FakeRequest())
            assert(contentAsString(result) == productList.mkString(", "))
            assert(status(result) == OK)
        }
    }

    "lookup" should {
        "return a OK with the product if it is found" in new Fixtures {
            // Given
            val productId = "1"
            val product: Product = Product(productId, "iPhone")

            // When
            when(productService.findById(productId))
              .thenReturn(Future.successful(Some(product)))

            // Then
            val result: Future[Result] =
                controller.lookup(productId).apply(FakeRequest())
            assert(contentAsString(result) == product.toString)
            assert(status(result) == OK)
        }

        "return a NotFound if no product is found" in new Fixtures {
            // Given
            val productId = "1"

            // When
            when(productService.findById(productId))
              .thenReturn(Future.successful(None))

            // Then
            val result: Future[Result] =
                controller.lookup(productId).apply(FakeRequest())
            assert(status(result) == NOT_FOUND)
        }
    }

    "create" should {
        "return a Created with product id if request is a success" in new Fixtures {
            // Given
            val productName = "iPhone"
            val expectedProductId = "1"
            val json = Json.obj("name" -> productName)

            // When
            when(productService.create(productName))
              .thenReturn(Future.successful(expectedProductId))

            // Then
            val result: Future[Result] =
                controller.create.apply(FakeRequest().withBody(json))
            assert(contentAsJson(result) == Json.obj("productId" -> expectedProductId))
            assert(status(result) == CREATED)
        }

        "return a 500 if an error occured inside the service" in new Fixtures {
            // Given
            val productName = "iPhone"
            val expectedProductId = "1"
            val json = Json.obj("name" -> productName)

            // When
            when(productService.create(productName))
              .thenReturn(Future.failed(new Exception("TEST")))

            // Then
            val result: Future[Result] =
                controller.create.apply(FakeRequest().withBody(json))
            assert(status(result) == INTERNAL_SERVER_ERROR)
        }

        "return a BadRequest is request does not contain a name" in new
            Fixtures {
            // Given
            val productName = "iPhone"
            val json = Json.obj("not_a_name" -> productName)

            // Then
            val result: Future[Result] =
                controller.create.apply(FakeRequest().withBody(json))
            assert(status(result) == BAD_REQUEST)
        }
    }

    trait Fixtures {
        val productService: ProductsService = mock[ProductsService]
        val controller = new ProductsController(stubControllerComponents(), productService)
    }
}
