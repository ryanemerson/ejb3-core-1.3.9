/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.stateful;

import org.jboss.aop.Advisor;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.context.base.BaseSessionInvocationContext;
import org.jboss.ejb3.context.spi.InvocationContext;
import org.jboss.ejb3.context.spi.SessionInvocationContext;
import org.jboss.ejb3.core.context.EJBInvocation;
import org.jboss.ejb3.core.context.SessionInvocationContextAdapter;
import org.jboss.ejb3.interceptors.container.BeanContext;

import java.lang.reflect.Method;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 102482 $
 */
public class StatefulContainerInvocation extends EJBContainerInvocation<StatefulContainer, StatefulBeanContext>
   implements EJBInvocation
{
   private static final long serialVersionUID = -7636489066612082373L;
   
   private Object id;
   private SessionInvocationContext invocationContext;

   public StatefulContainerInvocation(Interceptor[] interceptors, long methodHash, Method advisedMethod, Method unadvisedMethod, Advisor advisor, Object id, Class<?> invokedBusinessInterface)
   {
      super(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor);
      this.id = id;
      this.invocationContext = new SessionInvocationContextAdapter(invokedBusinessInterface, this);
   }

   public StatefulContainerInvocation(MethodInfo info, Object id, Class<?> invokedBusinessInterface)
   {
      super(info);
      this.id = id;
      this.invocationContext = new SessionInvocationContextAdapter(invokedBusinessInterface, this);
   }

   public StatefulContainerInvocation()
   {
   }

   public Object getId()
   {
      return id;
   }

   public Invocation copy()
   {
      StatefulContainerInvocation wrapper = new StatefulContainerInvocation(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor, id, invocationContext.getInvokedBusinessInterface());
      wrapper.metadata = this.metadata;
      wrapper.currentInterceptor = this.currentInterceptor;
      //wrapper.setTargetObject(this.getTargetObject());
      wrapper.setArguments(this.getArguments());
      wrapper.setBeanContext(getBeanContext());
      return wrapper;
   }

   public InvocationContext getInvocationContext()
   {
      return invocationContext;
   }

   @Override
   public void setBeanContext(BeanContext<?> beanCtx)
   {
      super.setBeanContext(beanCtx);
      if(beanCtx == null)
         invocationContext.setEJBContext(null);
      else
         invocationContext.setEJBContext(((org.jboss.ejb3.BeanContext<?>) beanCtx).getEJBContext());
   }
}
