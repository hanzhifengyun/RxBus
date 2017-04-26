# RxBus
RxBus for Android

## Dependency

###Maven 
	
		<dependency>
		  <groupId>com.hanzhifengyun</groupId>
		  <artifactId>rxbus</artifactId>
		  <version>1.0.0</version>
		  <type>pom</type>
		</dependency>
		
###Gradle 

		compile 'com.hanzhifengyun:rxbus:1.0.0'
		
###Use

	 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(this);
    }
    
        
    @OnSubscribe(code = 100,
     threadType = ThreadType.UI)
    public void onDataChanged() {
        //loadData...
    }
    
    @OnSubscribe(code = 100,
     threadType = ThreadType.UI)
    public void onDataChanged(List<String> dataList) {
        //showData...
    }
    
    
    
    RxBus.getInstance().send(100);
    
    List<String> dataList = new ArrayList<>();
    dataList.add("a");
    dataList.add("b");
    dataList.add("c");
    
    RxBus.getInstance().send(100, dataList);