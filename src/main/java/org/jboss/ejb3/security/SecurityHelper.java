/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.security;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.mdb.MessagingContainer;

import javax.ejb.TimedObject;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import java.lang.reflect.Method;

//$Id$

/**
 *  Helper class for the EJB3 Security Interceptors
 *  @author Anil.Saldhana@redhat.com
 *  @since  Aug 23, 2007 
 *  @version $Revision$
 */
public class SecurityHelper
{
   /**
    * Check if the method is an EJBTimeOut method
    * @param m method
    * @return true if it is a ejb timeout callback
    */
   public boolean isEJBTimeOutCallback(Method m)
   {
      /** The TimedObject.ejbTimeout callback */
      Method ejbTimeout = null;
      
      try
      {
         // Get the timeout method
         ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
      }
      catch (NoSuchMethodException ignore)
      {
      } 
      return m == ejbTimeout; 
   } 
   
   /**
    * Checks whether a method declares a Timeout annotation
    * @param container EJBContainer
    * @param meth The method under investigation for an annotation
    * @return @Timeout annotation exists
    */
   public boolean containsTimeoutAnnotation(EJBContainer container, Method meth)
   {
      return (Timeout) container.resolveAnnotation(meth, Timeout.class) != null;
   }
   
   /**
    * Determine if the container is a MDB
    * @param container
    * @return
    */
   public boolean isMDB(Container container)
   {
      return container instanceof MessagingContainer;
   }
}
