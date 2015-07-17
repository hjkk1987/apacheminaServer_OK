package com.atet.mina.server.activity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements IMsgCallback {
	private Button btnConnect = null;
	private Button btnDisconnect = null;
	private final int CONNECT_SERVER_SUCCESS = 1;
	private final int CONNECT_SERVER_FAIL = 2;
	private final int CREATE_SERVER_SUCCESS = 3;
	private final int CREATE_SERVER_FAIL = 4;
	private final int CONNECT_HTTP_SERVER_SUCCESS = 5;
	private final int REFRESH_TV_CONTENT = 6;
	private final int PORT = 60135;
	private final String CHAR_SET = ChangeCharSet.US_ASCII;
	private final int THEAD_POOL_NUM = 5;

	private ExecutorService service = null;
	private Context ctx;
	private IoAcceptor acceptor;
	private String TAG = "MainActivity";
	private Button btnSend = null;
	private EditText editText = null;
	private InetSocketAddress inetSocketAddress = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ctx = MainActivity.this;
		service = Executors.newFixedThreadPool(THEAD_POOL_NUM);
		inetSocketAddress = new InetSocketAddress(PORT);
		widget_init();
	}

	public void startServer() {

		Log.d(TAG, "onCreateServerClicked");

		if (service == null) {

			return;
		}

		service.submit(new Runnable() {

			@Override
			public void run() {

				createServer();
			}
		});

	}

	private void createServer() {
		if (acceptor == null || !acceptor.isActive()) {
			acceptor = new NioSocketAcceptor();

			try {

				DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

				TextLineCodecFactory factory = new TextLineCodecFactory(
						Charset.forName(CHAR_SET));

				factory.setDecoderMaxLineLength(Integer.MAX_VALUE);
				factory.setEncoderMaxLineLength(Integer.MAX_VALUE);
				chain.addLast("codec", new ProtocolCodecFilter(factory));

				acceptor.setHandler(new ServerHandler(this));

				acceptor.getSessionConfig()
						.setIdleTime(IdleStatus.BOTH_IDLE, 3);
				DefaultSocketSessionConfig socketSessionConfig = (DefaultSocketSessionConfig) acceptor
						.getSessionConfig();
				socketSessionConfig.setReuseAddress(true);
				socketSessionConfig.setKeepAlive(true);
				socketSessionConfig.setSoLinger(1);

				acceptor.setCloseOnDeactivation(true);
				acceptor.setDefaultLocalAddress(inetSocketAddress);
				acceptor.bind();
				myHandler.sendEmptyMessage(CREATE_SERVER_SUCCESS);
			} catch (IOException e) {
				System.out.println("cat io exception");

				Log.d("Main", "cat exp: " + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	private void closeServer() {
		if (null != acceptor) {
			acceptor.unbind();
			acceptor.dispose();
			acceptor = null;
		}
	}

	private void widget_init() {
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnDisconnect = (Button) findViewById(R.id.btndisConnect);
		btnSend = (Button) findViewById(R.id.btnMsgSend);
		editText = (EditText) findViewById(R.id.etSendMsg);
		btnConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startServer();
			}
		});

		btnDisconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				closeServer();
			}
		});
	}

	private void sendMessage() {

	}

	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case CONNECT_SERVER_SUCCESS:
				Toast.makeText(ctx, "connect to server success",
						Toast.LENGTH_SHORT).show();
				break;

			case CREATE_SERVER_SUCCESS:
				Toast.makeText(ctx, "create server success", Toast.LENGTH_SHORT)
						.show();

				break;

			case CONNECT_SERVER_FAIL:
				Toast.makeText(ctx, "connect to server fail",
						Toast.LENGTH_SHORT).show();
				break;
			case CREATE_SERVER_FAIL:
				Toast.makeText(ctx, "create server fail", Toast.LENGTH_SHORT)
						.show();
				break;

			case CONNECT_HTTP_SERVER_SUCCESS:

				Bundle data = msg.getData();
				String rsp = data.getString("msg");

				Toast.makeText(ctx, "http rsp: " + rsp, Toast.LENGTH_SHORT)
						.show();
				break;

			case REFRESH_TV_CONTENT:

				break;
			}
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void serverReceiveMsg(final String msg) {
		// TODO Auto-generated method stub

		// runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// Toast.makeText(ctx, "服务端接收到信息为:" + msg, Toast.LENGTH_SHORT)
		// .show();
		// }
		// });

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

		closeServer();
	}

	@Override
	public void newClientCon(final String address) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "设备:" + address + " 连接成功!",
						1000).show();
			}
		});
	}

	@Override
	public void clientDisCon(final String address) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "设备:" + address + " 断开!",
						1000).show();
			}
		});
	}

	@Override
	public void clientException(final String address) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "设备:" + address + " 异常断开!",
						1000).show();
			}
		});
	}
}
