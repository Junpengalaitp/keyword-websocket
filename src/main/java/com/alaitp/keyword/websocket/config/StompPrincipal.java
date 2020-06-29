package com.alaitp.keyword.websocket.config;

import lombok.Data;

import java.security.Principal;

@Data
class StompPrincipal implements Principal {
    String name;

    StompPrincipal(String name) {
        this.name = name;
    }
}
