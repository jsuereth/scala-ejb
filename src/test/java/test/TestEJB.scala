package test

import _root_.javax.ejb.EJBContext
import transactions._
import ejb.ejb._

//TODO - EasyMock this up!


class TestEJB {
   private var context : EJBContext = _
   
   
   def mustStartAndCommitTransaction() {
     var x = 0     
     for(tx <- transaction(context)) {
       //Let's do stuff!
       x = 1
     }
     
     
     //TODO _ Assert x == 1 and all transaction methods were called!
   }
}

import _root_.scalax.ejb._

class MyBean {
  
}

class TestEJB2 extends EJBEmHelper with EJBHelper {
   override def entityManager = null
   override def context = null
   
   def testQuery(id : Long) = {
      (for {
        tx <- transaction
        result <- ejb_query_single[MyBean]("from  MyBean where MyBean.id = ?") % id 
      } yield result).toSeq.first
   }
   
   def testUpdate(id : Long, value : Double) = {
     for {
       tx <- transaction
       result <- ejb_update("update MyBean set MyBean.value = ? where MyBean.id = ?") % value % id
     } yield result
   }
}
