## abupdate IOT MQTT

#### 支持的功能
1. 连接
2. 断开连接
3. pub
4. sub
5. unSub

####使用方法

##### 1.设置Context

	MqttManager.getInstance().setContext(context);

    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    
    <service android:name="com.abupdate.mqtt_libs.mqtt_service.MqttService"/>
    
##### 2.注册消息监听
<code>
MqttManager.getInstance().registerMessageListener(listener);
</code>

##### 3.连接
1. 创建ConnectCommand实例；
2. 连接；

参考代码：

	ConnectCommand connectCommand = new ConnectCommand()
                .setClientId(DeviceInfo.getInstance().mid)
                .setServer(SERVER)
                .setPort(PORT)
                .setLastWill(
                        createLastWill(),
                        String.format("product/%s/%s/logout", DeviceInfo.getInstance().productId, RegisterInfo.getInstance().deviceId),
                        1,
                        false)
                .setTimeout(1000 * 5)
                .setKeepAlive(20)
                .setCleanSession(false)
                .setSsl("/assets/adcom.bks", "123456b")
                .setUserNameAndPassword(username, password);
        try {
            mqttManager.connect(connectCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

##### 4.断连
参考代码：

	DisconnectCommand disconnectCommand = new DisconnectCommand();
        try {
            MqttManager.getInstance().disConnect(disconnectCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

##### 5.pub
参考代码：

	PubCommand pubCommand = new PubCommand().setTopic(topic).setQos(qos).setMessage(message).setRetained(retained);
        try {
            MqttManager.getInstance().pub(pubCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

##### 6.sub
参考代码：

	SubCommand subCommand = new SubCommand().setQos(qos).setTopic(topic);
        try {
            MqttManager.getInstance().sub(subCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

##### 7.unSub

	UnsubCommand unsubCommand = new UnsubCommand().setTopic(topic);
        try {
            MqttManager.getInstance().unSub(unsubCommand,listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

##### 8. 判断是否是连接状态

	MqttManager.getInstance().isConneect();

##### 9. 查看mqtt内部log日志

	MqttManager.getInstance().setTraceEnable(true);
	MqttManager.getInstance().registerTraceListener(traceHandler);
若需要关闭则setTraceEnable(false)