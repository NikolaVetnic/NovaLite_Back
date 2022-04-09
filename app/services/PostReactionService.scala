package services

import dao.PostReactionDao

import javax.inject.Inject
import models.PostReaction
import utils.EStatus
import utils.EStatus.EStatus

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PostReactionService @Inject()(
  dao: PostReactionDao,
  userService: UserService,
  postService: PostService,
  reactionService: ReactionService)(implicit ex: ExecutionContext) {

  def exists(postReaction: PostReaction): Future[Boolean] = {
    dao.exists(postReaction.userId, postReaction.postId)
  }

  def getAll(): Future[Seq[PostReaction]] = {
    dao.all()
  }

  def getByPostId(postId: Long): Future[Seq[PostReaction]] = {
    dao.getByPostId(postId)
  }

  def getByUserIdAndPostId(userId: Long, postId: Long): Future[Option[PostReaction]] = {
    dao.getByUserIdAndPostId(userId, postId)
  }

  def create(postReaction: PostReaction): Future[EStatus] = {

    val checkPost = Await.result(postService.exists(postReaction.postId), Duration.Inf)
    val checkUser = Await.result(userService.existsId(postReaction.userId), Duration.Inf)
    val checkReaction = Await.result(reactionService.exists(postReaction.reactionId), Duration.Inf)

    if (checkPost && checkUser && checkReaction)
      Future {
        dao.delete(postReaction.userId, postReaction.postId)
        dao.insert(postReaction)
        EStatus.Success
      }
    else
      Future { EStatus.Failure }
  }

  def delete(userId: Long, postId: Long): Future[Unit] = {
    dao.delete(userId, postId)
  }
}
