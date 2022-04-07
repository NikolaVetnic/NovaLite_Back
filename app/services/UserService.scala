package services

import dao.UserDao

import javax.inject.Inject
import models.User

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UserService @Inject()(dao: UserDao)(implicit ex: ExecutionContext) {

  def existsId(id: Long): Future[Boolean] = {
    dao.existsId(id)
  }

  def existsUsername(username: String): Future[Boolean] = {
    dao.existsUsername(username)
  }

  def get(id: Long): Future[Option[User]] = {
    dao.get(id)
  }

  def getAll(): Future[Seq[User]] = {
    dao.all()
  }

  def create(user: User): Future[Object] = {
    existsUsername(user.username).map {
      case true =>
        Failure
      case false =>
        dao.insert(user)
        Success
    }
  }

  def update(user: User): Future[Object] = {
    existsId(user.id.get).map {
      case true =>
        dao.update(user)
        Success
      case false =>
        Failure
    }
  }

  def delete(id: Long): Future[Unit] = {
    dao.delete(id)
  }
}
