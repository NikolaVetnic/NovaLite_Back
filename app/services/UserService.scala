package services

import dao.UserDAO
import javax.inject.Inject
import models.User

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(dao: UserDAO)(implicit ex: ExecutionContext) {

  def get(id: Long): Future[Option[User]] = {
    dao.get(id)
  }

  def getAll(): Future[Seq[User]] = {
    dao.all()
  }

  def create(user: User): Future[String] = {
    dao.insert(user)
  }

  def update(user: User): Future[User] = {
    dao.update(user)
  }

  def delete(id: Long): Future[Unit] = {
    dao.delete(id)
  }
}
