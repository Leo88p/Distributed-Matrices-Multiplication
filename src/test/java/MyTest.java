import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
class MyTest {
    Client Client = new Client();
    @Test
    @DisplayName("Add two numbers")
    void addition() {
        assertEquals(2, Client.add(1, 1));
    }
}
