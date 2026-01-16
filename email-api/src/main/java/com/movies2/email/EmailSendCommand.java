package com.movies2.email;

import java.util.List;

public record EmailSendCommand(
        String subject,
        String content,
        List<String> recipients
) { }
