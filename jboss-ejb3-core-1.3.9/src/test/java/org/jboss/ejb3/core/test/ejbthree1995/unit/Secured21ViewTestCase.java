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
package org.jboss.ejb3.core.test.ejbthree1995.unit;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1995.SecuredGreeter21Bean;
import org.jboss.ejb3.core.test.ejbthree1995.SecuredGreeter21Remote;
import org.jboss.ejb3.core.test.ejbthree1995.SecuredGreeter21RemoteHome;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.security.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.Principal;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Secured21ViewTestCase extends AbstractEJB3TestCase
{
   @After
   public void after()
   {
      SecurityContextAssociation.setSecurityContext(null);
   }

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      deploy("securitymanager-beans.xml");

      SessionContainer container = deploySessionEjb(SecuredGreeter21Bean.class);
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
   public void testAnonymous() throws Exception
   {
      SecurityContext sc = SecurityContextFactory.createSecurityContext("test");
      SecurityContextAssociation.setSecurityContext(sc);
            
      SecuredGreeter21RemoteHome home = lookup("SecuredGreeter21Bean/home", SecuredGreeter21RemoteHome.class);
      SecuredGreeter21Remote bean = home.create("testAnonymous");
      String result = bean.sayHi();

      assertEquals("Hi testAnonymous (anonymous)", result);
   }

   @Test
   public void testSomebody() throws Exception
   {
      login("somebody", null);
      
      SecuredGreeter21RemoteHome home = lookup("SecuredGreeter21Bean/home", SecuredGreeter21RemoteHome.class);
      SecuredGreeter21Remote bean = home.create("testSomebody");
      String result = bean.sayHi();

      assertEquals("Hi testSomebody (somebody)", result);
   }
}
