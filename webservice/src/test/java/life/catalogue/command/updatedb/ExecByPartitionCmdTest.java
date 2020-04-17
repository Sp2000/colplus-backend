package life.catalogue.command.updatedb;

import life.catalogue.command.CmdTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExecByPartitionCmdTest extends CmdTestBase {

  public ExecByPartitionCmdTest() {
    super(new ExecByPartitionCmd());
  }

  @Test
  public void execute() throws Exception {
    assertTrue(run("execByPartition", "--prompt", "0", "--sql", "SELECT id, scientific_name FROM name_{KEY} LIMIT 1").isEmpty());
  }

}