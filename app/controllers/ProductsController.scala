package controllers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import javax.inject._
import play.api.libs.json.{JsString, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.ProductsService

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ProductsController @Inject()(
  cc: ControllerComponents,
  productsService: ProductsService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def list: Action[AnyContent] = Action.async {
    productsService.all().map { products =>
      Ok(products.mkString(", "))
    }
  }

  def lookup(id: String): Action[AnyContent] = Action.async {
    productsService.findById(id).map {
      case Some(product) => Ok(product.toString)
      case None => NotFound
    }
  }

  def create: Action[JsValue] = Action.async(parse.json) { request =>
    val json = request.body
    (json \ "name").toOption match {
      case Some(JsString(name)) =>
        productsService.create(name).map { productId =>
          Created(Json.obj("productId" -> productId))
        }.recover { case NonFatal(_) =>
          InternalServerError("An error occured during product creation.")
        }
      case _ => Future.successful(
        BadRequest("Expected a JSON value with field 'name' to be a string.")
      )
    }
  }

}
