/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree2275;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;

/**
 * SimpleSFSB
 *
 * @author Brad Maxwell
 * @version $Revision: $
 */
@Stateful(name="EJBTHREE-2275-SFSB")
@Remote(Counter.class)
// set removalTimeoutSeconds to 1 second and idletimeout to 10 seconds to allow removal to occur before passivation
@CacheConfig(idleTimeoutSeconds = 10, removalTimeoutSeconds=1)
public class SimpleSFSB implements Counter
{

   private int count;

   /**
    * @see org.jboss.ejb3.test.ejbthree2275.Counter#getCount()
    */
   public int getCount()
   {
      return this.count;
   }

   /**
    * @see org.jboss.ejb3.test.ejbthree2275.Counter#incrementCount()
    */
   public void incrementCount()
   {
      this.count++;

   }

}
