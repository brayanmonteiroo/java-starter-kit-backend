package com.brayanmonteiro.starter.adapters.out.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class Sha256TokenHasherAdapterTest {

    @Test
    void hashIsDeterministicAndNotPlainText() {
        Sha256TokenHasherAdapter hasher = new Sha256TokenHasherAdapter();
        String a = hasher.hash("token-value");
        String b = hasher.hash("token-value");

        assertEquals(a, b);
        assertNotEquals("token-value", a);
        assertEquals(64, a.length());
    }
}
