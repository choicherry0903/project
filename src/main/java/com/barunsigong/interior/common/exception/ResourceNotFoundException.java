package com.barunsigong.interior.common.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() { super("요청한 정보를 찾을 수 없습니다."); }
}
