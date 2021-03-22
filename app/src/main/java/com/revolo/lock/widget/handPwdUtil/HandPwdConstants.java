package com.revolo.lock.widget.handPwdUtil;

public class HandPwdConstants {

	private HandPwdConstants() {
	}

	//*/ 手势密码点的状态
	public static final int POINT_STATE_NORMAL = 0; // 正常状态

	public static final int POINT_STATE_SELECTED = 1; // 按下状态

	public static final int POINT_STATE_WRONG = 2; // 错误状态

	//保存在绑定网关的时候选择的网关id
	public static final String GATEWAYID="gatewayId";

}
