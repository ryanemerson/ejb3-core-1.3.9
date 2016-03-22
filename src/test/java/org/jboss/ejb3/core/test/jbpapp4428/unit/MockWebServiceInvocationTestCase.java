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
package org.jboss.ejb3.core.test.jbpapp4428.unit;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.jbpapp4428.MockSPIProviderResolver;
import org.jboss.ejb3.core.test.jbpapp4428.WebServiceMockStateless;
import org.jboss.ejb3.core.test.jbpapp4428.WebServiceMockStatelessBean;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SecurityContextUtil;
import org.jboss.security.SimplePrincipal;
import org.jboss.wsf.spi.invocation.integration.InvocationContextCallback;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.Principal;

/**
 * This test mimics the way WS calls into EJB3.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class MockWebServiceInvocationTestCase extends AbstractEJB3TestCase
{
   private static StatelessContainer container;

   @AfterClass
   public static void afterClass() throws Exception
   {
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      System.setProperty("org.jboss.wsf.spi.SPIProviderResolver", MockSPIProviderResolver.class.getName());

      AbstractEJB3TestCase.beforeClass();

      deploy("securitymanager-beans.xml");

      container = (StatelessContainer) deploySessionEjb(WebServiceMockStatelessBean.class);
      container.setJaccContextId("test");
   }

   private SecurityContext login(String name, Object credential) throws Exception
   {
      SecurityContext sc = SecurityContextFactory.createSecurityContext("test");
      SecurityContextUtil util = sc.getUtil();
      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();
      subject.getPrincipals().add(principal);
      subject.getPrivateCredentials().add(credential);
      util.createSubjectInfo(principal, credential, subject);
      SecurityContextAssociation.setSecurityContext(sc);
      return sc;
   }

   @Test
   public void testFail() throws Throwable
   {
      final SecurityContext sc = SecurityContextFactory.createSecurityContext("test");
      SecurityContextAssociation.setSecurityContext(sc);

      WebServiceMockStateless bean = lookup(WebServiceMockStatelessBean.class.getSimpleName() + "/local", WebServiceMockStateless.class);
      bean.fail();
   }

   @Test
   public void testSucceed() throws Throwable
   {
      final SecurityContext sc = SecurityContextFactory.createSecurityContext("test");
      SecurityContextAssociation.setSecurityContext(sc);

      final Method method = WebServiceMockStateless.class.getDeclaredMethod("succeed");
      final Object[] args = null;
      final InvocationContextCallback invCtxCallback = new InvocationContextCallback()
      {
         @Override
         public <T> T get(Class<T> tClass)
         {
            return null;
         }
      };
      container.invokeEndpoint(method, args, invCtxCallback);
   }
}
