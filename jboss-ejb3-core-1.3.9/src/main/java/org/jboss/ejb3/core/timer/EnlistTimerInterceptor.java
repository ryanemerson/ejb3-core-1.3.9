/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
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
package org.jboss.ejb3.core.timer;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.aop.AbstractInterceptor;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.logging.Logger;

import javax.ejb.Timer;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import static org.jboss.ejb3.core.timer.TimerCallbackInvocationHelper.getTimer;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class EnlistTimerInterceptor extends AbstractInterceptor
{
   private static final Logger log = Logger.getLogger(EnlistTimerInterceptor.class);

   private final TransactionManager tm;

   public EnlistTimerInterceptor()
   {
      this.tm = TxUtil.getTransactionManager();
   }

   @Override
   public Object invoke(Invocation invocation) throws Throwable
   {
      final Timer timer = getTimer(invocation);
      if (timer == null)
         throw new IllegalStateException("EJBTHREE-2035: timer not set on invocation");
      if (timer instanceof Synchronization)
      {
         final Transaction tx = getTransaction();
         if (tx != null && tx.getStatus() != Status.STATUS_MARKED_ROLLBACK)
         {
            tx.registerSynchronization((Synchronization) timer);
         }
      }
      else
         log.warn("EJBTHREE-2035: Timer does not implement Synchronization, transaction semantics will not work");
      return invocation.invokeNext();
   }

   private Transaction getTransaction() throws SystemException
   {
      return tm.getTransaction();
   }
}
