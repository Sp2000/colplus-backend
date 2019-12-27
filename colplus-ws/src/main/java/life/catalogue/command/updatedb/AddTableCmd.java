package life.catalogue.command.updatedb;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import life.catalogue.WsServerConfig;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to add new partition tables for a given master table.
 * When adding new partitioned tables to the db schema we need to create partition table
 * for every existing dataset that has data.
 *
 * The command will look at the existing name partition tables to find the datasets with data.
 * The master table must exist already and be defined to be partitioned by column dataset_key !!!
 */
public class AddTableCmd extends ConfiguredCommand<WsServerConfig> {
  private static final Logger LOG = LoggerFactory.getLogger(AddTableCmd.class);
  private static final String ARG_PROMPT = "prompt";
  private static final String ARG_TABLE = "table";

  private WsServerConfig cfg;
  
  public AddTableCmd() {
    super("addTable", "Adds new partition tables to the database schema");
  }
  
  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
    // Adds import options
    subparser.addArgument("--"+ARG_PROMPT)
        .setDefault(10)
        .dest(ARG_PROMPT)
        .type(Integer.class)
        .required(false)
        .help("Waiting time in seconds for a user prompt to abort db update. Use zero for no prompt");
    subparser.addArgument("--"+ARG_TABLE, "-t")
            .dest(ARG_TABLE)
            .type(String.class)
            .required(true)
            .help("Name of the partitioned table to attach the created partitions to");
  }
  
  @Override
  protected void run(Bootstrap<WsServerConfig> bootstrap, Namespace namespace, WsServerConfig cfg) throws Exception {
    String table = namespace.getString(ARG_TABLE);
    final int prompt = namespace.getInt(ARG_PROMPT);
    if (prompt > 0) {
      System.out.format("Adding partition tables for %s in database %s on %s.\n", table, cfg.db.database, cfg.db.host);
      System.out.format("You have %s seconds to abort if you did not intend to do so !!!\n", prompt);
      TimeUnit.SECONDS.sleep(prompt);
    }
  
    this.cfg = cfg;
    execute(table);
    System.out.println("Done !!!");
  }
  
  private void execute(String table) throws Exception {
    execute(cfg, table);
  }

  static class ForeignKey {
    final String table;
    final String column;
    final boolean cascade;

    public ForeignKey(String table, String column, boolean cascade) {
      this.table = table;
      this.column = column;
      this.cascade = cascade;
    }
  }

  public static void execute(WsServerConfig cfg, String table) throws Exception {
    final String pCreate = "CREATE TABLE %s (LIKE %s INCLUDING DEFAULTS INCLUDING CONSTRAINTS)";
    final String pCreateIdx = "CREATE INDEX ON %s (%s)";
    final String pCreateFk  = "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (id)";
    final String pAttach = "ALTER TABLE %s ATTACH PARTITION %s FOR VALUES IN ( %s )";

    LOG.info("Start adding partition tables for {}", table);
    try (Connection con = cfg.db.connect(cfg.db);
         Statement st = con.createStatement();
         PreparedStatement pExists = con.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name = ? ")
    ) {
      List<ForeignKey> fks = analyze(st, table);

      for (int key : keys(st)) {
        final String pTable = table+"_"+key;
        if (exists(pExists, pTable)) {
          LOG.info("{} already exists", pTable);

        } else {
          LOG.info("Create table {}", pTable);
          st.execute(String.format(pCreate, pTable, table));
          for (ForeignKey fk : fks) {
            // index
            st.execute(String.format(pCreateIdx, pTable, fk.column));
            // foreign key with/without cascade
            String sql = String.format(pCreateFk, pTable, pTable+"_"+fk.column+"_fk", fk.column, fk.table+"_"+key);
            if (fk.cascade) {
              sql = sql + " ON DELETE CASCADE";
            }
            st.execute(sql);
          }

          // attach
          st.execute(String.format(pAttach, table, pTable, key));
        }
      }
    }
  }

  private static boolean exists(PreparedStatement pExists, String table) throws SQLException {
    pExists.setString(1, table);
    pExists.execute();
    ResultSet rs = pExists.getResultSet();
    boolean exists = rs.next();
    rs.close();
    return exists;
  }

  @VisibleForTesting
  protected static Set<Integer> keys(Statement st) throws SQLException {
    Set<Integer> keys = new HashSet<>();
    st.execute("select table_name from information_schema.tables where table_schema='public' and table_name ~* '^name_\\d+'");
    ResultSet rs = st.getResultSet();

    Pattern TABLE = Pattern.compile("name_(\\d+)$");
    while (rs.next()) {
      String tbl = rs.getString(1);
      Matcher m = TABLE.matcher(tbl);
      if (m.find()) {
        keys.add( Integer.parseInt(m.group(1)) );
      }
    }
    rs.close();
    LOG.info("Found {} existing name partition tables", keys.size());
    return keys;
  }

  /**
   * Looks for the known foreign key columns in the master table
   */
  @VisibleForTesting
  protected static List<ForeignKey> analyze(Statement st, String table) throws SQLException {
    List<ForeignKey> fks = new ArrayList<>();

    final List<ForeignKey> knownFks = List.of(
            new ForeignKey("name", "name_id", true),
            new ForeignKey("reference", "reference_id", true),
            new ForeignKey("reference", "published_in_id", true),
            new ForeignKey("verbatim", "verbatim_key", false)
    );

    st.execute("SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '"+table+"'");
    ResultSet rs = st.getResultSet();
    while (rs.next()) {
      String col = rs.getString(1);
      for (ForeignKey fk : knownFks) {
        if (fk.column.equalsIgnoreCase(col)) {
          fks.add(fk);
          break;
        }
      }
    }
    rs.close();
    return fks;
  }

}