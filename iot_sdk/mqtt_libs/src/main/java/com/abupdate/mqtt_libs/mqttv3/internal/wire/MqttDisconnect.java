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

import java.io.IOException;



/**
 * An on-the-wire representation of an MQTT DISCONNECT message.
 */
public class MqttDisconnect extends MqttWireMessage {
	public static final String KEY="Disc";
	
	public MqttDisconnect() {
		super(MqttWireMessage.MESSAGE_TYPE_DISCONNECT);
	}

	public MqttDisconnect(byte info, byte[] variableHeader) throws IOException {
		super(MqttWireMessage.MESSAGE_TYPE_DISCONNECT);
	}
	
	@Override
    protected byte getMessageInfo() {
		return (byte) 0;
	}

	@Override
    protected byte[] getVariableHeader() throws MqttException {
		return new byte[0];
	}

	/**
	 * Returns whether or not this message needs to include a message ID.
	 */
	@Override
    public boolean isMessageIdRequired() {
		return false;
	}
	
	@Override
    public String getKey() {
		return KEY;
	}
}
