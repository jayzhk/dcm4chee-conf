/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.hooks;

import static java.lang.String.format;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * @author Roman K
 */
public class HooksProducer {

    @Inject
    private HooksManager hooksManager;
    
    @Produces
    public <T> Hooks<T> resolveHooks(InjectionPoint injectionPoint, BeanManager beanManager) {
        final Collection<Object> activeOrderedHooks = hooksManager.getOrderedActiveHooks(injectionPoint, beanManager);
        
        Limit hookMultiplicity = injectionPoint.getAnnotated().getAnnotation(Limit.class);
        if(hookMultiplicity != null) {
            int nrOfActiveHooks = activeOrderedHooks.size();
            
            if((nrOfActiveHooks < hookMultiplicity.min()) || (nrOfActiveHooks > hookMultiplicity.max())) {
                throw new IllegalArgumentException(
                        format("Number of configured active hooks (%s) does not match specified limit [min = %s, max = %s] for hook interface '%s'",
                                nrOfActiveHooks,
                                hookMultiplicity.min(), 
                                hookMultiplicity.max(),        
                                getHookTypeName(injectionPoint) ));
            }
        }
        

        return new Hooks() {
            @Override
            public Iterator iterator() {
                return activeOrderedHooks.iterator();
            }
        };
    }
    
    private static String getHookTypeName(InjectionPoint injectionPoint) {
        ParameterizedType type = (ParameterizedType)injectionPoint.getType();
        Type hookType = type.getActualTypeArguments()[0];
        Class<?> hookTypeClass = (Class<?>)hookType;
        return hookTypeClass.getName();
    }

}
