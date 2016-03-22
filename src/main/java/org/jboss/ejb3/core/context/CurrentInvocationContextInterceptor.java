/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.core.context;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.context.CurrentInvocationContext;
import org.jboss.ejb3.context.spi.InvocationContext;
import org.jboss.ejb3.interceptors.container.LifecycleMethodInterceptorsInvocation;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class CurrentInvocationContextInterceptor implements Interceptor
{
   public String getName()
   {
      return getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      InvocationContext ctx = getInvocationContext(invocation);
      CurrentInvocationContext.push(ctx);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         CurrentInvocationContext.pop();
      }
   }

   private InvocationContext getInvocationContext(Invocation invocation)
   {
      if(invocation instanceof LifecycleMethodInterceptorsInvocation)
         return getInvocationContext((LifecycleMethodInterceptorsInvocation) invocation);
      return ((EJBInvocation) invocation).getInvocationContext();
   }

   // TODO: LifecycleMethodInterceptorsInvocation can not be cast to EJBInvocation
   private InvocationContext getInvocationContext(LifecycleMethodInterceptorsInvocation invocation)
   {
      BeanContext<?> beanContext = (BeanContext<?>) invocation.getBeanContext();
      InvocationContext invocationContext = beanContext.createLifecycleInvocation();
      invocationContext.setEJBContext(beanContext.getEJBContext());
      return invocationContext;
   }
}
