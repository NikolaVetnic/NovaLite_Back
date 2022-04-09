package services

import dao.{RoleDao, UserDao}

import javax.inject.Inject
import models.User
import utils.EStatus
import utils.EStatus.EStatus

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class UserService @Inject()(dao: UserDao, roleDao: RoleDao)(implicit ex: ExecutionContext) {

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

  def create(user: User): Future[EStatus] = {

    val checkUsername = Await.result(dao.existsUsername(user.username), Duration.Inf)
    val checkRole = Await.result(roleDao.exists(user.roleId), Duration.Inf)

    if (!checkUsername && checkRole)
      Future {
        dao.insert(user)
        EStatus.Success
      }
    else
      Future {
        EStatus.Failure
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
