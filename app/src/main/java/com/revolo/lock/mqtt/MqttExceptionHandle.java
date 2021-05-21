package com.revolo.lock.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import timber.log.Timber;

public class MqttExceptionHandle {

	private MqttExceptionHandle() {
	}

	public static final int ConnectException = 1;
	public static final int PublishException = 2;
	public static final int SubscribeException = 3;

	/**
	 * @param type
	 * @param asyncActionToken
	 */
	public static void onFail(int type, IMqttToken asyncActionToken, final Throwable exception) {
		if (asyncActionToken == null) {
			return;
		}
		if (asyncActionToken.getException() == null) {
			return;
		}
		if (exception == null) {
			return;
		}
		final int result = asyncActionToken.getException().getReasonCode();
		Timber.d("mqtt发生异常"+exception.toString()+"类型是"+type);
		switch (result) {
			//客户端遇到异常。使用{@link #getCause()}方法获取底层原因。
			case 0:

				break;
			case 0x01:

				break;
			/** The server has rejected the supplied client ID
			 *服务器拒绝了提供的客户机ID
			 * */
			case 0x02:
				/** The broker was not available to handle the request.
				 *代理无法处理该请求。
				 * */
			case 0x03:
				break;
			/** Authentication with the server has failed, due to a bad user name or password.
			 *由于用户名或密码错误，服务器的身份验证失败。
			 * */
			case 0x04:
				break;
			/** Not authorized to perform the requested operation
			 *未获授权执行所要求的操作
			 * */
			case 0x05:
				break;

			/** An unexpected error has occurred.
			 *发生了意外错误。
			 * */
			case 0x06:
				break;

			/** Error from subscribe - returned from the server.
			 *从服务器返回的订阅错误。
			 * */
			case 0x80:

				break;
			/**
			 * Client timed out while waiting for a response from the server.
			 * The server is no longer responding to keep-alive messages.
			 * *客户端在等待服务器响应时超时。
			 * *服务器不再响应保持活动的消息。
			 */
			case 32000:


				break;

			/**
			 * Internal error, caused by no new message IDs being available.
			 * 内部错误，由没有可用的新消息id引起。
			 */
			case 32001:
				break;

			/**
			 * Client timed out while waiting to write messages to the server.
			 * 客户机在等待向服务器写入消息时超时。
			 */
			case 32002:

				break;

			/**
			 * The client is already connected.
			 * 客户端已经连接。
			 */
			case 32100:
				break;

			/**
			 * The client is already disconnected.
			 * 客户端已经断开连接。
			 */
			case 32101:
				break;
			/**
			 * The client is currently disconnecting and cannot accept any new work.
			 * This can occur when waiting on a token, and then disconnecting the client.
			 * If the message delivery does not complete within the quiesce timeout
			 * period, then the waiting token will be notified with an exception.
			 *客户端正在断开连接，无法接受任何新工作。
			 *这可能发生在等待令牌，然后断开客户端。
			 *如果消息传递没有在静默超时期间完成，则会异常通知等待令牌。
			 */
			case 32102:
				break;

			/** Unable to connect to server
			 *无法连接到服务器
			 * */
			case 32103:
				break;

			/**
			 * The client is not connected to the server.  The {@link MqttClient#connect()}
			 * or {@link MqttClient#connect(MqttConnectOptions)} method must be called
			 * first.  It is also possible that the connection was lost - see
			 * {@link MqttClient#setCallback(MqttCallback)} for a way to track lost
			 * connections.
			 * 客户端没有连接到服务器。{@link MqttClient # connect ()}
			 * 必须调用{@link MqttClient#connect(MqttConnectOptions)}方法
			 * *第一。也有可能是连接丢失了
			 * * {@link MqttClient#setCallback(MqttCallback)}用于跟踪丢失的路径
			 * 客户端没有连接到服务器。必须首先调用{@link MqttClient#connect()}或{@link MqttClient#connect(MqttConnectOptions)}方法。
			 * 也有可能是连接丢失了——查看{@link MqttClient#setCallback(MqttCallback)}来跟踪丢失的连接。
			 *
			 */
			case 32104:
				break;

			/**
			 * Server URI and supplied <code>SocketFactory</code> do not match.
			 * URIs beginning <code>tcp://</code> must use a <code>javax.net.SocketFactory</code>,
			 * and URIs beginning <code>ssl://</code> must use a <code>javax.net.ssl.SSLSocketFactory</code>.
			 *
			 * 服务器URI和提供的<code>SocketFactory</code>不匹配。
			 * uri开头的<code>tcp:// /code>必须使用<code>javax.net.SocketFactory</code>，
			 * 并且uri开头的<code>ssl://</code>必须使用<code>javax.net.ssl.SSLSocketFactory</code> >。
			 */
			case 32105:
				break;

			/**
			 * SSL configuration error.
			 * SSL配置错误。
			 */
			case 32106:
				break;

			/**
			 * Thrown when an attempt to call {@link MqttClient#disconnect()} has been
			 * made from within a method on {@link MqttCallback}.  These methods are invoked
			 * by the client's thread, and must not be used to control disconnection.
			 *
			 * @see MqttCallback#messageArrived(String, MqttMessage)
			 * 当尝试从{@link MqttClient#disconnect()}上的方法中调用{@link MqttCallback}时抛出。
			 * 这些方法由客户机的线程调用，不能用于控制断开。
			 */
			case 32107:
				break;

			/**
			 * Protocol error: the message was not recognized as a valid MQTT packet.
			 * Possible reasons for this include connecting to a non-MQTT server, or
			 * connecting to an SSL server port when the client isn't using SSL.
			 * 协议错误:消息没有被识别为有效的MQTT包。
			 * 可能的原因包括连接到非mqtt服务器，
			 * 或者在客户机不使用SSL时连接到SSL服务器端口。
			 */
			case 32108:
				break;

			/**
			 * The client has been unexpectedly disconnected from the server. The {@link #getCause() cause}
			 * will provide more details.
			 * 客户机意外地与服务器断开连接。{@link #getCause() cause}
			 * 将提供更多细节。
			 */
			case 32109:
				break;

			/**
			 * A connect operation in already in progress, only one connect can happen
			 * at a time.
			 * 一个连接操作已经在进行中，一次只能发生一个连接。
			 */
			case 32110:
				break;

			/**
			 * The client is closed - no operations are permitted on the client in this
			 * state.  New up a new client to continue.
			 * 客户端是关闭的——在这种状态下，客户端不允许任何操作。一个新的客户端继续。
			 */
			case 32111:
				break;

			/**
			 * A request has been made to use a token that is already associated with
			 * another action.  If the action is complete the reset() can ve called on the
			 * token to allow it to be reused.
			 * 请求使用已经与另一个操作关联的令牌。
			 * 如果操作完成，可以调用令牌上的reset()来允许它被重用。
			 */
			case 32201:
				break;

			/**
			 * A request has been made to send a message but the maximum number of inflight
			 * messages has already been reached. Once one or more messages have been moved
			 * then new messages can be sent.
			 * 已发出发送消息的请求，但已达到飞行消息的最大数量。
			 * 一旦移动了一个或多个消息，就可以发送新的消息。
			 *
			 */
			case 32202:
				break;

			/**
			 * The Client has attempted to publish a message whilst in the 'resting' / offline
			 * state with Disconnected Publishing enabled, however the buffer is full and
			 * deleteOldestMessages is disabled, therefore no more messages can be published
			 * until the client reconnects, or the application deletes buffered message
			 * manually.
			 *
			 * 客户端试图在启用断开连接的发布的“休眠”/脱机状态下发布消息，
			 * 但是缓冲区已满且deleteOldestMessages已禁用，
			 * 因此在客户端重新连接或应用程序手动删除缓冲消息之前不能再发布消息。
			 *
			 */
			case 32203:
				break;
		}

		if (result == 32100 || result == 32101||result==0) {
			return;
		}


	}

}
