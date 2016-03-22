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
package org.jboss.ejb3.session;

import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.context.spi.SessionInvocationContext;
import org.jboss.ejb3.core.context.SessionInvocationContextAdapter;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SessionContainerInvocation<A extends SessionContainer, T extends BeanContext<A>> extends EJBContainerInvocation<A, T>
{
   private SessionInvocationContext invocationContext;

   public SessionContainerInvocation(Class<?> invokedBusinessInterface, MethodInfo info)
   {
      super(info);
      this.invocationContext = new SessionInvocationContextAdapter(invokedBusinessInterface, this);
   }
   
   /**
    * Creates a {@link SessionContainerInvocation}.
    * <p>
    *   This constructor is similar to {@link #SessionContainerInvocation(Class, MethodInfo)} except that this
    *   constructor overwrites the interceptors for this {@link Invocation} with the passed <code>interceptors</code>.
    *   This effectively, ignores the interceptors available from {@link MethodInfo#getInterceptors()} 
    * </p>
    * 
    * @param invokedBusinessInterface The invoked business interface
    * @param info The {@link MethodInfo}
    * @param interceptors The interceptors which will be used by this {@link Invocation}. 
    */
   public SessionContainerInvocation(Class<?> invokedBusinessInterface, MethodInfo info, Interceptor[] interceptors)
   {
      this(invokedBusinessInterface, info);
      this.interceptors = interceptors;
   }


   public SessionInvocationContext getInvocationContext()
   {
      return invocationContext;
   }

   @Override
   public void setBeanContext(org.jboss.ejb3.interceptors.container.BeanContext<?> beanCtx)
   {
      super.setBeanContext(beanCtx);
      if(beanCtx == null)
         invocationContext.setEJBContext(null);
      else
         invocationContext.setEJBContext(((org.jboss.ejb3.BeanContext<?>) beanCtx).getEJBContext());
   }   
}
