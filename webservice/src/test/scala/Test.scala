import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.{Assert, Test}

class TestSuit{

  @Test
  def test: Unit = {
//    val sample = "{\"goalID\":\"1fcdf292-4179-4f17-9772-2e468c333915:(04/10/2018 08:41:50,828):(04/10/2018 08:41:53,098)\"}"

    val sample = "{\"startTime\":-1,\"endTime\":-1,\"smartType\":1,\"goalType\":-1,\"minDuration\":-1,\"maxDuration\":-1}"
    val mapper = new ObjectMapper

//    val obj = mapper.readValue(sample, classOf[InceptorTLFilterStateBeans])
    println()
  }
}
