D:
cd D:\develop\code\workspace\iot_sdk\build\iport_dir
jar -xvf http_libs.jar
jar -xvf iot_download_libs.jar
jar -xvf iot_libs.jar
jar -xvf mqtt_libs.jar
jar -xvf trace.jar
del /F *.jar

jar -cvfM %1 .
