package org.poseidon;


import org.poseidon.experiment.ProductModel;
import org.poseidon.experiment.UserModel;
import org.raniaia.poseidon.BeanManager;
import org.raniaia.poseidon.components.cache.PoseidonCache;
import org.raniaia.poseidon.components.db.JdbcSupport;
import org.raniaia.poseidon.components.db.NativeResult;
import org.raniaia.poseidon.framework.tools.StringUtils;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 多线程调度测试
 *
 * @author tiansheng
 * @version 1.0.0
 * @date 2019/11/15 14:15
 * @since 1.8
 */
public class ThreadExample {

   volatile static int closeCount = 0;
   volatile static int createCount = 0;

    public static final JdbcSupport jdbc = BeanManager.get(JdbcSupport.class);

    public static void main(String[] args) {
        userModelInsert();
        // productModelInsert();
        // cacheReadAndWrite();
    }

    public static void userModelInsert() {
        for (int i = 0; i < 10000; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    UserModel model = new UserModel();
                    String uuid = UUID.randomUUID().toString();
                    model.setUserName(uuid);
                    model.setGoogleEmail(uuid);
                    model.setAddress(uuid);
                    model.setUuid(uuid);
                    model.setCreateTime(new Date());
                    jdbc.insert(model);
                }
                closeCount++;
                System.err.println(StringUtils.format("线程退出[{}]", closeCount));
            }).start();
            createCount++;
            System.err.println(StringUtils.format("线程创建[{}]", createCount));
        }
    }

    public static void productModelInsert() {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    ProductModel model = new ProductModel();
                    String uuid = UUID.randomUUID().toString();
                    model.setProductName("产品[".concat(String.valueOf(new Date().getTime())).concat("]"));
                    model.setUuid(uuid);
                    jdbc.insert(model);
                }
                System.err.println("线程退出");
            }).start();
            System.err.println("线程创建");
        }
    }

    public static void cacheReadAndWrite() {
        final PoseidonCache cache = BeanManager.get("cache");
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                for (int j = 0; j < 200; j++) {
                    if (j % 2 == 0) {
                        cache.save("select", new NativeResult() {
                            @Override
                            public NativeResult build(ResultSet rset) {
                                return null;
                            }

                            @Override
                            public <T> T conversionJavaBean(Class<T> target) {
                                return null;
                            }

                            @Override
                            public <T> List<T> conversionJavaList(Class<T> target) {
                                return null;
                            }

                            @Override
                            public String toJSONString() {
                                return null;
                            }

                            @Override
                            public void hasNext() {

                            }

                            @Override
                            public String next() {
                                return null;
                            }

                            @Override
                            public void reset() {

                            }
                        }, "a");
                    } else {
                        cache.refreshAll();
                    }
                }
                closeCount++;
                System.err.println("线程退出:[" + closeCount + "]");
                System.out.println();
            }).start();
            createCount++;
            System.err.println("线程创建[" + createCount + "]");
        }
    }

}