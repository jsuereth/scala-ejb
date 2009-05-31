package scalax.ejb.transactions

import _root_.javax.{ejb => j2ee}
import _root_.javax.{transaction => jta }

object ejb {
  
   /**
    * This method converts an EJB context into a wrapper that can be used to 
    */  
   def transaction(ctx : j2ee.EJBContext) = new EjbContextWrapper {
     val context = ctx
   }
}



/**
 * This trait provides methods that allow easier control of transactions with an EJB
 */
trait EjbContextWrapper {
  val context : j2ee.EJBContext 

  /** This class lets us pass around ejb transaction objects as "undoable actions" */
  private class TransactionWrapper(val tx : javax.transaction.UserTransaction) extends UndoableAction {
     override def commit() = tx.commit()     
     override def rollback() = tx.rollback()
     override def retry() = throw new RedoException
  }  
  
  private class RedoException extends Exception
  
  def foreach(f : UndoableAction => Unit) : Unit = map(f)
  
  def flatMap[A](f : UndoableAction => Iterable[A]) : Iterable[A] = map(f)
  
  def map[A](f : UndoableAction => A) : A = {
    /** This method performs a transaction */
    def performTransaction() : A =  {
       val tx  = context.getUserTransaction
        try {
         tx.begin();
         val result = f(new TransactionWrapper(tx))
         //TODO - Check for internal commits...?
         tx.commit();
         result
        } catch {
          case e : Exception =>
             try {
                tx.rollback();
             } catch  {
               case x : jta.SystemException =>
                 throw new j2ee.EJBException("Rollback failed: " + x.getMessage())
             }
             throw e
       }
    }
    /** This method re-attempts a transaction if it catches a RedoException */
    def redoPerform() : A = {
      try {
        performTransaction()
      } catch {
        case redo : RedoException =>
          //TODO - Limit retries?
          redoPerform()
      }
    }
    redoPerform()
  }
  
  
}
