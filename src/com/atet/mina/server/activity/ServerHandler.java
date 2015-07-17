package com.atet.mina.server.activity;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import android.util.Log;

public class ServerHandler extends IoHandlerAdapter {

	public static final String TAG = "ServerHandler";

	private IMsgCallback mCallback;

	public ServerHandler(IMsgCallback callback) {

		mCallback = callback;
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionClosed(session);
		session.close(true);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		// TODO Auto-generated method stub
		super.sessionIdle(session, status);
		if (IdleStatus.READER_IDLE == status) {
			session.close(true);
		}
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		String expression = message.toString();

		Log.e(TAG, "receive client msg: " + expression);

		if (null != mCallback) {

			mCallback.serverReceiveMsg(message.toString());
		}

		// if ("quit".equalsIgnoreCase(expression.trim())) {
		// session.close(true);
		// return;
		// }

		String result = "echo: " + expression;
		session.write(result);

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		Log.e("exceptionCaught", "服务器异常");
		session.close(true);
		if (null != mCallback) {

			mCallback.clientException(session.getRemoteAddress().toString());
		}
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.inputClosed(session);
		Log.e("inputClosed", "设备:" + session.getRemoteAddress().toString());
		if (null != mCallback) {

			mCallback.clientDisCon(session.getRemoteAddress().toString());
		}
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionCreated(session);

		Log.e("inputClosed", "设备:" + session.getRemoteAddress().toString()
				+ "已经连接!");
		if (null != mCallback) {

			mCallback.newClientCon(session.getRemoteAddress().toString());
		}
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionOpened(session);
	}

}
