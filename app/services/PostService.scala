package services

import dao.PostDao
import models.{Post, PostInsertDto}
import utils.EStatus
import utils.EStatus.{EStatus, Success}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostService @Inject()(dao: PostDao, userService: UserService)(implicit ex: ExecutionContext) {


  def exists(id: Long): Future[Boolean] = {
    dao.exists(id)
  }


  def get(id: Long): Future[Option[Post]] = {
    dao.get(id)
  }


  def getByOwnerId(ownerId: Long): Future[Seq[Post]] = {
    dao.getByOwnerId(ownerId)
  }


  def getByIdAndOwnerId(id: Long, ownerId: Long): Future[Seq[Post]] = {
    dao.getByOwnerId(ownerId)
  }


  def getByTitleContentAndOwnerId(title: String, content: String, ownerId: Long): Future[Seq[Post]] = {
    dao.getByTitleContentAndOwnerId(title, content, ownerId)
  }


  def getAll(): Future[Seq[Post]] = {
    dao.all()
  }


  def create(postDto: PostInsertDto): Future[EStatus] = {
    userService.existsId(postDto.ownerId).map {
      case true =>
        dao.insert(postDto)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def update(post: Post): Future[EStatus] = {
    dao.existsAndOwnedBy(post.id.get, post.ownerId).map {
      case true =>
        dao.update(post)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def delete(id: Long): Future[EStatus] = {
    dao.exists(id).map {
      case true =>
        dao.delete(id)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def deleteByIdAndOwnerId(id: Long, ownerId: Long): Future[EStatus] = {
    dao.existsAndOwnedBy(id, ownerId).map {
      case true =>
        dao.deleteByIdAndOwnerId(id, ownerId)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }
}
