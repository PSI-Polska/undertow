package io.undertow.server.handlers;

import java.io.IOException;

import io.undertow.Handlers;
import io.undertow.UndertowLogger;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.protocol.http.HttpContinue;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Handler that provides support for HTTP/1.1 continue responses.
 * <p/>
 * By default this will accept all requests. To change this behaviour this
 * handler must be subclassed and the {@link #acceptRequest(io.undertow.server.HttpServerExchange)}
 * method overridden tp provide the desired behaviour.
 *
 * @see io.undertow.server.protocol.http.HttpContinue
 * @author Stuart Douglas
 */
public class HttpContinueAcceptingHandler implements HttpHandler {

    private volatile HttpHandler next;

    public HttpContinueAcceptingHandler(HttpHandler next) {
        this.next = next;
    }

    public HttpContinueAcceptingHandler() {
        this(ResponseCodeHandler.HANDLE_404);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(HttpContinue.requiresContinueResponse(exchange)) {
            if(acceptRequest(exchange)) {
                HttpContinue.sendContinueResponse(exchange, new IoCallback() {
                    @Override
                    public void onComplete(final HttpServerExchange exchange, final Sender sender) {
                        exchange.dispatch(next);
                    }

                    @Override
                    public void onException(final HttpServerExchange exchange, final Sender sender, final IOException exception) {
                        UndertowLogger.REQUEST_IO_LOGGER.ioException(exception);
                        exchange.endExchange();
                    }
                });

            } else {
                HttpContinue.rejectExchange(exchange);
            }
        } else {
            next.handleRequest(exchange);
        }
    }

    protected boolean acceptRequest(final HttpServerExchange exchange) {
        return true;
    }

    public HttpHandler getNext() {
        return next;
    }

    public HttpContinueAcceptingHandler setNext(final HttpHandler next) {
        Handlers.handlerNotNull(next);
        this.next = next;
        return this;
    }
}
