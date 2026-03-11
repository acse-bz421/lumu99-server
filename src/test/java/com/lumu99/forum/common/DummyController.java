package com.lumu99.forum.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DummyController {

    @GetMapping("/dummy/error")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void throwError() {
        throw new IllegalArgumentException("bad request");
    }
}
