package services

import dao.{RoleDao, UserDao}
import models.User
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(dao: UserDao)(implicit ex: ExecutionContext) {


  def existsId(id: Long): Future[Boolean] = {
    dao.existsId(id)
  }


  def existsUsername(username: String): Future[Boolean] = {
    dao.existsUsername(username)
  }


  def isValidLogin(username: String, password: String): Future[Boolean] = {
    dao.isValidLogin(username, password)
  }


  def get(id: Long): Future[Option[User]] = {
    dao.get(id)
  }


  def getByUsername(username: String): Future[Option[User]] = {
    dao.getByUsername(username)
  }


  def getAll(): Future[Seq[User]] = {
    dao.all()
  }


  def create(user: User): Future[User] = {
    for {
      _ <- dao.insert(user)
      users <- dao.all()
    } yield users.sortBy(_.id).last
  }


  def update(user: User): Future[Option[User]] = {
    for {
      _ <- dao.update(user)
      user <- get(user.id.get)
    } yield user
  }


  def update2(user: User): Future[EStatus] = {
    existsId(user.id.get).map {
      case true =>
        dao.update(user)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def delete(id: Long): Future[EStatus] = {

    val res = for {
      b <- dao.existsId(id)
      _ <- dao.delete(id)
    } yield b

    res.map {
      case true => EStatus.Success
      case false => EStatus.Failure
    }
  }
}
