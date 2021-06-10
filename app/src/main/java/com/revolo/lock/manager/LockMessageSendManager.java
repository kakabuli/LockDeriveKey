package com.revolo.lock.manager;

/**
 * 消息发送管理
 */
public class LockMessageSendManager {
    private static LockMessageSendManager lockMessageSendManager;

    public static LockMessageSendManager getInstance() {
        if (null == lockMessageSendManager) {
            lockMessageSendManager = new LockMessageSendManager();
        }
        return lockMessageSendManager;
    }

    public interface LockMessageSendDao {
        void onSendBle();

        void onSendMqtt();
    }

    private LockMessageSendDao sendDao;

    public void setLockMessageSendDao(LockMessageSendDao lockMessageSendDao) {
        sendDao = lockMessageSendDao;
    }

    //下发下一条消息
    public void nextSendMessage() {
    }

    //清理消息
    public void removeMessage() {
    }

  //  public VO
}
