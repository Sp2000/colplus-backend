package life.catalogue.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import life.catalogue.api.vocab.Country;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Organisation {
  private String name;
  private String department;
  private String city;
  private String state;
  private Country country;

  public static List<Organisation> parse(String... names) {
    return parse(List.of(names));
  }

  public static List<Organisation> parse(List<String> names) {
    return names == null ? null : names.stream().map(Organisation::parse).collect(Collectors.toList());
  }

  public static Organisation parse(String name) {
    return new Organisation(name);
  }

  public Organisation() {
  }

  public Organisation(Organisation other) {
    this.name = other.name;
    this.department = other.department;
    this.city = other.city;
    this.state = other.state;
    this.country = other.country;
  }

  public Organisation(String name) {
    this(name, null, null, null, null);
  }

  public Organisation(String name, String department, String city, String state, Country country) {
    this.name = name;
    this.department = department;
    this.city = city;
    this.state = state;
    this.country = country;
  }

  public String getLabel() {
    if (isEmpty()) return null;

    StringBuilder sb = new StringBuilder();
    append(sb, department);
    append(sb, name);
    append(sb, city);
    append(sb, state);
    if (country != null) {
      append(sb, country.getTitle());
    }
    return sb.toString();
  }

  public String quoteParts() {
    if (isEmpty()) return null;

    StringBuilder sb = new StringBuilder();
    quote(sb, department);
    sb.append(',');
    quote(sb, name);
    sb.append(',');
    quote(sb, city);
    sb.append(',');
    quote(sb, state);
    if (country != null) {
      sb.append(',');
      quote(sb, country.getTitle());
    }
    return sb.toString();
  }
  private static void quote(StringBuilder sb, String x) {
    sb.append('\"');
    if (x != null) {
      sb.append(x);
    }
    sb.append('\"');
  }

  @JsonIgnore
  public boolean isEmpty(){
    return name == null && department == null && city == null && state == null && country == null;
  }

  private static void append(StringBuilder sb, String x) {
    if (x != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(x);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Organisation)) return false;
    Organisation that = (Organisation) o;
    return Objects.equals(name, that.name) &&
      Objects.equals(department, that.department) &&
      Objects.equals(city, that.city) &&
      Objects.equals(state, that.state) &&
      country == that.country;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, department, city, state, country);
  }

  @Override
  public String toString() {
    return getLabel();
  }
}
