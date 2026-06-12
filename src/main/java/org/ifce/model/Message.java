package org.ifce.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record Message(
        String sender,
        String receiver,
        String content,
        LocalDateTime timestamp
) implements Serializable {
}