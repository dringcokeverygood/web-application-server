package util;

import org.junit.Assert;
import org.junit.Test;

public class StringIndexTest {

    @Test
    public void 인덱스_테스트1() {
        Assert.assertEquals(0, "123".indexOf("1"));
    }
    @Test
    public void 인덱스_테스트2() {
        Assert.assertEquals(-1, "123".indexOf("4"));
    }
}
