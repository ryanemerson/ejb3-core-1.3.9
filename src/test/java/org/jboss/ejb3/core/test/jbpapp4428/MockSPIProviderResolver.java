/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.ejb3.core.test.jbpapp4428;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.ExtensibleWebServiceContext;
import org.jboss.wsf.spi.invocation.InvocationType;
import org.jboss.wsf.spi.invocation.WebServiceContextEJB;
import org.jboss.wsf.spi.invocation.WebServiceContextFactory;

import javax.xml.ws.handler.MessageContext;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class MockSPIProviderResolver extends SPIProviderResolver
{
   private static final Map<Class<?>, Object> SPIS = new HashMap<Class<?>, Object>();

   private static final SPIProvider INSTANCE = new SPIProvider()
   {
      @Override
      public <T> T getSPI(Class<T> tClass)
      {
         return tClass.cast(SPIS.get(tClass));
      }
   };

   static
   {
      SPIS.put(WebServiceContextFactory.class, new WebServiceContextFactory()
      {
         @Override
         public ExtensibleWebServiceContext newWebServiceContext(InvocationType invocationType, MessageContext messageContext)
         {
            return new WebServiceContextEJB(messageContext);
         }
      });
   }

   @Override
   public SPIProvider getProvider()
   {
      return INSTANCE;
   }
}
