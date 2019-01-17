package services

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class PreferencesService @Inject()(implicit ec: ExecutionContext) {

  private val stars = TrieMap.empty[String, Set[String]]

  // not pretty but does the trick
  def star(userId: String, productId: String): Future[Unit] = Future {
    val starred = stars.getOrElse(userId, Set.empty) + productId
    stars += (userId -> starred)
  }

  def unstar(userId: String, productId: String): Future[Unit] = Future {
    val starred = stars.getOrElse(userId, Set.empty) - productId
    stars += (userId -> starred)
  }

  def starred(userId: String): Future[Set[String]] = Future {
    stars.getOrElse(userId, Set.empty)
  }

}