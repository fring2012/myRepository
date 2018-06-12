## abupdate TRACE

一个简单的日志封装类，支持打印日志时的常用功能，代码简单就一个类，源码清晰；

使用方法
- gradle添加依赖
`compile 'com.abupdate.iot:trace:1.0.4'`

#### 支持的功能
1. 方便调用：Trace.d();
2. 无需初始化，直接使用
3. 支持保存文件
4. 支持跟踪源码
5. 支持自定义配置
6. 支持格式化打印array,list,json,xml
7. 支持打印线程信息
8. 支持格式化字符串
9. 支持打印异常调用栈

#### 不足(没必要支持)
1. 控制台输出4K的限制，日志文件无限制
2. 格式化json时，url连接显示问题(不影响使用)

#### 使用方法
```
Trace.d(TAG, " d");
Trace.i(TAG, " i");
Trace.w(TAG, " w");
Trace.e(TAG, " e = " + null);
Trace.d(TAG, " %s,%d", "raise", 1);
Trace.i(TAG, " %s,%d", "raise", 1);
Trace.w(TAG, " %s,%d", "raise", 1);
Trace.e(TAG, new NullPointerException("fu*k null pointer exception."));
Trace.json(TAG, "{\"name\":\"BeJson\",\"url\":\"http://www.bejson.com\",\"page\":88,\"isNonProfit\":true}");
Trace.array(TAG, new String[]{"value1", "value2"});
Trace.list(TAG, Arrays.asList("list1", "list2", "list3"));
Trace.xml(TAG,"<student><age>12</age><name>jack</name><skill><language>chinese</language><run>22</run></skill></student>");
Trace.file(TAG, new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.log"));
```