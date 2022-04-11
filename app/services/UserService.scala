package services

import dao.{RoleDao, UserDao}
import models.User
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(dao: UserDao, roleDao: RoleDao)(implicit ex: ExecutionContext) {

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

  def create(user: User): Future[EStatus] = {

    val res = for {
      b0 <- dao.existsUsername(user.username)
      b1 <- roleDao.exists(user.roleId)
    } yield b0 && b1

    res.map {
      case true => {
        dao.insert(user)
        EStatus.Success
      }
      case false => {
        EStatus.Failure
      }
    }
  }

  def update(user: User): Future[EStatus] = {
    existsId(user.id.get).map {
      case true =>
        dao.update(user)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }

  def delete(id: Long): Future[Unit] = {
    dao.delete(id)
  }
}
