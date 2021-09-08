package nl.toefel.server;

import com.google.protobuf.Descriptors;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;

public class FieldMaskHelper {

  public static Message applyFieldMask(Message current, Message patcher, FieldMask mask) {
    Message updated = current;

    if (mask == null) {
      return updated;
    }

    if (!current.getDescriptorForType().getFullName().equals(patcher.getDescriptorForType().getFullName())) {
      throw new RuntimeException("current and patcher must be the same type");
    }

    for (String path : mask.getPathsList()) {
      Message.Builder patcheeBuilder = updated.toBuilder();

      ParentField patcherParentField = getField(patcher.toBuilder(), path);
      ParentField patcheeParentField = getField(patcheeBuilder, path);

      patcheeParentField.parent.setField(patcheeParentField.field, patcherParentField.parent.getField(patcherParentField.field));
      updated = patcheeBuilder.build();
    }

    return updated;
  }

  public static ParentField getField(Message.Builder msg, String path) {
    Message.Builder parent = msg;
    String[] names = path.split("\\.");
    Descriptors.FieldDescriptor field = null;
    int i = 0;
    for (String name : names) {
      field = parent.getDescriptorForType().findFieldByName(name);
      if (i < names.length - 1) {
        parent = parent.getFieldBuilder(field);
      }
      i++;
    }
    return new ParentField(parent, field);
  }

  public static class ParentField {
    public Message.Builder parent;
    public Descriptors.FieldDescriptor field;

    public ParentField(Message.Builder parent, Descriptors.FieldDescriptor field) {
      this.parent = parent;
      this.field = field;
    }
  }
}
