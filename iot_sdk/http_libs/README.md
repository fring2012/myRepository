## abupdate IOT HTTPTOOLS

### 初始化
    HttpManager.build(sCx)
            .setRedirectTimes(0)
            .setRetryTimes(3)
            .setSSL(new String(SDKConfig.KEY), "/assets/adcom.bks", new MyHostnameVerifier())
            .create();
            
### post form请求
    Map params = new HashMap();
    params.put("url", "http%3A//www.qq.com");
    params.put("user_key", "99aaff5e906348863e96617b21d364cd");
    //同步调用
    Response response = HttpIotUtils.postForm("http://ni2.org/api/create_multi.json")
            .map(params)
            .setMaxRedirectTimes(5)
            .build()
            .exec();
    //异步调用
    Response response = HttpIotUtils.postForm("http://ni2.org/api/create_multi.json")
            .map(params)
            .setMaxRedirectTimes(5)
            .build()
            .exec(new HttpListener(true) {//设为true表示回调到主线程，否回调到当前线程
                @Override
                public void onSuccess(String data, Response response) {
                    super.onSuccess(data, response);
                }

                @Override
                public void onFailure(HttpException e, Response response) {
                    super.onFailure(e, response);
                }
            });
         
### post json请求
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("mid", deviceInfo.mid);
    
    Response response = HttpIotUtils.postJson(baseUrl)
            .json(json)
            .build()
            .exec();
            
### get请求
    Response response1 = HttpIotUtils.get("http://www.baidu.com/s?wd=happy")
    .build()
    .exec();