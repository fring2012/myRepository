/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
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
 *    James Sutton - Initial Contribution for Automatic Reconnect & Offline Buffering
 */
package com.abupdate.mqtt_libs.mqttv3.internal;

import com.abupdate.mqtt_libs.mqttv3.BufferedMessage;
import com.abupdate.mqtt_libs.mqttv3.DisconnectedBufferOptions;
import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.mqtt_libs.mqttv3.MqttToken;
import com.abupdate.mqtt_libs.mqttv3.internal.wire.MqttWireMessage;

import java.util.ArrayList;



public class DisconnectedMessageBuffer implements Runnable {
	
	private static final String CLASS_NAME = "DisconnectedMessageBuffer";
	private DisconnectedBufferOptions bufferOpts;
	private ArrayList buffer;
	private Object	bufLock = new Object();  	// Used to synchronise the buffer
	private IDisconnectedBufferCallback callback;
	
	public DisconnectedMessageBuffer(DisconnectedBufferOptions options){
		this.bufferOpts = options;
		buffer = new ArrayList();
	}
	
	/**
	 * This will add a new message to the offline buffer,
	 * if the buffer is full and deleteOldestMessages is enabled
	 * then the 0th item in the buffer will be deleted and the
	 * new message will be added. If it is not enabled then an
	 * MqttException will be thrown.
	 * @param message the {@link MqttWireMessage} that will be buffered
	 * @param token the associated {@link MqttToken}
	 * @throws MqttException if the Buffer is full
	 */
	public void putMessage(MqttWireMessage message, MqttToken token) throws MqttException {
		BufferedMessage bufferedMessage = new BufferedMessage(message, token);
		synchronized (bufLock) {
			if(buffer.size() < bufferOpts.getBufferSize()){
				buffer.add(bufferedMessage);
			} else if(bufferOpts.isDeleteOldestMessages() == true){
				buffer.remove(0);
				buffer.add(bufferedMessage);
			}else {
				throw new MqttException(MqttException.REASON_CODE_DISCONNECTED_BUFFER_FULL);
			}
		}
	}
	
	/**
	 * Retrieves a message from the buffer at the given index.
	 * @param messageIndex the index of the message to be retrieved in the buffer
	 * @return the {@link BufferedMessage}
	 */
	public BufferedMessage getMessage(int messageIndex){
		synchronized (bufLock) {
			return((BufferedMessage) buffer.get(messageIndex));
		}
	}
	
	
	/**
	 * Removes a message from the buffer
	 * @param messageIndex the index of the message to be deleted in the buffer
	 */
	public void deleteMessage(int messageIndex){
		synchronized (bufLock) {
			buffer.remove(messageIndex);
		}
	}
	
	/**
	 * Returns the number of messages currently in the buffer
	 * @return The count of messages in the buffer
	 */
	public int getMessageCount() {
		synchronized (bufLock) {
			return buffer.size();
		}
	}
	
	/**
	 * Flushes the buffer of messages into an open connection
	 */
	@Override
    public void run() {
		final String methodName = "run";
		// @TRACE 516=Restoring all buffered messages.
			while(getMessageCount() > 0){
				try {
				BufferedMessage bufferedMessage = getMessage(0);
				callback.publishBufferedMessage(bufferedMessage);
				// Publish was successful, remove message from buffer.
				deleteMessage(0);
				} catch (MqttException ex) {
					// Error occurred attempting to publish buffered message likely because the client is not connected
					// @TRACE 517=Error occured attempting to publish buffered message due to disconnect.
					break;
				}
			}
	}

	public void setPublishCallback(IDisconnectedBufferCallback callback) {
		this.callback = callback;
	}
	
	public boolean isPersistBuffer(){
		return bufferOpts.isPersistBuffer();
	}

}
