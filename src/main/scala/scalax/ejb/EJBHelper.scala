package scalax.ejb

import _root_.javax.{ejb => j2ee}
import _root_.javax.{transaction => jta }

import transactions.{ejb => ejb_transactions}

trait EJBHelper {
   def context : j2ee.EJBContext
   
   
   def transaction = ejb_transactions.transaction(context)
   
   
}
import _root_.javax.{persistence => jpa}

trait EJBEmHelper {
    def entityManager : jpa.EntityManager
    
    
    def ejb_query(sql : String) = new QueryHolder {
      protected def query = entityManager.createQuery(sql) 
    }
    
    def ejb_query_single[A](sql : String) = new SingleQueryHolder[A] {
      protected def query = entityManager.createQuery(sql) 
    } 
    
    def ejb_update(sql : String) = new UpdateHolder {
      protected def query = entityManager.createQuery(sql)
    }
    
    trait BaseQueryHolder[ReturnType] {
      /** The query we're manipulating */
      protected def query : jpa.Query
      /** Overridden by out subclass for the correct method of executing a query */
      protected def performQuery(query : jpa.Query) : ReturnType
      /** The bound parameters for placeholders */
      private var parameters = new collection.mutable.ListBuffer[Any]()
      
      /** Binds a value to a parameter placehodler (?) */
      def %[A <: Any](x : A) = {
        parameters.append(x)
        this
      }
      
      
      def foreach(f : ReturnType => Unit) : Unit =  map(f)      
      def flatMap[B](f : ReturnType => Iterable[B]) = map(f)
      /** Takes results of this query and maps them to some other type */
      def map[B](f : ReturnType => B) : Seq[B] = {
        val q = query
        //TODO - Validate queries?
        for( (p, idx) <- parameters.toList.zipWithIndex) {
           query.setParameter(idx+1, p)
        }
        import collection.jcl.Conversions._
        f(performQuery(q)) :: Nil
      }
    }
    /** A trait that will hold regular ejb queries */
    trait QueryHolder extends BaseQueryHolder[Seq[_]] {       
      import collection.jcl.Conversions._
      protected def performQuery(query : jpa.Query) : Seq[_] = convertList(query.getResultList())      
    }
    /** A trait that will hold ejb "single result" queries */
    trait SingleQueryHolder[A] extends BaseQueryHolder[A] {
      protected def performQuery(query : jpa.Query) : A = query.getSingleResult().asInstanceOf[A]
    }
    /** A trait that will hold ejb updates */
    trait UpdateHolder extends BaseQueryHolder[Long] {
      protected def performQuery(query : jpa.Query) : Long = query.executeUpdate()
    }
    
}