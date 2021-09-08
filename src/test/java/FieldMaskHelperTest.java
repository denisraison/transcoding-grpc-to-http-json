import com.google.protobuf.FieldMask;
import nl.toefel.reservations.v1.Desk;
import nl.toefel.reservations.v1.Reservation;
import nl.toefel.reservations.v1.Room;
import nl.toefel.server.FieldMaskHelper;
import org.junit.Assert;
import org.junit.Test;

public class FieldMaskHelperTest {

  @Test
  public void shouldPatchMessage() {
    FieldMask fieldMask = FieldMask.newBuilder()
        .addPaths("room.name")
        .addPaths("room.desk.brand")
        .build();

    Reservation current = Reservation.newBuilder()
        .setTitle("Reservation 01")
        .setVenue("Venue 100")
        .setRoom(Room.newBuilder()
            .setName("Room name old")
            .setDesk(Desk.newBuilder()
                .setBrand("Desk brand old")
                .build())
            .build())
        .build();

    Reservation patcher = Reservation.newBuilder()
        .setTitle("Wrong...")
        .setRoom(Room.newBuilder()
            .setName("Room name new")
            .setDesk(Desk.newBuilder()
                .setBrand("Desk brand new")
                .build())
            .build())
        .build();

    Reservation updated =
        (Reservation) FieldMaskHelper.applyFieldMask(current, patcher, fieldMask);

    Assert.assertEquals("Reservation 01", updated.getTitle());
    Assert.assertEquals("Venue 100", updated.getVenue());

    Assert.assertEquals("Room name new", updated.getRoom().getName());
    Assert.assertEquals("Desk brand new", updated.getRoom().getDesk().getBrand());
  }

  @Test(expected = RuntimeException.class)
  public void shouldFailForDifferentTypes(){
    FieldMask f = FieldMask.newBuilder().build();
    Reservation r = Reservation.newBuilder().build();
    Desk d = Desk.newBuilder().build();

    FieldMaskHelper.applyFieldMask(r, d, f);
  }
}
