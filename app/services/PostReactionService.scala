package services

import dao.PostReactionDao
import models.PostReaction
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class PostReactionService @Inject()(
  dao: PostReactionDao,
  userService: UserService,
  postService: PostService,
  reactionService: ReactionService)(implicit ex: ExecutionContext) {


  /**********
   * EXISTS *
   **********/
  def exists(postReaction: PostReaction): Future[Boolean] = {
    dao.exists(postReaction.userId, postReaction.postId)
  }


  /*******
   * GET *
   *******/
  def getAll(): Future[Seq[PostReaction]] = {
    dao.all()
  }


  def getByUserId(id: Long): Future[Seq[PostReaction]] = {
    dao.getByUserId(id)
  }


  def getByPostId(id: Long): Future[Seq[PostReaction]] = {
    dao.getByPostId(id)
  }


  def getLikesByUserIdAndPostId(userId: Long, postId: Long): Future[Int] = {

    val res = for {
      likes <- dao.getLikesByUserIdAndPostId(userId, postId)
    } yield likes.length

    res
  }


  def getByUserIdAndPostId(userId: Long, postId: Long): Future[Option[PostReaction]] = {
    dao.getByUserIdAndPostId(userId, postId)
  }


  /********
   * POST *
   ********/
  def create(postReaction: PostReaction): Future[EStatus] = {

    val res = for {
      b0 <- postService.exists(postReaction.postId)
      b1 <- userService.existsId(postReaction.userId)
      b2 <- reactionService.exists(postReaction.reactionId)
    } yield b0 && b1 && b2

    res.map {
      case true => {
        dao.delete(postReaction.userId, postReaction.postId)
        dao.insert(postReaction)
        EStatus.Success
      }
      case false => {
        EStatus.Failure
      }
    }
  }


  /**********
   * DELETE *
   **********/
  def delete(userId: Long, postId: Long): Future[EStatus] = {
    dao.exists(userId, postId).map {
      case true =>
        dao.delete(userId, postId)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }
}
