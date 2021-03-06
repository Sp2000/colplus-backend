package life.catalogue.api.event;

import com.google.common.base.Preconditions;
import life.catalogue.api.model.DataEntity;
import life.catalogue.api.model.Dataset;

/**
 * A changed entity message for the bus system.
 * Creation or updates result in a changed message with an existing key and obj,
 * deletions result in a change message with a key but a null obj.
 * @param <K> key type
 * @param <T> entity type
 */
public class EntityChanged<K, T> {
  public final K key;
  public final T obj;
  public final Class<T> objClass;
  private final boolean created;

  public static <K, T extends DataEntity<K>>  EntityChanged<K,T> created(T obj){
    return new EntityChanged<>(obj.getKey(), obj, true, (Class<T>) obj.getClass());
  }

  public static <K, T extends DataEntity<K>>  EntityChanged<K,T> change(T obj){
    return new EntityChanged<>(obj.getKey(), obj, false, (Class<T>) obj.getClass());
  }

  public static <K, T> EntityChanged<K, T> delete(K key, Class<T> objClass){
    return new EntityChanged<>(key, null, false, objClass);
  }

  EntityChanged(K key, T obj, boolean created, Class<T> objClass) {
    this.key = Preconditions.checkNotNull(key);
    this.obj = obj; // can be null in case of deletions
    this.objClass = Preconditions.checkNotNull(objClass);
    this.created = created;
  }

  public boolean isDeletion(){
    return obj == null;
  }

  public boolean isCreated(){
    return obj != null && created;
  }

  public boolean isUpdated(){
    return obj != null && !created;
  }
}
