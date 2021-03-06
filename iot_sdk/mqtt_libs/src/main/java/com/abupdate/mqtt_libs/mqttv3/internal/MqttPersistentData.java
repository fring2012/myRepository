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
package com.abupdate.mqtt_libs.mqttv3.internal;


import com.abupdate.mqtt_libs.mqttv3.MqttPersistable;

public class MqttPersistentData implements MqttPersistable {
	// Message key
	private String    key  = null;

	// Message header
	private byte[] header  = null;
	private int    hOffset = 0;
	private int    hLength = 0;

	// Message payload
	private byte[] payload = null;
	private int    pOffset = 0;
	private int    pLength = 0;

	/**
	 * Construct a data object to pass across the MQTT client persistence interface.
	 * 
	 * When this Object is passed to the persistence implementation the key is
	 * used by the client to identify the persisted data to which further
	 * update or deletion requests are targeted.<BR>
	 * When this Object is created for returning to the client when it is
	 * recovering its state from persistence the key is not required to be set.
	 * The client can determine the key from the data. 
	 * @param key     The key which identifies this data
	 * @param header  The message header
	 * @param hOffset The start offset of the header bytes in header.
	 * @param hLength The length of the header in the header bytes array.
	 * @param payload The message payload
	 * @param pOffset The start offset of the payload bytes in payload.
	 * @param pLength The length of the payload in the payload bytes array
	 * when persisting the message.
	 */
	public MqttPersistentData( String key,
			byte[] header,
			int    hOffset,
			int    hLength,
			byte[] payload,
			int    pOffset,
			int    pLength) {
		this.key     = key;
		this.header  = header;
		this.hOffset = hOffset;
		this.hLength = hLength;
		this.payload = payload;
		this.pOffset = pOffset;
		this.pLength = pLength;
	}

	public String getKey() {
		return key;
	}

	@Override
    public byte[] getHeaderBytes() {
		return header;
	}

	@Override
    public int getHeaderLength() {
		return hLength;
	}

	@Override
    public int getHeaderOffset() {
		return hOffset;
	}

	@Override
    public byte[] getPayloadBytes() {
		return payload;
	}

	@Override
    public int getPayloadLength() {
		if ( payload == null ) {
			return 0;
		}
		return pLength;
	}

	@Override
    public int getPayloadOffset() {
		return pOffset;
	}
}
