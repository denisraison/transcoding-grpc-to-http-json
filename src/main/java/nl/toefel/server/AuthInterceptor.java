package nl.toefel.server;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class AuthInterceptor implements ServerInterceptor {
  public static final Context.Key<Identity> ACCESS = Context.key("access");

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                               Metadata headers,
                                                               ServerCallHandler<ReqT, RespT> next) {
    Identity identity = validateToken(headers);
    if (identity == null) {
      // Assume user not authenticated
      call.close(Status.UNAUTHENTICATED.withDescription("invalid token :("), new Metadata());
      return new ServerCall.Listener<ReqT>() {
      };
    }

    Context context = Context.current().withValue(ACCESS, identity);
    return Contexts.interceptCall(context, call, headers, next);
  }

  Identity validateToken(Metadata metadata) {
    String token = metadata.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
    // TODO: Validate token here. Call the auth service if necessary
    return token != null ? new Identity(token) : null;
  }
}
