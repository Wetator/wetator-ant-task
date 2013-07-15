/*
 * Copyright (c) 2008-2010 Ronald Brill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.rbri.wet.core.variable;

import org.rbri.wet.util.SecretString;



/**
 * An object that stores a variable.
 *  
 * @author rbri
 */
public final class Variable {
    private String name;
    private SecretString secretValue;
    

    /**
     * Constructor.
     */
    public Variable(String aName, String aValue) {
        this(aName, aValue, false);
    }


    /**
     * Constructor.
     */
    public Variable(String aName, String aValue, boolean anSecretFlag) {
        this(aName, new SecretString(aValue, anSecretFlag));
    }


    /**
     * Constructor.
     */
    public Variable(String aName, SecretString aValue) {
        super();

        if (null == aName) {
            throw new IllegalArgumentException("Parameter aName can't be null.");
        }

        name = aName;
        secretValue = aValue;
    }


    public String getName() {
        return name;
    }
    

    public SecretString getValue() {
        return secretValue;
    }
}
