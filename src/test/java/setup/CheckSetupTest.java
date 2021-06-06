package setup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CheckSetupTest {
	
//	@Test
//	public void itKnowsSaturdayIsAHoliday() {
//		Setup setup = new Setup();
//		assertTrue(setup.isTodayHoliday("SATURDAY"));
//	}
	
	@Test
	public void itKnowsSaturdayIsAHolidayJava8Version() {
		Setup setup = new Setup();
		assertTrue(setup.isTodayHoliday8("SATURDAY"));
	}

}
