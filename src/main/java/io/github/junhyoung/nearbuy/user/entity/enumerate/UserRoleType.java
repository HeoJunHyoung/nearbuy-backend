package io.github.junhyoung.nearbuy.user.entity.enumerate;

public enum UserRoleType {
    ADMIN, USER;

    // "ROLE_" 접두사를 처리하는 정적 팩토리 메소드
    public static UserRoleType fromRoleName(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name must not be null");
        }
        if (roleName.startsWith("ROLE_")) {
            // "ROLE_" 접두사를 제외한 나머지 부분(ex: "USER")으로 Enum 상수
            return UserRoleType.valueOf(roleName.substring(5));
        }
        // 접두사가 없다면 이름 그대로 Enum 상수
        return UserRoleType.valueOf(roleName);
    }
}
