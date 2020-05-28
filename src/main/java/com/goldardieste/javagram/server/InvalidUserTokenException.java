package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.datacontainers.UserToken;

/**
 * This exception is thrown when a {@link UserToken} that is parsed by the server is
 * found to not be legitimate.
 */
public class InvalidUserTokenException extends Exception {
}
