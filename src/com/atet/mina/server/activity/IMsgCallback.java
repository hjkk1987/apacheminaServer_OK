package com.atet.mina.server.activity;

public interface IMsgCallback {

	public void newClientCon(String address);

	public void clientDisCon(String address);

	public void serverReceiveMsg(String msg);

	public void clientException(String address);
}
