package nl.toefel.server;

import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nl.toefel.reservations.v1.CreateReservationRequest;
import nl.toefel.reservations.v1.DeleteReservationRequest;
import nl.toefel.reservations.v1.GetReservationRequest;
import nl.toefel.reservations.v1.ListReservationsRequest;
import nl.toefel.reservations.v1.Person;
import nl.toefel.reservations.v1.Reservation;
import nl.toefel.reservations.v1.ReservationServiceGrpc;
import nl.toefel.reservations.v1.UpdateReservationRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservationController extends ReservationServiceGrpc.ReservationServiceImplBase {

  private final ReservationRepository reservationRepository;

  public ReservationController(ReservationRepository reservationRepository) {
    this.reservationRepository = reservationRepository;
  }

  @Override
  public void createReservation(CreateReservationRequest request, StreamObserver<Reservation> responseObserver) {
    System.out.println("createReservation() called");
    Identity identity = AuthInterceptor.ACCESS.get();

    Reservation createdReservation = reservationRepository.createReservation(request.getReservation());
    responseObserver.onNext(createdReservation);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteReservation(DeleteReservationRequest request, StreamObserver<Empty> responseObserver) {
    System.out.println("deleteReservation() called");
    reservationRepository.deleteReservation(request.getId());
    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void getReservation(GetReservationRequest request, StreamObserver<Reservation> responseObserver) {
    System.out.println("getReservation() called");
    Optional<Reservation> optionalReservation = reservationRepository.findReservation(request.getId());
    if (optionalReservation.isPresent()) {
      responseObserver.onNext(optionalReservation.orElse(Reservation.newBuilder().build()));
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(Status.NOT_FOUND
          .withDescription("no reservation with id " + request.getId())
          .asRuntimeException());
    }
  }

  @Override
  public void listReservations(ListReservationsRequest request, StreamObserver<Reservation> responseObserver) {
    System.out.println("listReservations() called with " + request);
    if ("error".equals(request.getRoom())) {
      responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
    } else if ("throw".equals(request.getRoom())) {
      throw Status.UNAUTHENTICATED.asRuntimeException();
    } else {
      // nothing, empty response should yield []
      responseObserver.onCompleted();
    }
  }

  private boolean hasAttendeeLastNames(Reservation it, List<String> requiredAttendeeLastNames) {
    List<String> reservationAttendeeLastNames = it.getAttendeesList().stream().map(Person::getLastName).collect(Collectors.toList());
    return reservationAttendeeLastNames.containsAll(requiredAttendeeLastNames);
  }

  @Override
  public void updateReservation(UpdateReservationRequest request, StreamObserver<Reservation> responseObserver) {
    System.out.println("updateReservation() called");
    Optional<Reservation> optionalReservation = reservationRepository.findReservation(request.getId());
    if (optionalReservation.isPresent()) {

      if (request.hasFieldMask()) {
        FieldMask fieldMask = request.getFieldMask();
        FieldMaskHelper.applyFieldMask(optionalReservation.get(), request.getReservation(), fieldMask);
      }

      responseObserver.onNext(optionalReservation.orElse(Reservation.newBuilder().build()));
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(Status.NOT_FOUND
          .withDescription("no reservation with id " + request.getId())
          .asRuntimeException());
    }
  }

  public void doPut() {

  }
}
