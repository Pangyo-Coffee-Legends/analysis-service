package com.nhnacademy.workanalysis.exception;

/**
 * 지정한 쓰레드 ID에 해당하는 쓰레드가 존재하지 않을 때 발생하는 예외입니다.
 */
public class AiChatThreadNotFoundException extends RuntimeException {
public AiChatThreadNotFoundException() {
    super("삭제할 쓰레드가 존재하지 않습니다.");

}
    /**
     * 메시지를 포함한 예외 생성자입니다.
     *
     * @param message 예외 메시지
     */
    public AiChatThreadNotFoundException(String message) {
        super(message);
    }


}
