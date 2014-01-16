package com.alcatel_lucent.dms.service;

import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.User;

@Service(value = "userService")
public class UserServiceImpl extends BaseServiceImpl implements UserService {

    @Override
    public User createUser(User user) throws BusinessException {
        User dbUser = (User) dao.retrieve(User.class, user.getLoginName());
        if (dbUser != null) {
            throw new BusinessException(BusinessException.USER_ALREADY_EXISTS, user.getLoginName());
        }
        return (User) dao.create(user);
    }


    @Override
    public User updateUser(String loginName, String name, String email,
                           Integer role, Integer status, Boolean isShowTip) throws BusinessException {
        User user = (User) dao.retrieve(User.class, loginName);
        if (user == null) {
            throw new BusinessException(BusinessException.USER_NOT_FOUND, loginName);
        }
        if (name != null) {
            user.setName(name);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }
        if (isShowTip != null) {
            user.setShowTips(isShowTip);
        }
        return user;
    }

    @Override
    public User mergeUser(User user) {
        return (User) dao.merge(user);
    }

}
