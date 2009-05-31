package scalax.ejb.transactions

/**
 * This trait provides common "inside a transaction" methods.
 */
trait UndoableAction {
  def commit() : Unit
  def rollback() : Unit
}
