package com.ruizhou.aicoder.service;

import com.mybatisflex.core.service.IService;
import com.ruizhou.aicoder.entity.User;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/liyupi">ruizhou</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);
    public String getEncryptPassword(String userPassword);
}
