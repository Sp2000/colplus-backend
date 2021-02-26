package life.catalogue.api.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import life.catalogue.api.datapackage.ColdpTerm;
import life.catalogue.api.vocab.CSLRefType;
import life.catalogue.api.vocab.ColDwcTerm;
import life.catalogue.api.vocab.Country;
import life.catalogue.api.vocab.TxtTreeTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.nameparser.api.Authorship;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Jackson module that defines all serde rules for all CoL API model classes.
 */
public class ApiModule extends SimpleModule {

  public static final ObjectMapper  MAPPER = configureMapper(new ObjectMapper());

  static {
    // register new term enums
    TermFactory.instance().registerTermEnum(ColDwcTerm.class);
    TermFactory.instance().registerTermEnum(ColdpTerm.class);
    TermFactory.instance().registerTermEnum(TxtTreeTerm.class);
  }
  
  public static ObjectMapper configureMapper(ObjectMapper mapper) {
    // keep all capital fields as such, dont lowercase them!!
    mapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    mapper.registerModule(new ApiModule());
    
    // flaky with java 11
    // if broken consider the experimental replacement blackbird: https://github.com/stevenschlansker/jackson-blackbird
    mapper.registerModule(new AfterburnerModule());
  
    mapper.addHandler(new CslArrayMismatchHandler());
    
    return mapper;
  }
  
  public ApiModule() {
    super("ColApi", new Version(1, 0, 0, null, "org.catalogueoflife", "api"));

    // first deserializers
    addDeserializer(Country.class, new CountrySerde.Deserializer());
    addDeserializer(CSLRefType.class, new CSLRefTypeSerde.Deserializer());
    addDeserializer(URI.class, new URIDeserializer());
    addDeserializer(UUID.class, new UUIDSerde.Deserializer());
    // override the JavaTimeModule to use a permissive localdate deserializer catching parsing exceptions
    addDeserializer(LocalDateTime.class, new PermissiveJavaDateSerde.LocalDateTimeDeserializer());
    addDeserializer(LocalDate.class, new PermissiveJavaDateSerde.LocalDateDeserializer());

    // then serializers:
    addSerializer(Country.class, new CountrySerde.Serializer());
    addSerializer(CSLRefType.class, new CSLRefTypeSerde.Serializer());
    addSerializer(UUID.class, new UUIDSerde.Serializer());

    // then key deserializers
    addKeyDeserializer(Country.class, new CountrySerde.KeyDeserializer());
    addKeyDeserializer(UUID.class, new UUIDSerde.KeyDeserializer());

    // then key serializers
    addKeySerializer(Country.class, new CountrySerde.FieldSerializer());
    addKeySerializer(UUID.class, new UUIDSerde.FieldSerializer());

    // fastutils primitive collection
    FastutilsSerde.register(this);
  }

  @Override
  public void setupModule(SetupContext ctxt) {
    // default enum serde
    ctxt.addDeserializers(new PermissiveEnumSerde.PermissiveEnumDeserializers());
    ctxt.addSerializers(new PermissiveEnumSerde.PermissiveEnumSerializers());
    ctxt.addKeySerializers(new PermissiveEnumSerde.PermissiveEnumKeySerializers());
    // lower camel case, permissive enum serde
    ctxt.addDeserializers(new LowerCamelCaseEnumSerde.LowerCamelCaseEnumDeserializers());
    ctxt.addKeyDeserializers(new LowerCamelCaseEnumSerde.LowerCamelCaseEnumKeyDeserializers());
    ctxt.addSerializers(new LowerCamelCaseEnumSerde.LowerCamelCaseEnumSerializers());
    ctxt.addKeySerializers(new LowerCamelCaseEnumSerde.LowerCamelCaseEnumKeySerializers());
    // required to properly register serdes
    super.setupModule(ctxt);
    ctxt.setMixInAnnotations(Authorship.class, AuthorshipMixIn.class);
    ctxt.setMixInAnnotations(Term.class, TermMixIn.class);
  }
  
  abstract class AuthorshipMixIn {
    @JsonIgnore
    abstract boolean isEmpty();
  }

  @JsonSerialize(using = TermSerde.Serializer.class, keyUsing = TermSerde.KeySerializer.class)
  @JsonDeserialize(using = TermSerde.Deserializer.class, keyUsing = TermSerde.KeyDeserializer.class)
  static abstract class TermMixIn {

  }

  static class URIDeserializer extends FromStringDeserializer<URI> {
  
    protected URIDeserializer() {
      super(URI.class);
    }
  
    @Override
    protected URI _deserializeFromEmptyString() throws IOException {
      return null;
    }
  
    @Override
    protected URI _deserialize(String value, DeserializationContext ctxt) throws IOException {
      return URI.create(value);
    }
  }
}
