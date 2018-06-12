/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */

package com.abupdate.mqtt_libs.mqttv3;

import com.abupdate.mqtt_libs.mqttv3.internal.ClientComms;

import java.util.Timer;
import java.util.TimerTask;



/**
 * Default ping sender implementation
 *
 * <p>This class implements the {@link MqttPingSender} pinger interface
 * allowing applications to send ping packet to server every keep alive interval.
 * </p>
 *
 * @see MqttPingSender
 */
public class TimerPingSender implements MqttPingSender {
	private static final String CLASS_NAME = TimerPingSender.class.getName();

	private ClientComms comms;
	private Timer timer;

	@Override
	public void init(ClientComms comms) {
		if (comms == null) {
			throw new IllegalArgumentException("ClientComms cannot be null.");
		}
		this.comms = comms;
	}

	@Override
	public void start() {
		final String methodName = "start";		
		String clientid = comms.getClient().getClientId();
		
		//@Trace 659=start timer for client:{0}

		timer = new Timer("MQTT Ping: " + clientid);
		//Check ping after first keep alive interval.
		timer.schedule(new PingTask(), comms.getKeepAlive());
	}

	@Override
	public void stop() {
		final String methodName = "stop";
		//@Trace 661=stop
		if(timer != null){
			timer.cancel();
		}
	}

	@Override
	public void schedule(long delayInMilliseconds) {
		timer.schedule(new PingTask(), delayInMilliseconds);		
	}
	
	private class PingTask extends TimerTask {
		private static final String methodName = "PingTask.run";
		
		@Override
		public void run() {
			//@Trace 660=Check schedule at {0}
			comms.checkForActivity();
		}
	}
}
