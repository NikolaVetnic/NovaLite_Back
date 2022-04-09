package utils

import play.api.libs.json.Json

object ErrorMsg {

  val ID_NOT_FOUND_ERROR =
    Json.obj("idNotFoundError" -> "Object with input ID not found.")
  val USERNAME_FOUND_ERROR =
    Json.obj("usernameFoundError" -> "User with input username already exists.")
  val ID_NOT_FOUND_OR_USERNAME_FOUND_ERROR =
    Json.obj("idNotFoundOrUsernameFoundError" -> "User with input ID not found or input username already exists.")
  val UPDATED_OBJECT_NOT_FOUND_ERROR =
    Json.obj("updatedObjectNotFoundError" -> "Updated object not found.")
  val RELATED_DB_ENTRY_NOT_FOUND_ERROR =
    Json.obj("dbEntryNotFoundError" -> "At least one related DB entry not found.")
  val OBJECT_NOT_CREATED_ERROR =
    Json.obj("objectNotCreatedError" -> "Forwarded object was not persisted.")
}
