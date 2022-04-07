package services

import dao.PostDao
import models.Post

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostService @Inject()(dao: PostDao)(implicit ex: ExecutionContext) {

  def get(id: Long): Future[Option[Post]] = {
    dao.get(id)
  }

  def getAll(): Future[Seq[Post]] = {
    dao.all()
  }

  def create(post: Post): Future[String] = {
    dao.insert(post)
  }

  def update(post: Post): Future[Post] = {
    dao.update(post)
  }

  def delete(id: Long): Future[Unit] = {
    dao.delete(id)
  }
}
