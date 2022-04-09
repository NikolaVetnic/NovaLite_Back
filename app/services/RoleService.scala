package services

import dao.RoleDao
import models.Role

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RoleService @Inject()(dao: RoleDao)(implicit ex: ExecutionContext) {

  def exists(id: Long): Future[Boolean] = {
    dao.exists(id)
  }

  def getAll(): Future[Seq[Role]] = {
    dao.all()
  }

  def get(id: Long): Future[Option[Role]] = {
    dao.get(id)
  }
}
