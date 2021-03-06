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
package com.abupdate.mqtt_libs.mqttv3.internal.wire;


import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.mqtt_libs.mqttv3.MqttPersistable;
import com.abupdate.mqtt_libs.mqttv3.MqttPersistenceException;

public abstract class MqttPersistableWireMessage extends MqttWireMessage
		implements MqttPersistable {
	
	public MqttPersistableWireMessage(byte type) {
		super(type);
	}
	
	@Override
    public byte[] getHeaderBytes() throws MqttPersistenceException {
		try {
			return getHeader();
		}
		catch (MqttException ex) {
			throw new MqttPersistenceException(ex.getCause());
		}
	}
	
	@Override
    public int getHeaderLength() throws MqttPersistenceException {
		return getHeaderBytes().length;
	}

	@Override
    public int getHeaderOffset() throws MqttPersistenceException{
		return 0;
	}

//	public String getKey() throws MqttPersistenceException {
//		return new Integer(getMessageId()).toString();
//	}

	@Override
    public byte[] getPayloadBytes() throws MqttPersistenceException {
		try {
			return getPayload();
		}
		catch (MqttException ex) {
			throw new MqttPersistenceException(ex.getCause());
		}
	}
	
	@Override
    public int getPayloadLength() throws MqttPersistenceException {
		return 0;
	}

	@Override
    public int getPayloadOffset() throws MqttPersistenceException {
		return 0;
	}

}
