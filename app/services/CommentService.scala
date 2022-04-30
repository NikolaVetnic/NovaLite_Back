package services

import dao.CommentDao
import models.{Comment, CommentDisplayDto, CommentInsertDto}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentService @Inject()(
  dao: CommentDao,
  userService: UserService,
  commentReactionService: CommentReactionService)(implicit ex: ExecutionContext) {


  /**********
   * EXISTS *
   **********/
  def exists(id: Long): Future[Boolean] =
    dao.exists(id)


  /*******
   * GET *
   *******/
  def getAll(): Future[Seq[Comment]] = {
    dao.all()
  }


  def get(id: Long): Future[Option[Comment]] = {
    dao.get(id)
  }


  def getByPostId(id: Long): Future[Seq[CommentDisplayDto]] = {
    for {
      comments <- dao.getByPostId(id)
      users <- userService.getAll()
      likes <- commentReactionService.getAllLikes()
    } yield {
      comments.map { comment =>
        CommentDisplayDto(
          comment.id.get,
          comment.content,
          comment.dateTime.toLocalDateTime,
          users.filter(user => user.id.get == comment.ownerId).head,
          comment.postId,
          likes.filter(_.commentId == comment.id.get).size
        )
      }
    }
  }


  /********
   * POST *
   ********/
  def create(commentInsertDto: CommentInsertDto): Future[Option[Comment]] = {
    for {
      _ <- dao.insert(commentInsertDto)
      s <- dao.num()
      comments <- getAll()
    } yield comments.sortBy(_.id).drop(s - 1).find(c => true)
  }


  /*******
   * PUT *
   *******/
  def update(comment: Comment): Future[Option[Comment]] = {
    for {
      _ <- dao.update(comment)
      comment <- get(comment.id.get)
    } yield comment
  }


  def deleteByPostId(postId: Long): Future[EStatus] = {
    for {
      comments <- dao.getByPostId(postId)
    } yield {
      comments.map { c =>
        commentReactionService.deleteByCommentId(c.id.get)
        dao.delete(c.id.get)
      }
      EStatus.Success
    }
  }


  def delete(id: Long): Future[EStatus] = {
    for {
      b <- dao.exists(id)
      _ <- commentReactionService.deleteByCommentId(id)
    } yield {
      if (b) {
        dao.delete(id)
        EStatus.Success
      } else {
        EStatus.Failure
      }
    }
  }
}
