/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */
package com.abupdate.mqtt_libs.mqttv3.persist;

import com.abupdate.mqtt_libs.mqttv3.MqttClientPersistence;
import com.abupdate.mqtt_libs.mqttv3.MqttPersistable;
import com.abupdate.mqtt_libs.mqttv3.MqttPersistenceException;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Persistence that uses memory
 * <p>
 * In cases where reliability is not required across client or device
 * restarts memory this memory peristence can be used. In cases where
 * reliability is required like when clean session is set to false
 * then a non-volatile form of persistence should be used.
 */
public class MemoryPersistence implements MqttClientPersistence {

    private Hashtable data;

    /**
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#close()
     */
    @Override
    public void close() throws MqttPersistenceException {
        data.clear();
    }

    /**
     * @return Enumeration
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#keys()
     */
    @Override
    public Enumeration keys() throws MqttPersistenceException {
        return data.keys();
    }

    /**
     * @param key the key for the data, which was used when originally saving it.
     * @return
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#get(java.lang.String)
     */
    @Override
    public MqttPersistable get(String key) throws MqttPersistenceException {
        return (MqttPersistable) data.get(key);
    }

    /**
     * @param clientId  The client for which the persistent store should be opened.
     * @param serverURI The connection string as specified when the MQTT client instance was created.
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#open(java.lang.String, java.lang.String)
     */
    @Override
    public void open(String clientId, String serverURI) throws MqttPersistenceException {
        this.data = new Hashtable();
    }

    /**
     * @param key         the key for the data, which will be used later to retrieve it.
     * @param persistable the data to persist
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#put(java.lang.String, MqttPersistable)
     */
    @Override
    public void put(String key, MqttPersistable persistable) throws MqttPersistenceException {
        data.put(key, persistable);
    }

    /**
     * @param key The key for the data to remove
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#remove(java.lang.String)
     */
    @Override
    public void remove(String key) throws MqttPersistenceException {
        data.remove(key);
    }

    /**
     * @throws MqttPersistenceException
     * @see MqttClientPersistence#clear()
     */
    @Override
    public void clear() throws MqttPersistenceException {
        data.clear();
    }

    /**
     * @see MqttClientPersistence#containsKey(java.lang.String)
     * @param key the key for data, which was used when originally saving it.
     * @return
     * @throws MqttPersistenceException
     */
    @Override
    public boolean containsKey(String key) throws MqttPersistenceException {
        return data.containsKey(key);
    }
}
