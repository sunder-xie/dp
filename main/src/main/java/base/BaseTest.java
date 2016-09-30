package base;

import dp.biz.common.CommonBiz;
import dp.dao.mapper.CommonMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Writer;

/**
 * Created by huangzhangting on 16/4/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context.xml"})
public class BaseTest {
    @Autowired
    protected CommonMapper commonMapper;
    @Autowired
    protected CommonBiz commonBiz;

    protected String path;

    protected Writer writer;

    //强制中断程序，测试时，方便使用
    protected void forcedInterrupt(){
        throw new RuntimeException("强制中断程序");
    }

}
