# RxBus for Android
通过RxBus来实现事件的通知和订阅，简化应用组件间的通信,解耦事件的发送者和接收者,避免复杂和容易出错的依赖和生命周期的问题.
## 1.	依赖方式Dependency
### 1.1	Maven
		<dependency>
		  <groupId>com.hanzhifengyun</groupId>
		  <artifactId>rxbus</artifactId>
		  <version>1.0.0</version>
		  <type>pom</type>
		</dependency>
### 1.2 Gradle
	compile 'com.hanzhifengyun:rxbus:1.0.0'
## 2 如何使用Use
### 2.1 在合适的时机注册
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在页面创建时订阅
		RxBus.getInstance().register(this);
	}

### 2.2 在合适的时机解除注册
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在页面销毁时解除订阅，防止页面销毁后导致内存溢出
		RxBus.getInstance().unRegister(this);
	}

### 2.3 如何发送事件
-	只发送事件code，不包含数据，一般用于通知某一事件的发生

		RxBus.getInstance().send(int code);

-	只发送事件数据，不包含code，一般用于所有关心相同类型数据变化的接收者

		RxBus.getInstance().send(Object data);

-	根据不同code发送事件数据，只有相同的code和数据类型的接收者才能接收到数据

		RxBus.getInstance().send(int code, Object data);

### 2.4 如何接收事件
1.	在已经注册过的类中自定义一个public方法，方法名随意，参数填写需要接收数据的类型.
2.	在方法上添加注解OnSubscribe，表示接收订阅的信息:
	-	code: 用于区分发送的不同事件的标识，int类型
	-	threadType: 标识事件接收时回调在什么线程

### 2.5 例子：
1.	只关心事件类型，不包含数据
-	定义事件类型

		public static final int CODE_PAY_SUCCESS = 3;//支付成功事件

-	发送事件

		RxBus.getInstance().send(CODE_PAY_SUCCESS);

-	接收事件，并在UI主线程中消费

		@OnSubscribe(code = CODE_PAY_SUCCESS,  threadType = ThreadType.UI)
		public void onPaySuccess() {
			//do something after pay success…
		}

2.	只关心事件数据
-	发送事件，只包含数据

		List<String> dataList = new ArrayList<>();
		dataList.add("a");
		dataList.add("b");
		dataList.add("c");
		RxBus.getInstance().send(dataList);

-	接收数据，并在IO子线程中消费

		@OnSubscribe(threadType = ThreadType. IO)
		public void onDataChanged(List<String> dataList) {
			// do something for data…
		}

3.	只关心特定的事件类型和特定的数据
-	定义事件类型

		public static final int CODE_LOAD_DATA = 9;//加载数据事件

-	发送事件

		List<String> dataList = new ArrayList<>();
		dataList.add("a");
		dataList.add("b");
		dataList.add("c");
		RxBus.getInstance().send(CODE_LOAD_DATA, dataList);

-	接收数据，并在CURRENT_THREAD当前线程中消费

		@OnSubscribe(code = CODE_LOAD_DATA, threadType =
		ThreadType. CURRENT_THREAD)
		public void onDataChanged(List<String> dataList) {
		// do something for data…
		}

