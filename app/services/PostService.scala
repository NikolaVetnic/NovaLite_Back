package services

import dao.PostDao
import models.Post

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PostService @Inject()(dao: PostDao, userService: UserService)(implicit ex: ExecutionContext) {

  def get(id: Long): Future[Option[Post]] = {
    dao.get(id)
  }

  def getAll(): Future[Seq[Post]] = {
    dao.all()
  }

  def create(post: Post): Future[Object] = {
    userService.exists(post.ownerId).map {
      case true =>
        dao.insert(post)
        Success
      case false =>
        Failure
    }
  }

  def update(post: Post): Future[Object] = {
    dao.exists(post.id).map {
      case true =>
        dao.update(post)
        Success
      case false =>
        Failure
    }
  }

  def delete(id: Long): Future[Unit] = {
    dao.delete(id)
  }
}
