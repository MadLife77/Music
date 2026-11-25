package cn.edu.seig.vibemusic.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.util.function.Function;

/**
 * 密码工具类
 * 支持BCrypt加密，同时兼容旧的MD5密码
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    /**
     * 判断密码是否为BCrypt加密格式（以$2开头）
     */
    public static boolean isBCrypt(String encodedPassword) {
        return encodedPassword != null && encodedPassword.startsWith("$2");
    }

    /**
     * 判断密码是否为MD5加密格式（32位十六进制字符串）
     */
    public static boolean isMD5(String password) {
        if (password == null) {
            return false;
        }
        return password.matches("^[a-fA-F0-9]{32}$");
    }

    /**
     * 使用BCrypt加密密码
     */
    public static String encodeBCrypt(String rawPassword) {
        return BCRYPT.encode(rawPassword);
    }

    /**
     * 验证密码（BCrypt方式）
     */
    public static boolean verifyBCrypt(String rawPassword, String encodedPassword) {
        return BCRYPT.matches(rawPassword, encodedPassword);
    }

    /**
     * MD5加密（保持与旧系统兼容）
     */
    public static String encodeMD5(String rawPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes());
    }

    /**
     * 验证密码（兼容BCrypt和MD5）
     * 
     * @param rawPassword     原始密码
     * @param encodedPassword 数据库中加密后的密码
     * @param upgradeFunction 如果是旧MD5密码，验证通过后升级的回调函数
     * @return true-验证通过，false-验证失败
     */
    public static boolean verifyPassword(String rawPassword, String encodedPassword, 
            Function<String, Boolean> upgradeFunction) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        // 如果是BCrypt格式，直接使用BCrypt验证
        if (isBCrypt(encodedPassword)) {
            return BCRYPT.matches(rawPassword, encodedPassword);
        }

        // 如果是MD5格式，验证MD5
        if (isMD5(encodedPassword)) {
            String md5Password = encodeMD5(rawPassword);
            if (md5Password.equals(encodedPassword)) {
                // MD5验证成功，如果有升级函数，则升级密码
                if (upgradeFunction != null) {
                    upgradeFunction.apply(rawPassword);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 验证密码（简化版本，不自动升级）
     */
    public static boolean verifyPassword(String rawPassword, String encodedPassword) {
        return verifyPassword(rawPassword, encodedPassword, null);
    }

    /**
     * 验证并升级密码
     * 返回true表示验证通过
     * 如果是MD5密码且验证通过，会自动升级为BCrypt
     */
    public static boolean verifyAndUpgrade(String rawPassword, String encodedPassword, 
            java.util.function.Consumer<String> upgradeConsumer) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        // 如果是BCrypt格式，直接验证
        if (isBCrypt(encodedPassword)) {
            return BCRYPT.matches(rawPassword, encodedPassword);
        }

        // 如果是MD5格式，验证并升级
        if (isMD5(encodedPassword)) {
            String md5Password = encodeMD5(rawPassword);
            if (md5Password.equals(encodedPassword)) {
                // 升级为BCrypt
                if (upgradeConsumer != null) {
                    upgradeConsumer.accept(rawPassword);
                }
                return true;
            }
        }

        return false;
    }
}
