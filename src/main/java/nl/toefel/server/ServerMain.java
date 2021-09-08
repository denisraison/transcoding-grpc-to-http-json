package nl.toefel.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import java.io.IOException;

public class ServerMain {
  public static void main(String[] args) throws IOException, InterruptedException {

    ReservationRepository repository = new ReservationRepository();

    Server service = ServerBuilder.forPort(53000)
        .addService(ServerInterceptors.intercept(new ReservationController(repository),
            new AuthInterceptor()))
        .build()
        .start();

    Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
    System.out.println("Started listening for rpc calls on 53000...");
    service.awaitTermination();
  }

}
