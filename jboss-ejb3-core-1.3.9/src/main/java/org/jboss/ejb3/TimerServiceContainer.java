/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3;

import org.jboss.aop.Domain;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory_2;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

import javax.ejb.Timeout;
import javax.ejb.TimerService;
import java.util.Hashtable;

/**
 * A {@link EJBContainer} which contains the common functionality around EJB3 timer service.
 * 
 * <p>
 *  {@link TimerServiceContainer} is responsible for creating, restoring and suspending {@link TimerService}
 *  at the right time during {@link EJBContainer} lifecycle events. The {@link TimerServiceContainer} knows
 *  about EJB3 spec rules and takes care of <i>not</i> creating, restoring or suspending timer service for
 *  stateful session beans.
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class TimerServiceContainer extends EJBContainer
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(TimerServiceContainer.class);

   /**
    * TimerService which will be created for this container
    */
   protected TimerService timerService;

   /**
    * {@link TimerServiceFactory} which will be used for interacting with the
    * timer service
    */
   private TimerServiceFactory timerServiceFactory;

   /**
    * Constructor 
    * 
    * @param name Name of the container
    * @param domain AOP domain for this container 
    * @param cl Classloader of this container
    * @param beanClassName The fully qualified class name of the EJB 
    * @param ejbName The name of the EJB
    * @param ctxProperties Naming context properties
    * @param deployment Deployment, <i>can</i> be null
    * @param beanMetaData Metadata of the EJB
    * @throws ClassNotFoundException
    */
   protected TimerServiceContainer(String name, Domain domain, ClassLoader cl, String beanClassName, String ejbName,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossEnterpriseBeanMetaData beanMetaData)
         throws ClassNotFoundException
   {
      super(name, domain, cl, beanClassName, ejbName, ctxProperties, deployment, beanMetaData);

   }

   /**
    *  Creates a {@link TimerService} for this container. 
    * {@link TimerService} creation is skipped for stateful session beans, since 
    * stateful session beans do not support timerservice
    * 
    * @see EJBContainer#lockedStart()
    */
   @Override
   protected void lockedStart() throws Exception
   {
      try
      {
         super.lockedStart();
         // for non-stateful beans, create timer service
         if (this.isStatefulBean() == false)
         {
            // just create the timerservice. Restoring of
            // any timers, will be done in afterStart(), once the container has fully started
            // (to allow for timeout method invocations)
            this.timerService = this.createTimerService();
         }

      }
      catch (Exception e)
      {
         try
         {
            this.lockedStop();
         }
         catch (Exception ignore)
         {
            logger.debug("Failed to cleanup after start() failure", ignore);
         }
         throw e;
      }
   }

   /**
    * Restores the timers after this container has fully started, thus
    * ensuring that any invocations on this container through the restored
    * timers are handled successfully
    * <p>
    * This method skips timerservice restoration of stateful session beans
    * </p>
    * 
    * @see org.jboss.ejb3.EJBContainer#afterStart()
    */
   @Override
   protected void afterStart()
   {
      super.afterStart();
      // restore timerservice for non-stateful beans
      if (this.isStatefulBean() == false)
      {
         restoreTimerService();
      }
   }

   /**
    * Suspends the {@link TimerService} associated with this container.
    * <p>
    * This method skips timerservice suspension of stateful session beans
    * </p>
    * 
    * @see EJBContainer#lockedStop()
    */
   @Override
   protected void lockedStop() throws Exception
   {
      if (this.timerService != null)
      {
         this.timerServiceFactory.suspendTimerService(timerService);
         this.timerService = null;
      }

      super.lockedStop();
   }

   /**
    * Returns the {@link TimerService} associated with this container.
    * 
    * @throws UnsupportedOperationException If this container corresponds to a stateful session bean
    */
   @Override
   public TimerService getTimerService()
   {
      if (this.isStatefulBean())
      {
         throw new UnsupportedOperationException("stateful bean doesn't support TimerService (EJB3 18.2#2)");
      }
      return this.timerService;
   }

   /**
    * Returns the {@link TimerService} associated with this container.
    * 
    * @throws UnsupportedOperationException If this container corresponds to a stateful session bean
    */
   // hmm, what exactly is this method for?
   @Override
   public TimerService getTimerService(Object pKey)
   {
      return this.getTimerService();
   }

   /**
    * Set the {@link TimerServiceFactory}, which will be used for managing 
    * the {@link TimerService} associated with this container
    * 
    * @param factory 
    */
   @Inject
   public void setTimerServiceFactory(TimerServiceFactory factory)
   {
      this.timerServiceFactory = factory;
   }

   /**
    * Returns the {@link TimedObjectInvoker} which will be used by the  
    * timer implementations to invoke the timeout method (annotated with {@link Timeout}
    * or specified in deployment descriptor) on the bean.
    *  
    * @return
    */
   protected abstract TimedObjectInvoker getTimedObjectInvoker();

   /**
    * Returns true if this {@link TimerServiceContainer} belongs to a stateful bean.
    * Else returns false
    * @return
    */
   private boolean isStatefulBean()
   {
      JBossEnterpriseBeanMetaData enterpriseBeanMetaData = this.xml;
      if (enterpriseBeanMetaData.isSession() == false)
      {
         return false;
      }
      JBossSessionBeanMetaData sessionBean = (JBossSessionBeanMetaData) enterpriseBeanMetaData;
      return sessionBean.isStateful();
   }

   /**
    * Creates and returns a {@link TimerService}. Uses the {@link #timerServiceFactory} for creating the
    * timer service.  
    * @return
    */
   private TimerService createTimerService()
   {
      // get the TimedObjectInvoker
      TimedObjectInvoker timedObjectInvoker = this.getTimedObjectInvoker();
      // if there's no TimedObjectInvoker, we can't do anything, so just
      // throw an exception
      if (timedObjectInvoker == null)
      {
         throw new IllegalStateException("Cannot create timerservice for EJB " + this.getEjbName()
               + " since there's no TimedObjectInvoker");
      }
      // create and return the timerservice
      return this.timerServiceFactory.createTimerService(timedObjectInvoker);
   }

   private void restoreTimerService()
   {
      if(timerServiceFactory instanceof TimerServiceFactory_2)
         ((TimerServiceFactory_2) timerServiceFactory).restoreTimerService(timerService, classloader);
      else
      {
         logger.warn("EJBHREE-2193: using deprecated TimerServiceFactory for restoring timers");
         timerServiceFactory.restoreTimerService(timerService);
      }
   }
}
