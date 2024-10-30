package com.docde.domain.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CItest {
    @Test
    public void test() {
        Long a = 5L;
        Long b = 10L;

        assertEquals(a + b, 15L);
    }
}
