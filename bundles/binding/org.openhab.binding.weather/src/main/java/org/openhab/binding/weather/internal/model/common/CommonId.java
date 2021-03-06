/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather.internal.model.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple class with the JAXB mapping for a common id.
 *
 * @author Gerhard Riegler
 * @since 1.6.0
 */
@XmlRootElement(name = "common-id-mapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class CommonId {
    @XmlAttribute(name = "id", required = true)
    private String id;

    @XmlElement(name = "provider")
    private List<CommonIdProvider> providers = new ArrayList<CommonIdProvider>();

    /**
     * Returns all the providers.
     */
    public List<CommonIdProvider> getProviders() {
        return providers;
    }

    /**
     * Returns the common id.
     */
    public String getId() {
        return id;
    }
}
