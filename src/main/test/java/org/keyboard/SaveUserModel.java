package org.keyboard;

import org.junit.Test;

import org.raniaia.poseidon.BeanManager;
import org.raniaia.poseidon.components.db.JdbcSupport;
import org.poseidon.experiment.UserInfo;

/**
 * Copyright by tiansheng on 2020/2/8 21:22
 * @author tiansheng
 * @version 1.0.0
 * @since 1.8
 */
public class SaveUserModel {

    private JdbcSupport jdbcSupport = BeanManager.get("jdbc");

    @Test
    public void test(){
        UserInfo info = new UserInfo();
        info.setName("test01");
        info.setEmail("raniaia@123.com");
        System.out.println(jdbcSupport.insert(info));
    }

}