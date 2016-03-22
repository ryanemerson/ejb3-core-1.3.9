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
package org.jboss.ejb3.core.test.ejbthree1995;

import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.Resource;
import javax.ejb.Init;
import javax.ejb.RemoteHome;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import java.security.Principal;

/**
 * An EJB 3 bean exposing an EJB 2.1 view.
 * 
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Stateful
@SecurityDomain(value="test", unauthenticatedPrincipal="anonymous")
@RemoteHome(SecuredGreeter21RemoteHome.class)
public class SecuredGreeter21Bean
{
   @Resource
   private SessionContext ctx;
   
   private String name;
   private String user;

   @Init
   public void init(String name)
   {
      Principal caller = ctx.getCallerPrincipal();
      this.name = name;
      this.user = caller.getName();
   }

   public String sayHi()
   {
      return "Hi " + name + " (" + user + ")";
   }
}
